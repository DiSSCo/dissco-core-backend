package eu.dissco.backend.domain;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;

public record AnnotationResponse(String id, String type, JsonNode body, String target,
                                 Instant lastUpdated, String creator, Instant created) {
}
