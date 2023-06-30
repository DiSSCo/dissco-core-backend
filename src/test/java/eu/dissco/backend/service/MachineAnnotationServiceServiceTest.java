package eu.dissco.backend.service;

import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.givenDigitalSpecimen;
import static eu.dissco.backend.utils.DigitalMediaObjectUtils.DIGITAL_MEDIA_PATH;
import static eu.dissco.backend.utils.MachineAnnotationServiceUtils.getFlattenedDigitalMedia;
import static eu.dissco.backend.utils.MachineAnnotationServiceUtils.getFlattenedDigitalSpecimen;
import static eu.dissco.backend.utils.MachineAnnotationServiceUtils.getMasResponse;
import static eu.dissco.backend.utils.MachineAnnotationServiceUtils.givenMasRecord;
import static eu.dissco.backend.utils.SpecimenUtils.SPECIMEN_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import eu.dissco.backend.repository.MachineAnnotationServiceRepository;
import java.util.List;
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

  private MachineAnnotationServiceService service;

  @BeforeEach
  void setup() {
    this.service = new MachineAnnotationServiceService(repository, kafkaPublisherService, MAPPER);
  }

  @Test
  void testGetMassForObject() throws JsonProcessingException {
    // Given
    var masRecord = givenMasRecord(givenFiltersDigitalMedia(false));
    given(repository.getAllMas()).willReturn(List.of(masRecord));

    // When
    var result = service.getMassForObject(getFlattenedDigitalMedia(), DIGITAL_MEDIA_PATH);

    // Then
    assertThat(result).isEqualTo(getMasResponse(masRecord, DIGITAL_MEDIA_PATH));
  }

  @Test
  void testGetMassForObjectNoFilterMatch() throws JsonProcessingException {
    // Given
    var masRecord = givenMasRecord(givenFiltersDigitalMedia(true));
    given(repository.getAllMas()).willReturn(List.of(masRecord));

    // When
    var result = service.getMassForObject(getFlattenedDigitalMedia(), DIGITAL_MEDIA_PATH);

    // Then
    assertThat(result.getData()).isEmpty();
  }

  @Test
  void testScheduleMass() throws JsonProcessingException {
    // Given
    var digitalSpecimen = givenDigitalSpecimen(ID);
    var masRecord = givenMasRecord(givenFiltersDigitalSpecimen());
    given(repository.getMasRecords(List.of(ID))).willReturn(List.of(masRecord));

    // When
    var result = service.scheduleMass(getFlattenedDigitalSpecimen(), List.of(ID), SPECIMEN_PATH,
        digitalSpecimen);

    // Then
    assertThat(result).isEqualTo(getMasResponse(masRecord, SPECIMEN_PATH));
    then(kafkaPublisherService).should().sendObjectToQueue("fancy-topic-name", digitalSpecimen);
  }

  @Test
  void testScheduleMassKafkaFailed() throws JsonProcessingException {
    // Given
    var digitalSpecimen = givenDigitalSpecimen(ID);
    var masRecord = givenMasRecord(givenFiltersDigitalSpecimen());
    given(repository.getMasRecords(List.of(ID))).willReturn(List.of(masRecord));
    willThrow(JsonProcessingException.class).given(kafkaPublisherService)
        .sendObjectToQueue("fancy-topic-name", digitalSpecimen);

    // When
    var result = service.scheduleMass(getFlattenedDigitalSpecimen(), List.of(ID), SPECIMEN_PATH,
        digitalSpecimen);

    // Then
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
