package eu.dissco.backend.domain;

import com.fasterxml.jackson.databind.JsonNode;

public record AnnotationRequest(
    String type,
    String motivation,
    JsonNode target,
    JsonNode body) {

}
