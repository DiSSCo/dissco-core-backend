package eu.dissco.backend.domain.annotation;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Body {

  @JsonProperty("ods:type")
  String odsType;
  @JsonProperty("oa:value")
  List<Object> oaValue;
  @JsonProperty("dcterms:reference")
  String dcTermsReference;
  @JsonProperty("ods:score")
  double odsScore;

}
