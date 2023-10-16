package eu.dissco.backend.domain.annotation;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Motivation {

  @JsonProperty("ods:adding") ADDING,
  @JsonProperty("oa:assessing") ASSESSING,
  @JsonProperty("oa:editing") EDITING,
  @JsonProperty("oa:commenting") COMMENTING;

  public static Motivation fromString(String motivation){
    switch (motivation){
      case "ods:adding" -> {
        return ADDING;
      } case "oa:assessing" -> {
        return ASSESSING;
      } case "oa:editing" -> {
        return EDITING;
      } case "oa:commenting" -> {
        return COMMENTING;
      } default -> throw new IllegalStateException("Unable to parse motivation " + motivation);
    }
  }

}
