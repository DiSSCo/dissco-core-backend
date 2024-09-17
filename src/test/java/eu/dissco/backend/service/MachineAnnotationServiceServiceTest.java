package eu.dissco.backend.service;

import static eu.dissco.backend.TestUtils.HANDLE;
import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.MAS_ID;
import static eu.dissco.backend.TestUtils.ORCID;
import static eu.dissco.backend.TestUtils.givenDigitalSpecimenWrapper;
import static eu.dissco.backend.utils.DigitalMediaObjectUtils.DIGITAL_MEDIA_PATH;
import static eu.dissco.backend.utils.MachineAnnotationServiceUtils.givenFlattenedDigitalMedia;
import static eu.dissco.backend.utils.MachineAnnotationServiceUtils.givenFlattenedDigitalSpecimen;
import static eu.dissco.backend.utils.MachineAnnotationServiceUtils.givenMas;
import static eu.dissco.backend.utils.MachineAnnotationServiceUtils.givenMasJobRequest;
import static eu.dissco.backend.utils.MachineAnnotationServiceUtils.givenMasResponse;
import static eu.dissco.backend.utils.MachineAnnotationServiceUtils.givenScheduledMasResponse;
import static eu.dissco.backend.utils.MasJobRecordUtils.JOB_ID;
import static eu.dissco.backend.utils.MasJobRecordUtils.givenMasJobRecord;
import static eu.dissco.backend.utils.MasJobRecordUtils.givenMasJobRecordIdMap;
import static eu.dissco.backend.utils.SpecimenUtils.SPECIMEN_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.dissco.backend.database.jooq.enums.MjrTargetType;
import eu.dissco.backend.domain.MasTarget;
import eu.dissco.backend.domain.jsonapi.JsonApiLinksFull;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiMeta;
import eu.dissco.backend.exceptions.BatchingNotPermittedException;
import eu.dissco.backend.exceptions.ConflictException;
import eu.dissco.backend.repository.MachineAnnotationServiceRepository;
import eu.dissco.backend.schema.OdsTargetDigitalObjectFilter;
import eu.dissco.backend.utils.MachineAnnotationServiceUtils;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
            Pair.of("$['ods:type']", List.of("Some Test value")))),
        Arguments.of(List.of(
            Pair.of("$['ods:format']", List.of("application/json")),
            Pair.of("$['digitalSpecimen']['ods:hasEvent'][*]['ods:Location']['dwc:country']",
                List.of("The Netherlands", "Belgium")))),
        Arguments.of(List.of(
            Pair.of("$['ods:format']", List.of("application/json")),
            Pair.of("$['digitalSpecimen']['ods:hasEvent'][*]['dwc:city']",
                List.of("Rotterdam", "Amsterdam")))),
        Arguments.of(List.of(Pair.of("$['omg:someRandomNonExistingKey']", List.of("whyKnows"))))
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
    var masRecord = givenMas(givenFiltersDigitalMedia(false));
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
    var masRecord = givenMas(givenFiltersDigitalMedia(filters));
    given(repository.getAllMas()).willReturn(List.of(masRecord));

    // When
    var result = service.getMassForObject(givenFlattenedDigitalMedia(), DIGITAL_MEDIA_PATH);

    // Then
    assertThat(result.getData()).isEmpty();
  }

  @Test
  void testScheduleMass() throws JsonProcessingException, ConflictException {
    // Given
    var digitalSpecimen = givenDigitalSpecimenWrapper(HANDLE + ID);
    var masRecord = MachineAnnotationServiceUtils.givenMas(givenFiltersDigitalSpecimen(), true);
    given(repository.getMasRecords(Set.of(HANDLE + ID))).willReturn(List.of(masRecord));
    given(masJobRecordService.createMasJobRecord(Set.of(masRecord), HANDLE + ID, ORCID,
        MjrTargetType.DIGITAL_SPECIMEN,
        Map.of(HANDLE + ID, givenMasJobRequest(true, null)))).willReturn(
        givenMasJobRecordIdMap(masRecord.getId(), true));
    var sendObject = new MasTarget(digitalSpecimen, JOB_ID, true);

    // When
    var result = service.scheduleMass(givenFlattenedDigitalSpecimen(),
        Map.of(HANDLE + ID, givenMasJobRequest(true, null)),
        SPECIMEN_PATH, digitalSpecimen, digitalSpecimen.getOdsID(), ORCID,
        MjrTargetType.DIGITAL_SPECIMEN);

    // Then
    assertThat(result).isEqualTo(
        givenScheduledMasResponse(givenMasJobRecord(true), SPECIMEN_PATH));
    then(kafkaPublisherService).should().sendObjectToQueue("fancy-topic-name", sendObject);
  }

  @Test
  void testScheduleMassEmpty() throws JsonProcessingException, ConflictException {
    // Given
    var digitalSpecimen = givenDigitalSpecimenWrapper(ID);
    var masRecord = givenMas(givenFiltersDigitalMedia(true));
    given(repository.getMasRecords(Set.of(MAS_ID))).willReturn(List.of(masRecord));
    var expected = new JsonApiListResponseWrapper(
        Collections.emptyList(),
        new JsonApiLinksFull(SPECIMEN_PATH),
        new JsonApiMeta(0)
    );

    // When
    var result = service.scheduleMass(givenFlattenedDigitalSpecimen(),
        Map.of(MAS_ID, givenMasJobRequest()), SPECIMEN_PATH,
        digitalSpecimen, digitalSpecimen.getOdsID(), ORCID,
        MjrTargetType.DIGITAL_SPECIMEN);

    // Then
    assertThat(result).isEqualTo(expected);
    then(masJobRecordService).shouldHaveNoInteractions();
    then(kafkaPublisherService).shouldHaveNoInteractions();
  }

  @Test
  void testScheduleMassInvalidBatchRequest() {
    // Given
    var digitalSpecimen = givenDigitalSpecimenWrapper(ID);
    var masRecord = givenMas(givenFiltersDigitalSpecimen());
    given(repository.getMasRecords(Set.of(HANDLE + ID))).willReturn(List.of(masRecord));

    // Then
    assertThrowsExactly(BatchingNotPermittedException.class,
        () -> service.scheduleMass(givenFlattenedDigitalSpecimen(),
            Map.of(HANDLE + ID, givenMasJobRequest(true, null)), SPECIMEN_PATH,
            digitalSpecimen, digitalSpecimen.getOdsID(), ORCID,
            MjrTargetType.DIGITAL_SPECIMEN));
  }

  @Test
  void testScheduleMassKafkaFailed() throws JsonProcessingException, ConflictException {
    // Given
    var digitalSpecimenWrapper = givenDigitalSpecimenWrapper(ID);
    var masRecord = givenMas(givenFiltersDigitalSpecimen());
    given(repository.getMasRecords(Set.of(HANDLE + ID))).willReturn(List.of(masRecord));
    given(masJobRecordService.createMasJobRecord(Set.of(masRecord), ID, ORCID,
        MjrTargetType.DIGITAL_SPECIMEN, Map.of(HANDLE + ID, givenMasJobRequest()))).willReturn(
        givenMasJobRecordIdMap(masRecord.getId()));
    var sendObject = new MasTarget(digitalSpecimenWrapper, JOB_ID, false);
    willThrow(JsonProcessingException.class).given(kafkaPublisherService)
        .sendObjectToQueue("fancy-topic-name", sendObject);

    // When
    var result = service.scheduleMass(givenFlattenedDigitalSpecimen(),
        Map.of(HANDLE + ID, givenMasJobRequest()), SPECIMEN_PATH,
        digitalSpecimenWrapper, digitalSpecimenWrapper.getOdsID(), ORCID,
        MjrTargetType.DIGITAL_SPECIMEN);

    // Then
    then(masJobRecordService).should().markMasJobRecordAsFailed(List.of(JOB_ID));
    assertThat(result.getData()).isEmpty();
  }

  private OdsTargetDigitalObjectFilter givenFiltersDigitalMedia(
      List<Pair<String, List<String>>> filters) {
    var targetFilter = new OdsTargetDigitalObjectFilter();
    for (var filter : filters) {
      targetFilter.setAdditionalProperty(filter.getLeft(), filter.getRight());
    }
    return targetFilter;
  }

  private OdsTargetDigitalObjectFilter givenFiltersDigitalMedia(boolean unmatchedFilter) {
    var filters = new OdsTargetDigitalObjectFilter()
        .withAdditionalProperty("$['ods:type']",
            List.of("https://doi.org/21.T11148/bbad8c4e101e8af01115"))
        .withAdditionalProperty("$['dwc:organisationName']",
            List.of("Royal Botanic Garden Edinburgh Herbarium"))
        .withAdditionalProperty("$['digitalSpecimen']['ods:hasEvent'][*]['ods:Location']['dwc:country']",
            List.of("*"));
    if (unmatchedFilter) {
      filters.withAdditionalProperty("$['digitalSpecimen']['ods:hasEvent'][*]['ods:Location']['dwc:island']",
          List.of("*"));
    }
    return filters;
  }

  private OdsTargetDigitalObjectFilter givenFiltersDigitalSpecimen() {
    return new OdsTargetDigitalObjectFilter()
        .withAdditionalProperty("$['dcterms:license']",
            List.of("http://creativecommons.org/licenses/by/4.0/legalcode",
                "http://creativecommons.org/licenses/by-nc/4.0/"))
        .withAdditionalProperty("$['ods:topicDiscipline']", List.of("Palaeontology"))
        .withAdditionalProperty("$['ods:midsLevel']", List.of(0, 1));
  }

}
