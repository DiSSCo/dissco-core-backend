package eu.dissco.backend.domain.annotation;


import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class Selector {

  @JsonProperty("ods:type")
  protected final SelectorType odsType;

  protected Selector(SelectorType odsType) {
    this.odsType = odsType;
  }
}
