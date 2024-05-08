package eu.dissco.backend.domain.annotation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AggregateRating {

  @JsonProperty("ods:type")
  String odsType;
  @JsonProperty("schema.org:ratingCount")
  double ratingCount;
  @JsonProperty("schema.org:ratingValue")
  double ratingValue;

}
