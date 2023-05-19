package eu.dissco.backend.domain.jsonapi;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ExceptionResponse(
        @JsonProperty("status")
        String statusCode,
        String title,
        String detail
) {
}
