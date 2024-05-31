package eu.dissco.backend.domain.annotation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

public enum AnnotationTargetType {

  @JsonProperty("https://doi.org/21.T11148/894b1e6cad57e921764e") DIGITAL_SPECIMEN(
      "https://doi.org/21.T11148/894b1e6cad57e921764e"),
  @JsonProperty("https://doi.org21.T11148/bbad8c4e101e8af01115") MEDIA_OBJECT(
      "https://doi.org21.T11148/bbad8c4e101e8af01115");

  @Getter
  private String name;

  AnnotationTargetType(String name) {
    this.name = name;
  }

}
