package eu.dissco.backend.exceptions;

public class UnprocessableEntityException extends Exception {

  public UnprocessableEntityException(){super();}
  public UnprocessableEntityException(String message) {
    super(message);
  }

}
