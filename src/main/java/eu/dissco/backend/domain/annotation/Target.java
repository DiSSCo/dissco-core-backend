package eu.dissco.backend.domain.annotation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Target {

  @JsonProperty("ods:id")
  String odsId;
  @JsonProperty("ods:type")
  String odsType;
  @JsonProperty("oa:selector")
  Selector oaSelector;

}
