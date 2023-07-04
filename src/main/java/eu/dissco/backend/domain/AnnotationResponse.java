package eu.dissco.backend.domain;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;

public record AnnotationResponse(
    String id,
    int version,
    String type,
    String motivation,
    JsonNode target,
    JsonNode body,
    int preferenceScore,
    String creator,
    Instant created,
    JsonNode generator,
    Instant generated,
    Instant deleted_on) {

}
