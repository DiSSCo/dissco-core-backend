package eu.dissco.backend.domain;

import eu.dissco.backend.exceptions.UnknownParameterException;
import java.util.HashMap;
import java.util.Map;

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
      "digitalSpecimenWrapper.ods:attributes.dwc:identification.taxonIdentifications.dwc:genus.keyword");

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
    return paramMap;
  }

  public static TaxonMappingTerms getNextLevel(int ordinal) throws UnknownParameterException {
    if (values.length > ordinal) {
      return values[ordinal+1];
    } else {
      throw new UnknownParameterException("No more levels after Genus");
    }
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
