package eu.dissco.backend.controller;

import eu.dissco.backend.exceptions.NotFoundException;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice(assignableTypes = ElvisController.class)
public class ElvisRestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<Void> handleNotFoundElvis(NotFoundException e){
    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(IOException.class)
  public ResponseEntity<Void> handleIOExceptionElvis(IOException e){
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
  }

}
