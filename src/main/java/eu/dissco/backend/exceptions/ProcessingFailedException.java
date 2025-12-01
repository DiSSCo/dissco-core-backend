package eu.dissco.backend.exceptions;

public class ProcessingFailedException extends Exception {

  public ProcessingFailedException(String message, Throwable cause) {
    super(message, cause);
  }

  public ProcessingFailedException(String message) {
    super(message);
  }
}
