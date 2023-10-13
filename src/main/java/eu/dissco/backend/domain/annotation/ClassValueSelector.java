package eu.dissco.backend.domain.annotation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class ClassValueSelector extends Selector {

  @JsonProperty("ods:class")
  private String odsClass;

  public ClassValueSelector() {
    super(SelectorType.CLASS_VALUE_SELECTOR);
  }

  public ClassValueSelector(String odsClass) {
    super(SelectorType.CLASS_VALUE_SELECTOR);
    this.odsClass = odsClass;
  }

  public ClassValueSelector withOdsClass(String odsClass) {
    this.odsClass = odsClass;
    return this;
  }

}
