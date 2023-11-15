package eu.dissco.backend.domain.annotation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString
public class ClassSelector extends Selector {

  @JsonProperty("ods:class")
  private String odsClass;

  public ClassSelector() {
    super(SelectorType.CLASS_SELECTOR);
  }

  public ClassSelector(String odsClass) {
    super(SelectorType.CLASS_SELECTOR);
    this.odsClass = odsClass;
  }

  public ClassSelector withOdsClass(String odsClass) {
    this.odsClass = odsClass;
    return this;
  }

}
