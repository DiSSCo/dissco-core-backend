package eu.dissco.backend.domain;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public enum MappingTerms {
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
    aggregationTerms.add(COUNTRY);
    aggregationTerms.add(MIDS_LEVEL);
    aggregationTerms.add(TYPE_STATUS);
    aggregationTerms.add(LICENSE);
    aggregationTerms.add(HAS_MEDIA);
    aggregationTerms.add(ORGANISATION_NAME);
    aggregationTerms.add(SOURCE_SYSTEM_ID);
    aggregationTerms.add(DATASET_ID);
    return aggregationTerms;
  }

  private static Map<String, String> getParamMapping() {
    var paramMap = new HashMap<String, String>();
    paramMap.put(COUNTRY.name, COUNTRY.fullName);
    paramMap.put(COUNTRY_CODE.name, COUNTRY_CODE.fullName);
    paramMap.put(MIDS_LEVEL.name, MIDS_LEVEL.fullName);
    paramMap.put(PHYSICAL_SPECIMEN_ID.name, PHYSICAL_SPECIMEN_ID.fullName);
    paramMap.put(TYPE_STATUS.name, TYPE_STATUS.fullName);
    paramMap.put(LICENSE.name, LICENSE.fullName);
    paramMap.put(HAS_MEDIA.name, HAS_MEDIA.fullName);
    paramMap.put(ORGANISATION_ID.name, ORGANISATION_NAME.fullName);
    paramMap.put(ORGANISATION_NAME.name, ORGANISATION_NAME.fullName);
    paramMap.put(SOURCE_SYSTEM_ID.name, SOURCE_SYSTEM_ID.fullName);
    paramMap.put(SPECIMEN_NAME.name, SPECIMEN_NAME.fullName);
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
