package eu.dissco.backend.domain.annotation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString
public class ClassSelector extends Selector {

  @JsonProperty("oa:class")
  private String oaClass;

  public ClassSelector() {
    super(SelectorType.CLASS_SELECTOR);
  }

  public ClassSelector(String oaClass) {
    super(SelectorType.CLASS_SELECTOR);
    this.oaClass = oaClass;
  }

  public ClassSelector withOaClass(String oaClass) {
    this.oaClass = oaClass;
    return this;
  }

}
