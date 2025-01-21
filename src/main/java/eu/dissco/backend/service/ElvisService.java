package eu.dissco.backend.service;


import static java.lang.Math.min;

import eu.dissco.backend.domain.elvis.ElvisSpecimen;
import eu.dissco.backend.domain.elvis.InventoryNumberSuggestion;
import eu.dissco.backend.domain.elvis.InventoryNumberSuggestionResponse;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.repository.DigitalSpecimenRepository;
import eu.dissco.backend.repository.ElasticSearchRepository;
import eu.dissco.backend.schema.DigitalSpecimen;
import eu.dissco.backend.schema.TaxonIdentification;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ElvisService {

  private final DigitalSpecimenRepository repository;
  private final ElasticSearchRepository elasticSearchRepository;

  public ElvisSpecimen searchByDoi(String id) throws NotFoundException {
    var specimen = repository.getLatestSpecimenById(id);
    if (specimen ==  null) {
      throw new NotFoundException();
    }
    return buildElvisSpecimen(specimen);
  }

  public InventoryNumberSuggestionResponse suggestInventoryNumber(String inventoryNumber,
      int pageNumber, int pageSize) throws IOException, NotFoundException {
    var results = searchElastic(inventoryNumber, pageNumber, pageSize);
    if (results.getLeft().equals(0L)){
      throw new NotFoundException();
    }
    var specimenList = results.getRight();
    return new InventoryNumberSuggestionResponse(results.getLeft(), specimenList.stream().map(
        specimen -> new InventoryNumberSuggestion(
            specimen.getOdsPhysicalSpecimenID(), specimen.getDctermsIdentifier())).toList());
  }

  private Pair<Long, List<DigitalSpecimen>> searchElastic(String inventoryNumber, int pageNumber,
      int pageSize)
      throws IOException {
    var map = Map.of(
        "ods:physicalSpecimenID.keyword", List.of("*" + inventoryNumber + "*"),
        "dcterms:identifier.keyword", List.of("https://doi.org/" + inventoryNumber + "*"));
    return elasticSearchRepository.elvisSearch(map, pageNumber, pageSize);
  }


  private static ElvisSpecimen buildElvisSpecimen(DigitalSpecimen digitalSpecimen) {
    var taxonIds = getBestTaxonIdentification(digitalSpecimen);
    String scientificName = getTaxonField(
        taxonIds.stream().map(TaxonIdentification::getDwcScientificName));
    String scientificNameAuthorship = getTaxonField(
        taxonIds.stream().map(TaxonIdentification::getDwcScientificNameAuthorship));
    String specificEpithet = getTaxonField(
        taxonIds.stream().map(TaxonIdentification::getDwcSpecificEpithet));
    String family = getTaxonField(taxonIds.stream().map(TaxonIdentification::getDwcFamily));
    String genus = getTaxonField(taxonIds.stream().map(TaxonIdentification::getDwcGenus));
    String vernacularName = getTaxonField(
        taxonIds.stream().map(TaxonIdentification::getDwcVernacularName));

    return new ElvisSpecimen(
        digitalSpecimen.getDctermsIdentifier(), // inventory number
        buildElvisTitle(digitalSpecimen), // title
        digitalSpecimen.getDwcCollectionCode() == null ? ""
            : digitalSpecimen.getDwcCollectionCode(), // collection code
        digitalSpecimen.getOdsPhysicalSpecimenID(), // catalog number
        digitalSpecimen.getOdsOrganisationCode() == null ? ""
            : digitalSpecimen.getOdsOrganisationCode(), // institution code
        digitalSpecimen.getDwcBasisOfRecord() == null ? "" : digitalSpecimen.getDwcBasisOfRecord(),
        digitalSpecimen.getDctermsIdentifier(), // uri
        scientificName, scientificNameAuthorship, specificEpithet, family, genus, vernacularName);
  }

  private static String getTaxonField(Stream<String> taxonFields) {
    var taxonList = taxonFields
        .map(a -> a == null ? "" : a)
        .toList();
    if (taxonList.isEmpty()) {
      return "";
    }
    var listAsString = String.join(", ", taxonList.subList(0, min(taxonList.size(), 2)));
    if (taxonList.size() > 2) {
      listAsString = listAsString + ", and " + (taxonList.size() - 2) + " more";
    }
    return listAsString;
  }

  private static String buildElvisTitle(DigitalSpecimen digitalSpecimen) {
    var title = new StringBuilder();
    if (digitalSpecimen.getOdsSpecimenName() != null) {
      title.append(digitalSpecimen.getOdsSpecimenName()).append(" ");
    }
    title
        .append(digitalSpecimen.getOdsPhysicalSpecimenID())
        .append(", ")
        .append(digitalSpecimen.getOdsOrganisationName());
    return title.toString();
  }

  private static List<TaxonIdentification> getBestTaxonIdentification(
      DigitalSpecimen digitalSpecimen) {
    if (digitalSpecimen.getOdsHasIdentifications().isEmpty()
        || digitalSpecimen.getOdsHasIdentifications().get(0).getOdsHasTaxonIdentifications()
        .isEmpty()) {
      return List.of();
    }
    // First, try to find accepted id
    for (var identification : digitalSpecimen.getOdsHasIdentifications()) {
      if (Boolean.TRUE.equals(identification.getOdsIsVerifiedIdentification())) {
        if (identification.getOdsHasTaxonIdentifications().isEmpty()) {
          return List.of();
        } else {
          return (identification.getOdsHasTaxonIdentifications());
        }
      }
    }
    // If no accepted id, take the first
    return digitalSpecimen.getOdsHasIdentifications().get(0).getOdsHasTaxonIdentifications();
  }


}
