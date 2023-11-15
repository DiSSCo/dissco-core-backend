package eu.dissco.backend.domain.annotation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;


@Getter
@EqualsAndHashCode(callSuper = true)
@ToString
public class FieldSelector extends Selector {

  @JsonProperty("ods:field")
  private String odsField;

  public FieldSelector() {
    super(SelectorType.FIELD_SELECTOR);
  }

  FieldSelector(String odsField) {
    super(SelectorType.FIELD_SELECTOR);
    this.odsField = odsField;
  }

  public FieldSelector withOdsField(String odsField) {
    this.odsField = odsField;
    return this;
  }
}
