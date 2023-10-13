package eu.dissco.backend.domain.annotation;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum SelectorType {
  @JsonProperty("FieldValueSelector") FIELD_VALUE_SELECTOR,
  @JsonProperty("ClassValueSelector") CLASS_VALUE_SELECTOR,
  @JsonProperty("FragmentSelector") FRAGMENT_SELECTOR;

}
