package eu.dissco.backend.domain.elastic;

import static eu.dissco.backend.domain.elastic.TaxonMappingTerms.KINGDOM;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum DefaultMappingTerms implements MappingTerm {
  COUNTRY("country", "ods:hasEvents.ods:hasLocation.dwc:country.keyword"),
  COUNTRY_CODE("countryCode", "ods:hasEvents.ods:hasLocation.dwc:countryCode.keyword"),
  LOCALITY("locality", "ods:hasEvents.ods:hasLocation.dwc:locality.keyword"),
  LATITUDE("latitude", "ods:hasEvents.ods:hasLocation.ods:hasGeoreference.dwc:decimalLatitude"),
  LONGITUDE("longitude", "ods:hasEvents.ods:hasLocation.ods:hasGeoreference.dwc:decimalLongitude"),
  MIDS_LEVEL("midsLevel", "ods:midsLevel"),
  PHYSICAL_SPECIMEN_ID_DEPRECATED("physicalSpecimenId", "ods:physicalSpecimenID.keyword"),
  PHYSICAL_SPECIMEN_ID("physicalSpecimenID", "ods:physicalSpecimenID.keyword"),
  IDENTIFIER("identifier", "dcterms:identifier.keyword"),
  TYPE_STATUS("typeStatus", "ods:hasIdentifications.dwc:typeStatus.keyword"),
  LICENSE("license", "dcterms:license.keyword"),
  HAS_MEDIA("hasMedia", "ods:isKnownToContainMedia"),
  ORGANISATION_ID("organisationID", "ods:organisationID.keyword"),
  ORGANISATION_NAME("organisationName", "ods:organisationName.keyword"),
  SOURCE_SYSTEM_ID("sourceSystemID", "ods:sourceSystemID.keyword"),
  SOURCE_SYSTEM_NAME("sourceSystemName", "ods:sourceSystemName.keyword"),
  SPECIMEN_NAME("specimenName", "ods:specimenName.keyword"),
  DATASET_NAME("datasetName", "dwc:datasetName.keyword"),
  COLLECTION_CODE("collectionCode", "dwc:collectionCode.keyword"),
  COLLECTION_ID("collectionID", "dwc:collectionID.keyword"),
  IDENTIFIED_BY("identifiedBy", "ods:hasIdentifications.ods:hasAgents.schema:name.keyword"),
  BASIS_OF_RECORD("basisOfRecord", "dwc:basisOfRecord.keyword"),
  LIVING_OR_PRESERVED("livingOrPreserved", "ods:livingOrPreserved"),
  TOPIC_DISCIPLINE("topicDiscipline", "ods:topicDiscipline"),
  HABITAT("habitat", "ods:hasEvents.dwc:habitat.keyword"),
  VIRTUAL_COLLECTION_ID("virtualCollectionID", "ods:hasEntityRelationships.ods:relatedResourceURI.keyword"),
  QUERY("q", "q");


  private static final Set<MappingTerm> aggregationSet = fillAggregationList();

  private static final Map<String, MappingTerm> paramMapping = fillParamMapping();
  private final String requestName;
  private final String fullName;

  DefaultMappingTerms(String requestName, String fullName) {
    this.requestName = requestName;
    this.fullName = fullName;
  }

  public static Set<MappingTerm> getAggregationSet() {
    return aggregationSet;
  }

  public static Map<String, MappingTerm> getParamMapping() {
    return paramMapping;
  }

  private static Set<MappingTerm> fillAggregationList() {
    var aggregationTerms = new HashSet<MappingTerm>();
    aggregationTerms.add(COUNTRY);
    aggregationTerms.add(TYPE_STATUS);
    aggregationTerms.add(LICENSE);
    aggregationTerms.add(ORGANISATION_NAME);
    aggregationTerms.add(ORGANISATION_ID);
    aggregationTerms.add(SOURCE_SYSTEM_NAME);
    aggregationTerms.add(DATASET_NAME);
    aggregationTerms.add(KINGDOM);
    aggregationTerms.add(TOPIC_DISCIPLINE);
    aggregationTerms.add(COLLECTION_CODE);
    aggregationTerms.add(COLLECTION_ID);
    return aggregationTerms;
  }

  private static Map<String, MappingTerm> fillParamMapping() {
    var paramMap = new HashMap<String, MappingTerm>();
    paramMap.put(COUNTRY.requestName, COUNTRY);
    paramMap.put(COUNTRY_CODE.requestName, COUNTRY_CODE);
    paramMap.put(LOCALITY.requestName, LOCALITY);
    paramMap.put(LATITUDE.requestName, LATITUDE);
    paramMap.put(LONGITUDE.requestName, LONGITUDE);
    paramMap.put(MIDS_LEVEL.requestName, MIDS_LEVEL);
    paramMap.put(PHYSICAL_SPECIMEN_ID_DEPRECATED.requestName, PHYSICAL_SPECIMEN_ID_DEPRECATED);
    paramMap.put(PHYSICAL_SPECIMEN_ID.requestName, PHYSICAL_SPECIMEN_ID);
    paramMap.put(TYPE_STATUS.requestName, TYPE_STATUS);
    paramMap.put(LICENSE.requestName, LICENSE);
    paramMap.put(HAS_MEDIA.requestName, HAS_MEDIA);
    paramMap.put(ORGANISATION_ID.requestName, ORGANISATION_ID);
    paramMap.put(ORGANISATION_NAME.requestName, ORGANISATION_NAME);
    paramMap.put(SOURCE_SYSTEM_ID.requestName, SOURCE_SYSTEM_ID);
    paramMap.put(SOURCE_SYSTEM_NAME.requestName, SOURCE_SYSTEM_NAME);
    paramMap.put(SPECIMEN_NAME.requestName, SPECIMEN_NAME);
    paramMap.put(DATASET_NAME.requestName, DATASET_NAME);
    paramMap.put(BASIS_OF_RECORD.requestName, BASIS_OF_RECORD);
    paramMap.put(LIVING_OR_PRESERVED.requestName, LIVING_OR_PRESERVED);
    paramMap.put(TOPIC_DISCIPLINE.requestName, TOPIC_DISCIPLINE);
    paramMap.put(COLLECTION_CODE.requestName, COLLECTION_CODE);
    paramMap.put(IDENTIFIED_BY.requestName, IDENTIFIED_BY);
    paramMap.put(COLLECTION_ID.requestName, COLLECTION_ID);
    paramMap.put(HABITAT.requestName, HABITAT);
    paramMap.put(VIRTUAL_COLLECTION_ID.requestName, VIRTUAL_COLLECTION_ID);
    paramMap.put(QUERY.requestName, QUERY);
    paramMap.putAll(TaxonMappingTerms.getTaxonMapping());
    paramMap.putAll(MissingMappingTerms.getMissingMapping());
    return paramMap;
  }

  @Override
  public String requestName() {
    return requestName;
  }

  @Override
  public String fullName() {
    return fullName;
  }
}
