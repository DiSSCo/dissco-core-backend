package eu.dissco.backend.domain.annotation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class HasRoi {

  @JsonProperty("ac:xFrac")
  double valX;
  @JsonProperty("ac:yFrac")
  double valY;
  @JsonProperty("ac:widthFrac")
  double widthFrac;
  @JsonProperty("ac:heightFrac")
  double heightFrac;

}
