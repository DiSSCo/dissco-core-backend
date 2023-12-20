package eu.dissco.backend.domain;

public enum MasJobState {
  SCHEDULED("SCHEDULED"),
  FAILED("FAILED"),
  COMPLETED("COMPLETED"),
  RUNNING("RUNNING");

  private final String state;

  MasJobState(String s) {
    this.state = s;
  }

}