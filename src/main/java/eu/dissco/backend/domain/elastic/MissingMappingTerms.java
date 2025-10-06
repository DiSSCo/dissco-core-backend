package eu.dissco.backend.domain.elastic;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum MissingMappingTerms implements MappingTerm {
  HAS_KINGDOM("hasKingdom",
      "ods:hasIdentifications.ods:hasTaxonIdentifications.dwc:kingdom.keyword"),
  HAS_PHYLUM("hasPhylum", "ods:hasIdentifications.ods:hasTaxonIdentifications.dwc:phylum.keyword"),
  HAS_CLASS("hasClass", "ods:hasIdentifications.ods:hasTaxonIdentifications.dwc:class.keyword"),
  HAS_ORDER("hasOrder", "ods:hasIdentifications.ods:hasTaxonIdentifications.dwc:order.keyword"),
  HAS_FAMILY("hasFamily", "ods:hasIdentifications.ods:hasTaxonIdentifications.dwc:family.keyword"),
  HAS_GENUS("hasGenus", "ods:hasIdentifications.ods:hasTaxonIdentifications.dwc:genus.keyword"),
  HAS_SPECIES("hasSpecies",
      "ods:hasIdentifications.ods:hasTaxonIdentifications.dwc:scientificName.keyword"),
  HAS_COUNTRY("hasCountry", "ods:hasEvents.ods:hasLocation.dwc:country.keyword"),
  HAS_LOCALITY("hasLocality", "ods:hasEvents.ods:hasLocation.dwc:locality.keyword"),
  HAS_LATITUDE("hasLatitude",
      "ods:hasEvents.ods:hasLocation.ods:hasGeoreference.dwc:decimalLatitude"),
  HAS_LONGITUDE("hasLongitude",
      "ods:hasEvents.ods:hasLocation.ods:hasGeoreference.dwc:decimalLongitude"),
  ;

  private final String requestName;
  private final String fullName;
  private static final Map<String, MissingMappingTerms> missingMapping = fillMissingMapping();
  public static final Set<String> MISSING_FIELD_NAMES;

  static {
    var s = new HashSet<String>();
    for (var e : MissingMappingTerms.values()) {
      s.add(e.requestName());
    }
    MISSING_FIELD_NAMES = Collections.unmodifiableSet(s);
  }

  MissingMappingTerms(String requestName, String fullName) {
    this.requestName = requestName;
    this.fullName = fullName;
  }

  public static Map<String, MissingMappingTerms> getMissingMapping() {
    return missingMapping;
  }

  private static Map<String, MissingMappingTerms> fillMissingMapping() {
    var paramMap = new HashMap<String, MissingMappingTerms>();
    paramMap.put(HAS_CLASS.requestName, HAS_CLASS);
    paramMap.put(HAS_ORDER.requestName, HAS_ORDER);
    paramMap.put(HAS_GENUS.requestName, HAS_GENUS);
    paramMap.put(HAS_FAMILY.requestName, HAS_FAMILY);
    paramMap.put(HAS_SPECIES.requestName, HAS_SPECIES);
    paramMap.put(HAS_COUNTRY.requestName, HAS_COUNTRY);
    paramMap.put(HAS_LOCALITY.requestName, HAS_LOCALITY);
    paramMap.put(HAS_LATITUDE.requestName, HAS_LATITUDE);
    paramMap.put(HAS_LONGITUDE.requestName, HAS_LOCALITY);
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
