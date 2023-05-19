package eu.dissco.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.dissco.backend.domain.jsonapi.ExceptionResponseWrapper;
import eu.dissco.backend.exceptions.ConflictException;
import eu.dissco.backend.exceptions.ForbiddenException;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.exceptions.UnknownParameterException;
import eu.dissco.backend.exceptions.UnprocessableEntityException;
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

  @ResponseStatus(HttpStatus.CONFLICT)
  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<ExceptionResponseWrapper> handleException(ConflictException e) {
    var exceptionResponse =  new ExceptionResponseWrapper(
            HttpStatus.CONFLICT,
            "Conflict",
            e.getMessage()
    );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(exceptionResponse);
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ExceptionResponseWrapper> handleException(NotFoundException e) {
    var exceptionResponse =  new ExceptionResponseWrapper(
            HttpStatus.NOT_FOUND,
            "Resource Not Found",
            e.getMessage()
    );
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionResponse);
  }

  @ResponseStatus(HttpStatus.FORBIDDEN)
  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<ExceptionResponseWrapper> handleException(ForbiddenException e) {
    var exceptionResponse =  new ExceptionResponseWrapper(
            HttpStatus.FORBIDDEN,
            "Forbidden",
            e.getMessage()
    );
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(exceptionResponse);
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ExceptionResponseWrapper> handleException(IllegalArgumentException e){
    var exceptionResponse =  new ExceptionResponseWrapper(
            HttpStatus.BAD_REQUEST,
            "Illegal Argument",
            e.getMessage()
    );
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
  }

  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  @ExceptionHandler(UnprocessableEntityException.class)
  public ResponseEntity<ExceptionResponseWrapper> handleException(UnprocessableEntityException e){
    var exceptionResponse =  new ExceptionResponseWrapper(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "Unprocessable Entity",
            e.getMessage()
    );
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(exceptionResponse);
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(UnknownParameterException.class)
  public ResponseEntity<ExceptionResponseWrapper> handleException(UnknownParameterException e){
    var exceptionResponse =  new ExceptionResponseWrapper(
            HttpStatus.BAD_REQUEST,
            "Unknown Parameter",
            e.getMessage()
    );
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
  }


}
