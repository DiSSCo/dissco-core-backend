package eu.dissco.backend.domain.jsonapi;

import java.util.List;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ExceptionResponseWrapper {

  private final List<ExceptionResponse> errors;

  public ExceptionResponseWrapper(HttpStatus statusCode, String title, String detail) {
    this.errors = List.of(new ExceptionResponse(String.valueOf(statusCode), title, detail));
  }

}
