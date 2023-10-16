package eu.dissco.backend.domain.annotation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;


@Getter
@EqualsAndHashCode(callSuper = true)
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
