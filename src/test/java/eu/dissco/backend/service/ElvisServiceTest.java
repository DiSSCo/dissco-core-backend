package eu.dissco.backend.service;

import static eu.dissco.backend.TestUtils.DOI;
import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.PHYSICAL_ID;
import static eu.dissco.backend.TestUtils.givenDigitalSpecimenWrapper;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import eu.dissco.backend.domain.elvis.ElvisSpecimen;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.repository.DigitalSpecimenRepository;
import eu.dissco.backend.repository.ElasticSearchRepository;
import eu.dissco.backend.schema.DigitalSpecimen;
import eu.dissco.backend.schema.Identification;
import eu.dissco.backend.schema.TaxonIdentification;
import java.io.IOException;
import java.util.List;
import java.util.Map;
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

  private ElvisService elvisService;

  @BeforeEach
  void setup() {
    elvisService = new ElvisService(repository, elasticSearchRepository);
  }

  @ParameterizedTest
  @MethodSource("specimenWithTaxonomy")
  void testSearchByDoi(DigitalSpecimen specimen, ElvisSpecimen expected) throws NotFoundException {
    // Given
    given(repository.getLatestSpecimenById(ID)).willReturn(specimen);

    // When
    var result = elvisService.searchByDoi(ID);

    // Then
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void testSearchByDoiFull() throws Exception {
    // Given
    var term = "term";
    var specimen = givenDigitalSpecimenWrapper(ID)
        .withDwcCollectionCode(term)
        .withOdsOrganisationCode(term)
        .withDwcBasisOfRecord(term)
        .withOdsSpecimenName(null);
    var title = "global_id_123123, Royal Botanic Garden Edinburgh Herbarium";
    var expected = new ElvisSpecimen(
        ID, title, term, PHYSICAL_ID, term, term, ID, "", "", "", "", "", ""
    );
    given(repository.getLatestSpecimenById(ID)).willReturn(specimen);

    // When
    var result = elvisService.searchByDoi(ID);

    // Then
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void testSearchByDoiNotFound() {
    // Given
    given(repository.getLatestSpecimenById(ID)).willReturn(null);

    // When / then
    assertThrows(NotFoundException.class, () -> elvisService.searchByDoi(ID));
  }

  @Test
  void testSuggestInventoryNumberNotFound() throws IOException {
    // Given
    given(elasticSearchRepository.elvisSearch(anyMap(), anyInt(), anyInt())).willReturn(Pair.of(0L, List.of()));

    // When / then
    assertThrows(NotFoundException.class, () -> elvisService.suggestInventoryNumber(PHYSICAL_ID, 1, 1));
  }

  @Test
  void testSuggestInventoryNumbers() throws Exception {
    // Given
    var expected = MAPPER.readTree(""" 
        {
          "total": 1,
          "inventoryNumbers": [
          {
            "catalogNumber":"global_id_123123",
            "inventoryNumber":"20.5000.1025/ABC-123-XYZ"
          }
          ]
        }
        """
    );
    var paramMap = Map.of(
        "ods:physicalSpecimenID.keyword", List.of("*" + PHYSICAL_ID + "*"),
        "dcterms:identifier.keyword", List.of(DOI + PHYSICAL_ID + "*"));

    given(elasticSearchRepository.elvisSearch(eq(paramMap), anyInt(), anyInt())).willReturn(
        Pair.of(1L, List.of(givenDigitalSpecimenWrapper(ID))));

    // When
    var result = elvisService.suggestInventoryNumber(PHYSICAL_ID, 1, 1);

    // Then
    assertThat(MAPPER.writeValueAsString(result)).isEqualTo(MAPPER.writeValueAsString(expected));

  }

  private static Stream<Arguments> specimenWithTaxonomy() {
    return Stream.of(
        Arguments.of(
            givenDigitalSpecimenWrapper(ID)
                .withOdsHasIdentifications(List.of(
                    new Identification()
                        .withOdsIsVerifiedIdentification(true)
                        .withOdsHasTaxonIdentifications(List.of(new TaxonIdentification()
                            .withDwcFamily("family"))))), givenElvisSpecimen(1)
        ),
        Arguments.of(
            givenDigitalSpecimenWrapper(ID)
                .withOdsHasIdentifications(List.of(
                    new Identification()
                        .withOdsIsVerifiedIdentification(false)
                        .withOdsHasTaxonIdentifications(List.of(new TaxonIdentification()
                            .withDwcFamily("family"))))), givenElvisSpecimen(1)
        ),
        Arguments.of(
            givenDigitalSpecimenWrapper(ID)
                .withOdsHasIdentifications(List.of(
                    new Identification()
                        .withOdsIsVerifiedIdentification(false)
                        .withOdsHasTaxonIdentifications(List.of()))), givenElvisSpecimen(0)
        ),
        Arguments.of(
            givenDigitalSpecimenWrapper(ID)
                .withOdsHasIdentifications(List.of(
                    new Identification()
                        .withOdsIsVerifiedIdentification(true)
                        .withOdsHasTaxonIdentifications(List.of(
                            new TaxonIdentification()
                                .withDwcFamily("family"),
                            new TaxonIdentification()
                                .withDwcFamily("another family"),
                            new TaxonIdentification()
                                .withDwcFamily("another other family")))
                )), givenElvisSpecimen(3)),
        Arguments.of(
            givenDigitalSpecimenWrapper(ID)
                .withOdsHasIdentifications(List.of(
                    new Identification()
                        .withOdsHasTaxonIdentifications(List.of()))), givenElvisSpecimen(0)),
        Arguments.of(givenDigitalSpecimenWrapper(ID)
                .withDwcCollectionCode("term")
                .withOdsOrganisationCode("term")
                .withDwcBasisOfRecord("term")
                .withOdsSpecimenName(null),
            new ElvisSpecimen(
                ID, "global_id_123123, Royal Botanic Garden Edinburgh Herbarium", "term",
                PHYSICAL_ID, "term", "term", ID, "", "", "", "", "", ""
            ))
    );
  }

  private static ElvisSpecimen givenElvisSpecimen(Integer taxonomies) {
    var title = "Abyssothyris Thomson, 1927 global_id_123123, Royal Botanic Garden Edinburgh Herbarium";
    if (taxonomies == 0) {
      return new ElvisSpecimen(
          ID, title, "", PHYSICAL_ID, "",
          "", ID, "", "", "", "", "", ""
      );
    } else if (taxonomies == 1) {
      return new ElvisSpecimen(
          ID, title, "", PHYSICAL_ID, "",
          "", ID, "", "", "", "family", "", ""
      );

    } else {
      var missing = ", , and 1 more";
      return new ElvisSpecimen(
          ID, title, "", PHYSICAL_ID, "", "", ID, missing, missing, missing,
          "family, another family, and 1 more", missing, missing);
    }
  }
}
