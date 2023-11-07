package eu.dissco.backend.domain;

import lombok.Getter;

@Getter
public enum AnnotationState {
  SCHEDULED("scheduled"),
  FAILED("failed"),
  COMPLETED("completed");

  private final String state;

  AnnotationState(String s) {
    this.state = s;
  }

  public static AnnotationState fromString(String s) {
    switch (s.toLowerCase()) {
      case "scheduled" -> {
        return SCHEDULED;
      }
      case "failed" -> {
        return FAILED;
      }
      case "completed" -> {
        return COMPLETED;
      }
      default ->
          throw new IllegalStateException("Unable to construct AnnotationState from state " + s);
    }

  }

}