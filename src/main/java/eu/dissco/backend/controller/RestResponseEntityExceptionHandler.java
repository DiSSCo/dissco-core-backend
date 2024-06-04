package eu.dissco.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.dissco.backend.domain.jsonapi.ExceptionResponseWrapper;
import eu.dissco.backend.exceptions.ConflictException;
import eu.dissco.backend.exceptions.ForbiddenException;
import eu.dissco.backend.exceptions.InvalidAnnotationRequestException;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.exceptions.PidCreationException;
import eu.dissco.backend.exceptions.UnknownParameterException;
import org.jooq.exception.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  @ExceptionHandler(JsonProcessingException.class)
  public ResponseEntity<ExceptionResponseWrapper> handleJsonException(JsonProcessingException e) {
    var exceptionResponse = new ExceptionResponseWrapper(
        HttpStatus.UNPROCESSABLE_ENTITY,
        "Json Processing Exception",
        e.getMessage()
    );
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(exceptionResponse);
  }

  @ResponseStatus(HttpStatus.BAD_GATEWAY)
  @ExceptionHandler(IOException.class)
  public ResponseEntity<ExceptionResponseWrapper> handleIOException(java.io.IOException e){
    logger.error("An IOException has occurred.", e);
    var exceptionResponse = new ExceptionResponseWrapper(
        HttpStatus.BAD_GATEWAY,
        "ElasticSearch exception",
        "Unable to connect to ElasticSearch services"
    );
    return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(exceptionResponse);
  }

  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  @ExceptionHandler(PidCreationException.class)
  public ResponseEntity<ExceptionResponseWrapper> handlePidCreationException(PidCreationException e) {
    var exceptionResponse = new ExceptionResponseWrapper(
        HttpStatus.UNPROCESSABLE_ENTITY,
        "PidCreationException",
        e.getMessage()
    );
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(exceptionResponse);
  }

  @ResponseStatus(HttpStatus.CONFLICT)
  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<ExceptionResponseWrapper> handleException(ConflictException e) {

    var message = "A conflict has occurred. Attempting to create a resource that already exists. ";
    var exceptionResponse = new ExceptionResponseWrapper(
        HttpStatus.CONFLICT,
        "ID Conflict",
        message
    );
    return ResponseEntity.status(HttpStatus.CONFLICT).body(exceptionResponse);
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ExceptionResponseWrapper> handleException(NotFoundException e) {
    var message =
        e.getMessage() == null ? "The requested resource was not found. " : e.getMessage();
    var exceptionResponse = new ExceptionResponseWrapper(
        HttpStatus.NOT_FOUND,
        "Resource Not Found",
        message
    );
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionResponse);
  }

  @ResponseStatus(HttpStatus.FORBIDDEN)
  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<ExceptionResponseWrapper> handleException(ForbiddenException e) {
    var exceptionResponse = new ExceptionResponseWrapper(
        HttpStatus.FORBIDDEN,
        "Forbidden",
        e.getMessage()
    );
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(exceptionResponse);
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ExceptionResponseWrapper> handleException(IllegalArgumentException e) {
    var exceptionResponse = new ExceptionResponseWrapper(
        HttpStatus.BAD_REQUEST,
        "Illegal Argument",
        e.getMessage()
    );
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(UnknownParameterException.class)
  public ResponseEntity<ExceptionResponseWrapper> handleException(UnknownParameterException e) {
    var exceptionResponse = new ExceptionResponseWrapper(
        HttpStatus.BAD_REQUEST,
        "Unknown Parameter",
        e.getMessage()
    );
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(InvalidAnnotationRequestException.class)
  public ResponseEntity<ExceptionResponseWrapper> handleException(
      InvalidAnnotationRequestException e) {
    var exceptionResponse = new ExceptionResponseWrapper(
        HttpStatus.BAD_REQUEST,
        "Invalid Annotation Request",
        e.getMessage()
    );
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
  }

}
