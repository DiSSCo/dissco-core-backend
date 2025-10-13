package eu.dissco.backend.domain.elastic;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum TaxonMappingTerms implements MappingTerm {
  KINGDOM("kingdom",
      "ods:hasIdentifications.ods:hasTaxonIdentifications.dwc:kingdom.keyword"),
  PHYLUM("phylum",
      "ods:hasIdentifications.ods:hasTaxonIdentifications.dwc:phylum.keyword"),
  CLASS("class",
      "ods:hasIdentifications.ods:hasTaxonIdentifications.dwc:class.keyword"),
  ORDER("order",
      "ods:hasIdentifications.ods:hasTaxonIdentifications.dwc:order.keyword"),
  FAMILY("family",
      "ods:hasIdentifications.ods:hasTaxonIdentifications.dwc:family.keyword"),
  GENUS("genus",
      "ods:hasIdentifications.ods:hasTaxonIdentifications.dwc:genus.keyword"),
  SPECIES("species", "ods:hasIdentifications.ods:hasTaxonIdentifications.dwc:scientificName.keyword");

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
