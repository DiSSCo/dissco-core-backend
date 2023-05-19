package eu.dissco.backend.domain.jsonapi;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
public class ExceptionResponseWrapper {
    private List<ExceptionResponse> errors;

    public ExceptionResponseWrapper(HttpStatus statusCode, String title, String detail){
        this.errors = List.of(new ExceptionResponse(String.valueOf(statusCode), title, detail));
    }

}
