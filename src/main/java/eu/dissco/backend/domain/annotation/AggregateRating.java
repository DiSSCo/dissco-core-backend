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
public class AggregateRating {

  @JsonProperty("ods:type")
  private String odsType;
  @JsonProperty("schema.org:ratingCount")
  private double ratingCount;
  @JsonProperty("schema.org:ratingValue")
  private double ratingValue;

  public AggregateRating withOdsType(String odsType) {
    this.odsType = odsType;
    return this;
  }

  public AggregateRating withRatingCount(double ratingCount) {
    this.ratingCount = ratingCount;
    return this;
  }

  public AggregateRating withRatingValue(double ratingValue) {
    this.ratingValue = ratingValue;
    return this;
  }


}
