package eu.dissco.backend.domain.annotation;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum TargetType {
  @JsonProperty("DigitalSpecimen") DIGITAL_SPECIMEN("DigitalSpecimen"),
  @JsonProperty("Annotation") ANNOTATION("Annotation"),
  @JsonProperty("MediaObject") MEDIA_OBJECT("MediaObject");

  private final String state;

  private TargetType(String s) {
    this.state = s;
  }

  @Override
  public String toString() {
    return this.state;
  }

}
