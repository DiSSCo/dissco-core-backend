package eu.dissco.backend.domain.annotation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString
public class FragmentSelector extends Selector {

  @JsonProperty("ac:hasRoi")
  private HasRoi acHasRoi;
  @JsonProperty("dcterms:conformsTo")
  private String dctermsConformsTo;

  public FragmentSelector() {
    super(SelectorType.FRAGMENT_SELECTOR);
  }

  public FragmentSelector(HasRoi acHasRoi, String dctermsConformsTo) {
    super(SelectorType.FRAGMENT_SELECTOR);
    this.acHasRoi = acHasRoi;
    this.dctermsConformsTo = dctermsConformsTo;
  }

  public FragmentSelector withAcHasRoi(HasRoi acHasRoi) {
    this.acHasRoi = acHasRoi;
    return this;
  }

  public FragmentSelector withDctermsConformsTo(String dctermsConformsTo) {
    this.dctermsConformsTo = dctermsConformsTo;
    return this;
  }

}
