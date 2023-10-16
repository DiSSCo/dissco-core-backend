package eu.dissco.backend.domain.annotation;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({
    @JsonSubTypes.Type(ClassValueSelector.class),
    @JsonSubTypes.Type(FieldValueSelector.class),
    @JsonSubTypes.Type(FragmentSelector.class)})
public abstract class Selector {

  @JsonProperty("ods:type")
  protected final SelectorType odsType;

  protected Selector(SelectorType odsType) {
    this.odsType = odsType;
  }
}
