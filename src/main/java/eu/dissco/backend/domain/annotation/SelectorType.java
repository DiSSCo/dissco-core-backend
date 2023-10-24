package eu.dissco.backend.domain.annotation;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum SelectorType {
  @JsonProperty("FieldValueSelector") FIELD_VALUE_SELECTOR("FieldValueSelector"),
  @JsonProperty("ClassValueSelector") CLASS_VALUE_SELECTOR("ClassValueSelector"),
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
