package eu.dissco.backend.domain;

import lombok.Getter;

@Getter
public enum AnnotationState {
  SCHEDULED("scheduled"),
  FAILED("failed");

  private final String state;

  AnnotationState(String s){
    this.state = s;
  }

}