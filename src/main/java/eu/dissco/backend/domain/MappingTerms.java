package eu.dissco.backend.domain;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public enum MappingTerms {
  TYPE("type", "digitalSpecimen.ods:type.keyword"),
  COUNTRY("country", "digitalSpecimen.ods:attributes.dwc:country.keyword"),
  COUNTRY_CODE("countryCode", "digitalSpecimen.ods:attributes.dwc:countryCode.keyword"),
  MIDS_LEVEL("midsLevel", "midsLevel"),
  PHYSICAL_SPECIMEN_ID("physicalSpecimenId", "digitalSpecimen.ods:physicalSpecimenId.keyword"),
  TYPE_STATUS("typeStatus", "digitalSpecimen.ods:attributes.dwc:typeStatus.keyword"),
  LICENSE("license", "digitalSpecimen.ods:attributes.dcterms:license.keyword"),
  HAS_MEDIA("hasMedia", "digitalSpecimen.ods:attributes.ods:hasMedia.keyword"),
  ORGANISATION_ID("organisationId", "digitalSpecimen.ods:attributes.ods:organisationId.keyword"),
  ORGANISATION_NAME("organisationName",
      "digitalSpecimen.ods:attributes.ods:organisationName.keyword"),
  SOURCE_SYSTEM_ID("sourceSystemId", "digitalSpecimen.ods:attributes.ods:sourceSystemId.keyword"),
  SPECIMEN_NAME("type", "digitalSpecimen.ods:type.keyword"),
  DATASET_ID("datasetId", "digitalSpecimen.ods:attributes.ods:datasetId.keyword"),
  KINGDOM("kingdom", "digitalSpecimen.ods:attributes.dwc:kingdom.keyword"),
  CLASS("class", "digitalSpecimen.ods:attributes.dwc:class.keyword"),
  FAMILY("family", "digitalSpecimen.ods:attributes.dwc:family.keyword"),
  GENUS("genus", "digitalSpecimen.ods:attributes.dwc:genus.keyword"),
  ORDER("order", "digitalSpecimen.ods:attributes.dwc:order.keyword"),
  PHYLUM("phylum", "digitalSpecimen.ods:attributes.dwc:phylum.keyword"),
  BASIS_OF_RECORD("basisOfRecord", "digitalSpecimen.ods:attributes.dwc:basisOfRecord.keyword"),
  LIVING_OR_PRESERVED("livingOrPreserved",
      "digitalSpecimen.ods:attributes.ods:livingOrPreserved.keyword"),
  TOPIC_DISCIPLINE("topicDiscipline", "digitalSpecimen.ods:attributes.ods:topicDiscipline.keyword"),
  QUERY("q", "q");

  public static final Set<MappingTerms> aggregationList = getAggregationList();
  private static final Map<String, String> paramMapping = getParamMapping();
  private final String name;
  private final String fullName;

  MappingTerms(String name, String fullName) {
    this.name = name;
    this.fullName = fullName;
  }

  public static Optional<String> getMappedTerm(String name) {
    return Optional.ofNullable(paramMapping.get(name));
  }

  private static Set<MappingTerms> getAggregationList() {
    var aggregationTerms = EnumSet.noneOf(MappingTerms.class);
    aggregationTerms.add(TYPE);
    aggregationTerms.add(COUNTRY);
    aggregationTerms.add(MIDS_LEVEL);
    aggregationTerms.add(TYPE_STATUS);
    aggregationTerms.add(LICENSE);
    aggregationTerms.add(HAS_MEDIA);
    aggregationTerms.add(ORGANISATION_NAME);
    aggregationTerms.add(ORGANISATION_ID);
    aggregationTerms.add(SOURCE_SYSTEM_ID);
    aggregationTerms.add(DATASET_ID);
    aggregationTerms.add(KINGDOM);
    aggregationTerms.add(CLASS);
    aggregationTerms.add(FAMILY);
    aggregationTerms.add(GENUS);
    aggregationTerms.add(ORDER);
    aggregationTerms.add(PHYLUM);
    aggregationTerms.add(BASIS_OF_RECORD);
    aggregationTerms.add(LIVING_OR_PRESERVED);
    aggregationTerms.add(TOPIC_DISCIPLINE);
    return aggregationTerms;
  }

  private static Map<String, String> getParamMapping() {
    var paramMap = new HashMap<String, String>();
    paramMap.put(TYPE.name, TYPE.fullName);
    paramMap.put(COUNTRY.name, COUNTRY.fullName);
    paramMap.put(COUNTRY_CODE.name, COUNTRY_CODE.fullName);
    paramMap.put(MIDS_LEVEL.name, MIDS_LEVEL.fullName);
    paramMap.put(PHYSICAL_SPECIMEN_ID.name, PHYSICAL_SPECIMEN_ID.fullName);
    paramMap.put(TYPE_STATUS.name, TYPE_STATUS.fullName);
    paramMap.put(LICENSE.name, LICENSE.fullName);
    paramMap.put(HAS_MEDIA.name, HAS_MEDIA.fullName);
    paramMap.put(ORGANISATION_ID.name, ORGANISATION_ID.fullName);
    paramMap.put(ORGANISATION_NAME.name, ORGANISATION_NAME.fullName);
    paramMap.put(SOURCE_SYSTEM_ID.name, SOURCE_SYSTEM_ID.fullName);
    paramMap.put(SPECIMEN_NAME.name, SPECIMEN_NAME.fullName);
    paramMap.put(KINGDOM.name, KINGDOM.fullName);
    paramMap.put(CLASS.name, CLASS.fullName);
    paramMap.put(FAMILY.name, FAMILY.fullName);
    paramMap.put(GENUS.name, GENUS.fullName);
    paramMap.put(ORDER.name, ORDER.fullName);
    paramMap.put(PHYLUM.name, PHYLUM.fullName);
    paramMap.put(BASIS_OF_RECORD.name, BASIS_OF_RECORD.fullName);
    paramMap.put(LIVING_OR_PRESERVED.name, LIVING_OR_PRESERVED.fullName);
    paramMap.put(TOPIC_DISCIPLINE.name, TOPIC_DISCIPLINE.fullName);
    paramMap.put(QUERY.name, QUERY.fullName);
    return paramMap;
  }

  public String getName() {
    return name;
  }

  public String getFullName() {
    return fullName;
  }

}
