package eu.dissco.backend.domain;

public enum AnnotationState {
  SCHEDULED("scheduled"),
  FAILED("FAILED");

  private final String state;

  AnnotationState(String s){
    this.state = s;
  }

  @Override
  public String toString(){
    return state;
  }

}
