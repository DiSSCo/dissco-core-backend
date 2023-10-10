package eu.dissco.backend.domain;

public enum AnnotationState {
  SCHEDULED("scheduled"),
  FAILED("failed"),
  COMPLETED("completed");

  private final String state;

  AnnotationState(String s){
    this.state = s;
  }

  @Override
  public String toString(){
    return state;
  }

}
