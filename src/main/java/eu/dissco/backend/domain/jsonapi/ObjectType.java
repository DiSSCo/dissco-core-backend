package eu.dissco.backend.domain.jsonapi;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ObjectType {
  @JsonProperty("annotation")ANNOTATION("annotation"),
  @JsonProperty("mediaObject")MEDIA_OBJECT("digitalMediaObject");

  private String state;
  private ObjectType(String state){
    this.state = state;
  }

  @Override
  public String toString() {return state; }



}
