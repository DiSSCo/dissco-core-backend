package eu.dissco.backend.domain;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum TaxonMappingTerms implements MappingTerm {
  KINGDOM("kingdom",
      "ods:hasIdentification.ods:hasTaxonIdentification.dwc:kingdom.keyword"),
  PHYLUM("phylum",
      "ods:hasIdentification.ods:hasTaxonIdentification.dwc:phylum.keyword"),
  CLASS("class",
      "ods:hasIdentification.ods:hasTaxonIdentification.dwc:class.keyword"),
  ORDER("order",
      "ods:hasIdentification.ods:hasTaxonIdentification.dwc:order.keyword"),
  FAMILY("family",
      "ods:hasIdentification.ods:hasTaxonIdentification.dwc:family.keyword"),
  GENUS("genus",
      "ods:hasIdentification.ods:hasTaxonIdentification.dwc:genus.keyword"),
  SPECIES("species", "ods:hasIdentification.ods:hasTaxonIdentification.dwc:scientificName.keyword");

  private static final Map<String, TaxonMappingTerms> taxonMapping = fillTaxonMapping();
  private final String requestName;
  private final String fullName;

  TaxonMappingTerms(String name, String fullName) {
    this.requestName = name;
    this.fullName = fullName;
  }

  public static Map<String, TaxonMappingTerms> getTaxonMapping() {
    return taxonMapping;
  }

  private static Map<String, TaxonMappingTerms> fillTaxonMapping() {
    var paramMap = new HashMap<String, TaxonMappingTerms>();
    paramMap.put(KINGDOM.requestName, KINGDOM);
    paramMap.put(PHYLUM.requestName, PHYLUM);
    paramMap.put(CLASS.requestName, CLASS);
    paramMap.put(ORDER.requestName, ORDER);
    paramMap.put(FAMILY.requestName, FAMILY);
    paramMap.put(GENUS.requestName, GENUS);
    paramMap.put(SPECIES.requestName, SPECIES);
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
