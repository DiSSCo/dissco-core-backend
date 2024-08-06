package eu.dissco.backend.domain.annotation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum AnnotationTargetType {

  @JsonProperty("https://doi.org/21.T11148/894b1e6cad57e921764e") DIGITAL_SPECIMEN(
      "https://doi.org/21.T11148/894b1e6cad57e921764e"),
  @JsonProperty("https://doi.org21.T11148/bbad8c4e101e8af01115") DIGITAL_MEDIA(
      "https://doi.org21.T11148/bbad8c4e101e8af01115");

  @Getter
  private String name;

  AnnotationTargetType(String name) {
    this.name = name;
  }

  public static AnnotationTargetType fromString(String type){
    if (DIGITAL_SPECIMEN.getName().equals(type)){
      return DIGITAL_SPECIMEN;
    }
    if (DIGITAL_MEDIA.getName().equals(type)){
      return DIGITAL_MEDIA;
    }
    log.error("Invalid annotation target type {}", type);
    throw new IllegalStateException();
  }

}
