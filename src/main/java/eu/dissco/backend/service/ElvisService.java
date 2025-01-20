package eu.dissco.backend.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.domain.elvis.ElvisSpecimen;
import eu.dissco.backend.domain.elvis.InventoryNumberSuggestion;
import eu.dissco.backend.domain.elvis.InventoryNumberSuggestionResponse;
import eu.dissco.backend.properties.ApplicationProperties;
import eu.dissco.backend.repository.DigitalSpecimenRepository;
import eu.dissco.backend.repository.ElasticSearchRepository;
import eu.dissco.backend.schema.DigitalSpecimen;
import eu.dissco.backend.schema.TaxonIdentification;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ElvisService {

  private final DigitalSpecimenRepository repository;
  private final ElasticSearchRepository elasticSearchRepository;
  private final ApplicationProperties applicationProperties;
  private final ObjectMapper mapper;

  public ElvisSpecimen searchByDoi(String id) {
    var specimen = repository.getLatestSpecimenById(id);
    return buildElvisSpecimen(specimen, applicationProperties.getBaseUrl());
  }

  public JsonNode searchBySpecimenId(String inventoryNumber, int pageNumber, int pageSize)
      throws IOException {
    var results = searchElastic(inventoryNumber, pageNumber, pageSize);
    var elvisSpecimens = results.getRight().stream()
        .map(specimen -> buildElvisSpecimen(specimen, applicationProperties.getBaseUrl())).toList();
    return mapper.createObjectNode()
        .put("total", results.getLeft())
        .set("specimens", mapper.valueToTree(elvisSpecimens));
  }

  public InventoryNumberSuggestionResponse suggestInventoryNumber(String inventoryNumber, int pageNumber, int pageSize)
      throws IOException {
    var results = searchElastic(inventoryNumber, pageNumber, pageSize);
    var specimenList = results.getRight();
    return new InventoryNumberSuggestionResponse(results.getLeft(), specimenList.stream().map(
        specimen -> new InventoryNumberSuggestion(specimen.getDwcCollectionID(),
            specimen.getOdsPhysicalSpecimenID(), specimen.getDctermsIdentifier())).toList());

  }

  private Pair<Long, List<DigitalSpecimen>> searchElastic(String inventoryNumber, int pageNumber,
      int pageSize)
      throws IOException {
    var map = Map.of("ods:physicalSpecimenID.keyword", List.of(inventoryNumber));
    return elasticSearchRepository.search(map, pageNumber, pageSize);
  }


  private static ElvisSpecimen buildElvisSpecimen(DigitalSpecimen digitalSpecimen, String url) {
    var inventoryNumber = digitalSpecimen.getOdsPhysicalSpecimenID();
    var identifier = digitalSpecimen.getDctermsIdentifier();
    var collectionCode = digitalSpecimen.getDwcCollectionCode();
    var catalogNumber = digitalSpecimen.getDwcCollectionID();
    var institutionId = digitalSpecimen.getOdsOrganisationID();
    var institutionCode = digitalSpecimen.getOdsOrganisationCode();
    var basisOfRecord = digitalSpecimen.getDwcBasisOfRecord();
    var uri = url + "/ds/" + digitalSpecimen.getId();
    var taxonId = getBestTaxonIdentification(digitalSpecimen);
    String scientificName = null;
    String scientificNameAuthorship = null;
    String specificEpithet = null;
    String family = null;
    String genus = null;
    String vernacularName = null;
    if (taxonId.isPresent()) {
      scientificName = taxonId.get().getDwcScientificName();
      scientificNameAuthorship = taxonId.get().getDwcScientificNameAuthorship();
      specificEpithet = taxonId.get().getDwcSpecificEpithet();
      family = taxonId.get().getDwcFamily();
      genus = taxonId.get().getDwcVernacularName();
      vernacularName = taxonId.get().getDwcVernacularName();
    }
    var title = buildElvisTitle(digitalSpecimen, taxonId);
    return new ElvisSpecimen(inventoryNumber, title, identifier, collectionCode, catalogNumber,
        institutionId,
        institutionCode, basisOfRecord, uri, scientificName, scientificNameAuthorship,
        specificEpithet, family, genus, vernacularName);
  }

  private static String buildElvisTitle(DigitalSpecimen digitalSpecimen,
      Optional<TaxonIdentification> taxonId) {
    String scientificName = "";
    if (taxonId.isPresent()) {
      scientificName = taxonId.get().getDwcScientificName();
    }
    // Todo - how to return blanks?
    return scientificName + " " + digitalSpecimen.getOdsOrganisationCode() + "-"
        + digitalSpecimen.getDwcCollectionCode() + "-" + digitalSpecimen.getDwcCollectionID();
  }

  private static Optional<TaxonIdentification> getBestTaxonIdentification(
      DigitalSpecimen digitalSpecimen) {
    if (digitalSpecimen.getOdsHasIdentifications().isEmpty()
        || digitalSpecimen.getOdsHasIdentifications().get(0).getOdsHasTaxonIdentifications()
        .isEmpty()) {
      return Optional.empty();
    }
    // First, try to find accepted id
    for (var identification : digitalSpecimen.getOdsHasIdentifications()) {
      if (Boolean.TRUE.equals(identification.getOdsIsVerifiedIdentification())) {
        if (identification.getOdsHasTaxonIdentifications().isEmpty()) {
          return Optional.empty();
        } else {
          return Optional.of(identification.getOdsHasTaxonIdentifications().get(0));
        }
      }
    }
    // If no accepted id, take the first
    return Optional.of(
        digitalSpecimen.getOdsHasIdentifications().get(0).getOdsHasTaxonIdentifications().get(0));
  }


}
