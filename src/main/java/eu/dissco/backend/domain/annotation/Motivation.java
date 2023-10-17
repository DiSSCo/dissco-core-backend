package eu.dissco.backend.domain.annotation;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Motivation {

  @JsonProperty("ods:adding") ADDING("ods:adding"),
  @JsonProperty("oa:assessing") ASSESSING("oa:assessing"),
  @JsonProperty("oa:editing") EDITING("oa:editing"),
  @JsonProperty("oa:commenting") COMMENTING("oa:commenting");
  private final String state;

  private Motivation(String s){
    this.state = s;
  }

  @Override
  public String toString() {
    return state;
  }

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
