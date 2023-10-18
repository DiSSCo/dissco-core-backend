package eu.dissco.backend.domain.annotation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class Target {

  @JsonProperty("ods:id")
  private String odsId;
  @JsonPropertyOrder("ods:type")
  private String odsType;
  @JsonProperty("oa:Selector")
  private Selector oaSelector;

  public Target withOdsId(String odsId) {
    this.odsId = odsId;
    return this;
  }

  public Target withOdsType(String odsType) {
    this.odsType = odsType;
    return this;
  }

  public Target withSelector(Selector oaSelector) {
    this.oaSelector = oaSelector;
    return this;
  }
}
