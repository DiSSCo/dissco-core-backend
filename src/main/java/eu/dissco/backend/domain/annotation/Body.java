package eu.dissco.backend.domain.annotation;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Body {

  @JsonProperty("ods:type")
  private String odsType;
  @JsonProperty("oa:value")
  private List<Object> oaValue;
  @JsonProperty("dcterms:reference")
  private String dcTermsReference;
  @JsonProperty("ods:score")
  private double odsScore;

  public Body withOdsType(String odsType) {
    this.odsType = odsType;
    return this;
  }

  public Body withOaValue(List<Object> oaValue) {
    this.oaValue = oaValue;
    return this;
  }

  public Body withDcTermsReference(String dcTermsReference) {
    this.dcTermsReference = dcTermsReference;
    return this;
  }

  public Body withOdsScore(double odsScore) {
    this.odsScore = odsScore;
    return this;
  }

}
