package eu.dissco.backend.domain.annotation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class Creator {

  @JsonProperty("ods:type")
  String odsType;
  @JsonProperty("foaf:name")
  String foafName;
  @JsonProperty("ods:id")
  String odsId;

}
