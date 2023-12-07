package eu.dissco.backend.domain;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;

@Getter
public enum MappingTerms {
  TYPE("type", "digitalSpecimenWrapper.ods:type.keyword"),
  COUNTRY("country",
      "digitalSpecimenWrapper.ods:attributes.occurrences.location.dwc:country.keyword"),
  COUNTRY_CODE("countryCode",
      "digitalSpecimenWrapper.ods:attributes.occurrences.location.dwc:countryCode.keyword"),
  MIDS_LEVEL("midsLevel", "midsLevel"),
  PHYSICAL_SPECIMEN_ID("physicalSpecimenId",
      "digitalSpecimenWrapper.ods:attributes.ods:physicalSpecimenId.keyword"),
  TYPE_STATUS("typeStatus",
      "digitalSpecimenWrapper.ods:attributes.dwc:identification.dwc:typeStatus.keyword"),
  LICENSE("license", "digitalSpecimenWrapper.ods:attributes.dcterms:license.keyword"),
  HAS_MEDIA("hasMedia", "digitalSpecimenWrapper.ods:attributes.ods:hasMedia"),
  ORGANISATION_ID("organisationId",
      "digitalSpecimenWrapper.ods:attributes.dwc:institutionId.keyword"),
  ORGANISATION_NAME("organisationName",
      "digitalSpecimenWrapper.ods:attributes.dwc:institutionName.keyword"),
  SOURCE_SYSTEM_ID("sourceSystemId",
      "digitalSpecimenWrapper.ods:attributes.ods:sourceSystem.keyword"),
  SPECIMEN_NAME("specimenName", "digitalSpecimenWrapper.ods:attributes.ods:specimenName.keyword"),
  DATASET_NAME("datasetName", "digitalSpecimenWrapper.ods:attributes.dwc:datasetName.keyword"),
  KINGDOM("kingdom",
      "digitalSpecimenWrapper.ods:attributes.dwc:identification.taxonIdentifications.dwc:kingdom.keyword"),
  CLASS("class",
      "digitalSpecimenWrapper.ods:attributes.dwc:identification.taxonIdentifications.dwc:class.keyword"),
  FAMILY("family",
      "digitalSpecimenWrapper.ods:attributes.dwc:identification.taxonIdentifications.dwc:family.keyword"),
  GENUS("genus",
      "digitalSpecimenWrapper.ods:attributes.dwc:identification.taxonIdentifications.dwc:genus.keyword"),
  ORDER("order",
      "digitalSpecimenWrapper.ods:attributes.dwc:identification.taxonIdentifications.dwc:order.keyword"),
  PHYLUM("phylum",
      "digitalSpecimenWrapper.ods:attributes.dwc:identification.taxonIdentifications.dwc:phylum.keyword"),
  BASIS_OF_RECORD("basisOfRecord",
      "digitalSpecimenWrapper.ods:attributes.dwc:basisOfRecord.keyword"),
  LIVING_OR_PRESERVED("livingOrPreserved",
      "digitalSpecimenWrapper.ods:attributes.ods:livingOrPreserved.keyword"),
  TOPIC_DISCIPLINE("topicDiscipline",
      "digitalSpecimenWrapper.ods:attributes.ods:topicDiscipline.keyword"),
  QUERY("q", "q");

  private static final Set<MappingTerms> aggregationList = assembleAggregationList();
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

  public static Set<MappingTerms> getAggregationList() {
    return aggregationList;
  }

  private static Set<MappingTerms> assembleAggregationList() {
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
    aggregationTerms.add(DATASET_NAME);
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

}
