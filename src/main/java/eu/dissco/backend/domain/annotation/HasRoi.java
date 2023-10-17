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

  @JsonProperty("ac:xFrac")
  private double xFrac;
  @JsonProperty("ac:yFrac")
  private double yFrac;
  @JsonProperty("ac:widthFrac")
  private double widthFrac;
  @JsonProperty("ac:HeightFrac")
  private double heightFrac;

  public HasRoi withAcxFrac(double xFrac) {
    this.xFrac = xFrac;
    return this;
  }

  public HasRoi withAcYFrac(double yFrac) {
    this.yFrac = yFrac;
    return this;
  }

  public HasRoi withAcWidthFrac(double widthFrac) {
    this.widthFrac = widthFrac;
    return this;
  }

  public HasRoi withAcHeightFrac(double heightFrac) {
    this.heightFrac = heightFrac;
    return this;
  }

}
