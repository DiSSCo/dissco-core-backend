package eu.dissco.backend.domain;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum TaxonMappingTerms implements MappingTerm {
  KINGDOM("kingdom",
      "digitalSpecimenWrapper.ods:attributes.dwc:identification.taxonIdentifications.dwc:kingdom.keyword"),
  PHYLUM("phylum",
      "digitalSpecimenWrapper.ods:attributes.dwc:identification.taxonIdentifications.dwc:phylum.keyword"),
  CLASS("class",
      "digitalSpecimenWrapper.ods:attributes.dwc:identification.taxonIdentifications.dwc:class.keyword"),
  ORDER("order",
      "digitalSpecimenWrapper.ods:attributes.dwc:identification.taxonIdentifications.dwc:order.keyword"),
  FAMILY("family",
      "digitalSpecimenWrapper.ods:attributes.dwc:identification.taxonIdentifications.dwc:family.keyword"),
  GENUS("genus",
      "digitalSpecimenWrapper.ods:attributes.dwc:identification.taxonIdentifications.dwc:genus.keyword"),
  SPECIES("species", "digitalSpecimenWrapper.ods:attributes.ods:specimenName.keyword");

  private static final Map<String, TaxonMappingTerms> taxonMapping = fillTaxonMapping();
  private static final TaxonMappingTerms[] values = values();
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
