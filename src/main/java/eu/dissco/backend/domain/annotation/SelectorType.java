package eu.dissco.backend.domain.annotation;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum SelectorType {
  @JsonProperty("FieldSelector") FIELD_SELECTOR("FieldSelector"),
  @JsonProperty("ClassSelector") CLASS_SELECTOR("ClassSelector"),
  @JsonProperty("FragmentSelector") FRAGMENT_SELECTOR("FragmentSelector");

  private final String state;

  SelectorType(String s){
    this.state = s;
  }

  @Override
  public String toString() {
    return state;
  }
}
