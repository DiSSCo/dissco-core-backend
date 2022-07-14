package eu.dissco.backend.domain;

import com.fasterxml.jackson.databind.JsonNode;

public record AnnotationRequest(String id, String type, JsonNode body, String target) {

}
