package eu.dissco.backend.service;

import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.ORCID;
import static eu.dissco.backend.TestUtils.givenDigitalSpecimenWrapper;
import static eu.dissco.backend.utils.DigitalMediaObjectUtils.DIGITAL_MEDIA_PATH;
import static eu.dissco.backend.utils.MachineAnnotationServiceUtils.givenFlattenedDigitalMedia;
import static eu.dissco.backend.utils.MachineAnnotationServiceUtils.givenFlattenedDigitalSpecimen;
import static eu.dissco.backend.utils.MachineAnnotationServiceUtils.givenMasRecord;
import static eu.dissco.backend.utils.MachineAnnotationServiceUtils.givenMasResponse;
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
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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

  private static Stream<Arguments> provideFilters() {
    return Stream.of(
        Arguments.of(List.of(
            Pair.of("$.ods:type", List.of("Some Test value")))),
        Arguments.of(List.of(
            Pair.of("$.ods:format", List.of("application/json")),
            Pair.of("$.digitalSpecimen.occurrences[*].location.dwc:country",
                List.of("The Netherlands", "Belgium")))),
        Arguments.of(List.of(
            Pair.of("$.ods:format", List.of("application/json")),
            Pair.of("$.digitalSpecimen.occurrences[*].dwc:city",
                List.of("Rotterdam", "Amsterdam"))))
    );
  }

  @BeforeEach
  void setup() {
    this.service = new MachineAnnotationServiceService(repository, kafkaPublisherService,
        masJobRecordService, MAPPER);
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

  @ParameterizedTest
  @MethodSource("provideFilters")
  void testGetMassForObjectNoFilterMatch(List<Pair<String, List<String>>> filters)
      throws JsonProcessingException {
    // Given
    var masRecord = givenMasRecord(givenFiltersDigitalMedia(filters));
    given(repository.getAllMas()).willReturn(List.of(masRecord));

    // When
    var result = service.getMassForObject(givenFlattenedDigitalMedia(), DIGITAL_MEDIA_PATH);

    // Then
    assertThat(result.getData()).isEmpty();
  }

  @Test
  void testScheduleMass() throws JsonProcessingException {
    // Given
    var digitalSpecimen = givenDigitalSpecimenWrapper(ID);
    var masRecord = givenMasRecord(givenFiltersDigitalSpecimen());
    given(repository.getMasRecords(List.of(ID))).willReturn(List.of(masRecord));
    given(masJobRecordService.createMasJobRecord(Set.of(masRecord), ID, ORCID)).willReturn(
        givenMasJobRecordIdMap(masRecord.id()));
    var sendObject = new MasTarget(digitalSpecimen, JOB_ID);

    // When
    var result = service.scheduleMass(givenFlattenedDigitalSpecimen(), List.of(ID), SPECIMEN_PATH,
        digitalSpecimen, digitalSpecimen.digitalSpecimen().getOdsId(), ORCID);

    // Then
    assertThat(result).isEqualTo(givenMasResponse(masRecord, SPECIMEN_PATH));
    then(kafkaPublisherService).should().sendObjectToQueue("fancy-topic-name", sendObject);
  }

  @Test
  void testScheduleMassKafkaFailed() throws JsonProcessingException {
    // Given
    var digitalSpecimenWrapper = givenDigitalSpecimenWrapper(ID);
    var masRecord = givenMasRecord(givenFiltersDigitalSpecimen());
    given(repository.getMasRecords(List.of(ID))).willReturn(List.of(masRecord));
    given(masJobRecordService.createMasJobRecord(Set.of(masRecord), ID, ORCID)).willReturn(
        givenMasJobRecordIdMap(masRecord.id()));
    var sendObject = new MasTarget(digitalSpecimenWrapper, JOB_ID);
    willThrow(JsonProcessingException.class).given(kafkaPublisherService)
        .sendObjectToQueue("fancy-topic-name", sendObject);

    // When
    var result = service.scheduleMass(givenFlattenedDigitalSpecimen(), List.of(ID), SPECIMEN_PATH,
        digitalSpecimenWrapper, digitalSpecimenWrapper.digitalSpecimen().getOdsId(), ORCID);

    // Then
    then(masJobRecordService).should().markMasJobRecordAsFailed(List.of(JOB_ID));
    assertThat(result.getData()).isEmpty();
  }

  private JsonNode givenFiltersDigitalMedia(List<Pair<String, List<String>>> filters) {
    var filterObject = MAPPER.createObjectNode();
    for (var filter : filters) {
      var arrayNode = MAPPER.createArrayNode();
      for (var value : filter.getRight()) {
        arrayNode.add(value);
      }
      filterObject.set(filter.getLeft(), arrayNode);
    }
    return filterObject;
  }

  private JsonNode givenFiltersDigitalMedia(boolean unmatchedFilter) {
    var filters = MAPPER.createObjectNode();
    filters.set("$.ods:type",
        MAPPER.createArrayNode().add("https://doi.org/21.T11148/bbad8c4e101e8af01115"));
    filters.set("$.ods:dcterms:publisher",
        MAPPER.createArrayNode().add("Royal Botanic Garden Edinburgh"));
    filters.set("$.digitalSpecimen.occurrences[*].location.dwc:country",
        MAPPER.createArrayNode().add("*"));
    if (unmatchedFilter) {
      filters.set("$.digitalSpecimen.occurrences[*].location.dwc:island",
          MAPPER.createArrayNode().add("*"));
    }
    return filters;
  }

  private JsonNode givenFiltersDigitalSpecimen() {
    var filters = MAPPER.createObjectNode();
    filters.set("$.dcterms:license",
        MAPPER.createArrayNode().add("http://creativecommons.org/licenses/by/4.0/legalcode")
            .add("http://creativecommons.org/licenses/by-nc/4.0/"));
    filters.set("$.ods:topicDiscipline",
        MAPPER.createArrayNode().add("Palaeontology"));
    filters.set("$.ods:midsLevel",
        MAPPER.createArrayNode().add(0).add(1));
    filters.set("$.occurrences.location.georeference.dwc:geodeticDatum",
        MAPPER.createArrayNode().add("WGS84").add("Another System"));
    return filters;
  }

}
