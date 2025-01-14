package eu.dissco.backend.service;

import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.PHYSICAL_ID;
import static eu.dissco.backend.TestUtils.SANDBOX_URI;
import static eu.dissco.backend.TestUtils.SPECIMEN_NAME;
import static eu.dissco.backend.TestUtils.givenDigitalSpecimenWrapper;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.BDDMockito.given;

import eu.dissco.backend.domain.elvis.ElvisSpecimen;
import eu.dissco.backend.properties.ApplicationProperties;
import eu.dissco.backend.repository.DigitalSpecimenRepository;
import eu.dissco.backend.repository.ElasticSearchRepository;
import eu.dissco.backend.schema.DigitalSpecimen;
import eu.dissco.backend.schema.Identification;
import eu.dissco.backend.schema.TaxonIdentification;
import java.util.List;
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
class ElvisServiceTest {

  @Mock
  private DigitalSpecimenRepository repository;
  @Mock
  private ElasticSearchRepository elasticSearchRepository;
  @Mock
  private ApplicationProperties applicationProperties;

  private ElvisService elvisService;

  @BeforeEach
  void setup() {
    elvisService = new ElvisService(repository, elasticSearchRepository, applicationProperties,
        MAPPER);
  }

  @Test
  void testSearchByDoi() {
    // Given
    given(repository.getLatestSpecimenById(ID)).willReturn(givenDigitalSpecimenWrapper(ID));
    given(applicationProperties.getBaseUrl()).willReturn(SANDBOX_URI);
    var expected = givenElvisSpecimen(false);

    // When
    var result = elvisService.searchByDoi(ID);

    // Then
    assertThat(result).isEqualTo(expected);
  }

  @ParameterizedTest
  @MethodSource("specimenWithTaxonomy")
  void searchBySpecimenId(DigitalSpecimen specimen, boolean hasTaxonomy) throws Exception {
    // Given
    given(elasticSearchRepository.search(anyMap(), anyInt(), anyInt())).willReturn(Pair.of(1L, List.of(specimen)));
    given(applicationProperties.getBaseUrl()).willReturn(SANDBOX_URI);
    var elvisSpecimens = List.of(givenElvisSpecimen(hasTaxonomy));
    var expected = MAPPER.createObjectNode()
        .put("total", 1)
        .set("specimens", MAPPER.valueToTree(elvisSpecimens));

    // When
    var result = elvisService.searchBySpecimenId(PHYSICAL_ID, 1, 1);

    // Then
    assertThat(MAPPER.writeValueAsString(result)).isEqualTo(MAPPER.writeValueAsString(expected));
  }

  @Test
  void testSuggestInventoryNumbers() throws Exception {
    // Given
    var expected = MAPPER.readTree(""" 
        {
          "total": 1,
          "inventoryNumbers": [
          {
            "catalogNumber": null,
            "inventoryNumber":"global_id_123123",
            "identifier":"20.5000.1025/ABC-123-XYZ"
          }
          ]
        }
        """
    );
    given(elasticSearchRepository.search(anyMap(), anyInt(), anyInt())).willReturn(Pair.of(1L, List.of(givenDigitalSpecimenWrapper(ID))));

    // When
    var result = elvisService.suggestInventoryNumber(PHYSICAL_ID, 1, 1);

    // Then
    assertThat(MAPPER.writeValueAsString(result)).isEqualTo(MAPPER.writeValueAsString(expected));

  }

  private static Stream<Arguments> specimenWithTaxonomy(){
    return Stream.of(
        Arguments.of(
            givenDigitalSpecimenWrapper(ID)
                .withOdsHasIdentifications(List.of(
                    new Identification()
                        .withOdsIsVerifiedIdentification(true)
                        .withOdsHasTaxonIdentifications(List.of(new TaxonIdentification()
                            .withDwcFamily("family"))))), true
        ),
        Arguments.of(
            givenDigitalSpecimenWrapper(ID)
                .withOdsHasIdentifications(List.of(
                    new Identification()
                        .withOdsIsVerifiedIdentification(false)
                        .withOdsHasTaxonIdentifications(List.of(new TaxonIdentification()
                            .withDwcFamily("family"))))), true
        ),
        Arguments.of(
            givenDigitalSpecimenWrapper(ID)
                .withOdsHasIdentifications(List.of(
                    new Identification()
                        .withOdsHasTaxonIdentifications(List.of()))), false)

    );
  }

  private static ElvisSpecimen givenElvisSpecimen(boolean hasTaxonomy) {
    if (hasTaxonomy) {
      return new ElvisSpecimen(
          PHYSICAL_ID, SPECIMEN_NAME, ID, null, null, "https://ror.org/0349vqz63", null,
          null, SANDBOX_URI + "/ds/" + ID, null, null, null, "family", null, null
      );
    }
    return new ElvisSpecimen(
        PHYSICAL_ID, SPECIMEN_NAME, ID, null, null, "https://ror.org/0349vqz63", null,
        null, SANDBOX_URI + "/ds/" + ID, null, null, null, null, null, null);
  }


}
