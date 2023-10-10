package eu.dissco.backend.domain;

public enum AnnotationState {
  SCHEDULED("scheduled"),
  FAILED("failed");

  private final String state;

  AnnotationState(String s){
    this.state = s;
  }

  public String getState(){
    return state;
  }

}