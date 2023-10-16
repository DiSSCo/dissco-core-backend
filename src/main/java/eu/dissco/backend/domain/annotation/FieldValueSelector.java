package eu.dissco.backend.domain.annotation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;


@Getter
public class FieldValueSelector extends Selector {

  @JsonProperty("ods:field")
  private String odsField;

  public FieldValueSelector() {
    super(SelectorType.FIELD_VALUE_SELECTOR);
  }

  FieldValueSelector(String odsField) {
    super(SelectorType.FIELD_VALUE_SELECTOR);
    this.odsField = odsField;
  }

  public FieldValueSelector withOdsField(String odsField) {
    this.odsField = odsField;
    return this;
  }
}
