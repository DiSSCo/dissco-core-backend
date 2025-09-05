package eu.dissco.backend.service;

import static eu.dissco.backend.TestUtils.DOI;
import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.MAS_ID;
import static eu.dissco.backend.TestUtils.ORCID;
import static eu.dissco.backend.TestUtils.SANDBOX_URI;
import static eu.dissco.backend.utils.DigitalMediaObjectUtils.DIGITAL_MEDIA_PATH;
import static eu.dissco.backend.utils.MachineAnnotationServiceUtils.givenFlattenedDigitalMedia;
import static eu.dissco.backend.utils.MachineAnnotationServiceUtils.givenMas;
import static eu.dissco.backend.utils.MachineAnnotationServiceUtils.givenMasJobRequest;
import static eu.dissco.backend.utils.MachineAnnotationServiceUtils.givenMasResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.dissco.backend.client.MasClient;
import eu.dissco.backend.database.jooq.enums.MjrTargetType;
import eu.dissco.backend.domain.MasScheduleJobRequest;
import eu.dissco.backend.exceptions.MasSchedulingException;
import eu.dissco.backend.repository.MachineAnnotationServiceRepository;
import eu.dissco.backend.schema.OdsHasTargetDigitalObjectFilter;
import feign.FeignException;
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
  private MasClient masClient;

  private MachineAnnotationServiceService service;

  private static Stream<Arguments> provideFilters() {
    return Stream.of(
        Arguments.of(List.of(
            Pair.of("$['ods:fdoType']", List.of("Some Test value")))),
        Arguments.of(List.of(
            Pair.of("$['ods:format']", List.of("application/json")),
            Pair.of("$['digitalSpecimen']['ods:hasEvents'][*]['ods:hasLocation']['dwc:country']",
                List.of("The Netherlands", "Belgium")))),
        Arguments.of(List.of(
            Pair.of("$['ods:format']", List.of("application/json")),
            Pair.of("$['digitalSpecimen']['ods:hasEvents'][*]['dwc:city']",
                List.of("Rotterdam", "Amsterdam")))),
        Arguments.of(List.of(Pair.of("$['omg:someRandomNonExistingKey']", List.of("whyKnows"))))
    );
  }

  @BeforeEach
  void setup() {
    this.service = new MachineAnnotationServiceService(repository, MAPPER, masClient);
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
  void testScheduleMass() throws Exception {
    // Given
    var expected = Set.of(new MasScheduleJobRequest(MAS_ID, DOI + ID, false, ORCID,
        MjrTargetType.DIGITAL_SPECIMEN));
    given(masClient.scheduleMas(expected)).willReturn(
        MAPPER.readTree("""
            [
              {
                "jobId" : "something"
              }
            ]
            """));

    // When
    service.scheduleMas(ID, List.of(givenMasJobRequest()), ORCID, MjrTargetType.DIGITAL_SPECIMEN,
        SANDBOX_URI);

    // Then
    then(masClient).should().scheduleMas(expected);
  }

  @Test
  void testScheduleMasFailed() {
    // Given
    doThrow(FeignException.class).when(masClient).scheduleMas(any());

    // When / Then
    assertThrows(
        MasSchedulingException.class,
        () -> service.scheduleMas(ID, List.of(givenMasJobRequest()), ORCID,
            MjrTargetType.DIGITAL_SPECIMEN, SANDBOX_URI));

  }

  private OdsHasTargetDigitalObjectFilter givenFiltersDigitalMedia(
      List<Pair<String, List<String>>> filters) {
    var targetFilter = new OdsHasTargetDigitalObjectFilter();
    for (var filter : filters) {
      targetFilter.setAdditionalProperty(filter.getLeft(), filter.getRight());
    }
    return targetFilter;
  }

  private OdsHasTargetDigitalObjectFilter givenFiltersDigitalMedia(boolean unmatchedFilter) {
    var filters = new OdsHasTargetDigitalObjectFilter()
        .withAdditionalProperty("$['ods:fdoType']",
            List.of("https://doi.org/21.T11148/bbad8c4e101e8af01115"))
        .withAdditionalProperty("$['dwc:organisationName']",
            List.of("Royal Botanic Garden Edinburgh Herbarium"))
        .withAdditionalProperty(
            "$['digitalSpecimen']['ods:hasEvents'][*]['ods:hasLocation']['dwc:country']",
            List.of("*"));
    if (unmatchedFilter) {
      filters.withAdditionalProperty(
          "$['digitalSpecimen']['ods:hasEvents'][*]['ods:hasLocation']['dwc:island']",
          List.of("*"));
    }
    return filters;
  }

}
