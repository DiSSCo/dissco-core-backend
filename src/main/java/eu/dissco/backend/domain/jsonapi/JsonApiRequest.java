package eu.dissco.backend.domain.jsonapi;

import com.fasterxml.jackson.databind.JsonNode;

public record JsonApiRequest(
    String type,
    JsonNode attributes
) {

}
