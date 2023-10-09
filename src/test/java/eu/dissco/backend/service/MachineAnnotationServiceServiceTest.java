package eu.dissco.backend.service;

import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.givenDigitalSpecimen;
import static eu.dissco.backend.utils.DigitalMediaObjectUtils.DIGITAL_MEDIA_PATH;
import static eu.dissco.backend.utils.MachineAnnotationServiceUtils.givenFlattenedDigitalMedia;
import static eu.dissco.backend.utils.MachineAnnotationServiceUtils.givenFlattenedDigitalSpecimen;
import static eu.dissco.backend.utils.MachineAnnotationServiceUtils.givenMasResponse;
import static eu.dissco.backend.utils.MachineAnnotationServiceUtils.givenMasRecord;
import static eu.dissco.backend.utils.MasJobRecordUtils.JOB_ID;
import static eu.dissco.backend.utils.MasJobRecordUtils.givenMasJobRecordIdMap;
import static eu.dissco.backend.utils.SpecimenUtils.SPECIMEN_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import eu.dissco.backend.domain.MasTarget;
import eu.dissco.backend.repository.MachineAnnotationServiceRepository;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MachineAnnotationServiceServiceTest {

  @Mock
  private MachineAnnotationServiceRepository repository;
  @Mock
  private KafkaPublisherService kafkaPublisherService;
  @Mock
  private MasJobRecordService masJobRecordService;

  private MachineAnnotationServiceService service;

  @BeforeEach
  void setup() {
    this.service = new MachineAnnotationServiceService(repository, kafkaPublisherService, masJobRecordService, MAPPER);
  }

  @Test
  void testGetMassForObject() throws JsonProcessingException {
    // Given
    var masRecord = givenMasRecord(givenFiltersDigitalMedia(false));
    given(repository.getAllMas()).willReturn(List.of(masRecord));

    // When
    var result = service.getMassForObject(givenFlattenedDigitalMedia(), DIGITAL_MEDIA_PATH);

    // Then
    assertThat(result).isEqualTo(givenMasResponse(masRecord, DIGITAL_MEDIA_PATH));
  }

  @Test
  void testGetMassForObjectNoFilterMatch() throws JsonProcessingException {
    // Given
    var masRecord = givenMasRecord(givenFiltersDigitalMedia(true));
    given(repository.getAllMas()).willReturn(List.of(masRecord));

    // When
    var result = service.getMassForObject(givenFlattenedDigitalMedia(), DIGITAL_MEDIA_PATH);

    // Then
    assertThat(result.getData()).isEmpty();
  }

  @Test
  void testScheduleMass() throws JsonProcessingException {
    // Given
    var digitalSpecimen = givenDigitalSpecimen(ID);
    var masRecord = givenMasRecord(givenFiltersDigitalSpecimen());
    given(repository.getMasRecords(List.of(ID))).willReturn(List.of(masRecord));
    given(masJobRecordService.createMasJobRecord(Set.of(masRecord), ID)).willReturn(givenMasJobRecordIdMap(masRecord.id()));
    var sendObject = new MasTarget(digitalSpecimen, JOB_ID);

    // When
    var result = service.scheduleMass(givenFlattenedDigitalSpecimen(), List.of(ID), SPECIMEN_PATH,
        digitalSpecimen, digitalSpecimen.id());

    // Then
    assertThat(result).isEqualTo(givenMasResponse(masRecord, SPECIMEN_PATH));
    then(kafkaPublisherService).should().sendObjectToQueue("fancy-topic-name", sendObject);
  }

  @Test
  void testScheduleMassKafkaFailed() throws JsonProcessingException {
    // Given
    var digitalSpecimen = givenDigitalSpecimen(ID);
    var masRecord = givenMasRecord(givenFiltersDigitalSpecimen());
    given(repository.getMasRecords(List.of(ID))).willReturn(List.of(masRecord));
    given(masJobRecordService.createMasJobRecord(Set.of(masRecord), ID)).willReturn(givenMasJobRecordIdMap(masRecord.id()));
    var sendObject = new MasTarget(digitalSpecimen, JOB_ID);
    willThrow(JsonProcessingException.class).given(kafkaPublisherService)
        .sendObjectToQueue("fancy-topic-name", sendObject);

    // When
    var result = service.scheduleMass(givenFlattenedDigitalSpecimen(), List.of(ID), SPECIMEN_PATH,
        digitalSpecimen, digitalSpecimen.id());

    // Then
    then(masJobRecordService).should().markMasJobRecordAsFailed(List.of(JOB_ID));
    assertThat(result.getData()).isEmpty();
  }

  private JsonNode givenFiltersDigitalMedia(boolean unmatchedFilter) {
    var filters = MAPPER.createObjectNode();
    filters.set("type", MAPPER.createArrayNode().add("2DImageObject"));
    filters.set("dcterms:publisher",
        MAPPER.createArrayNode().add("Royal Botanic Garden Edinburgh"));
    filters.set("specimen.dwc:typeStatus", MAPPER.createArrayNode().add("*"));
    if (unmatchedFilter) {
      filters.set("specimen.dwc:island", MAPPER.createArrayNode().add("*"));
    }
    return filters;
  }

  private JsonNode givenFiltersDigitalSpecimen() {
    var filters = MAPPER.createObjectNode();
    filters.set("dcterms:license",
        MAPPER.createArrayNode().add("http://creativecommons.org/licenses/by/4.0/legalcode"));
    filters.set("dwc:typeStatus",
        MAPPER.createArrayNode().add("holotype"));
    return filters;
  }
}
