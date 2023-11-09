package eu.dissco.backend.domain.annotation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class HasRoi {

  @JsonProperty("ac:widthFrac")
  private double widthFrac;
  @JsonProperty("ac:heightFrac")
  private double heightFrac;
  @JsonProperty("ac:xFrac")
  private double valX;
  @JsonProperty("ac:yFrac")
  private double valY;


}
