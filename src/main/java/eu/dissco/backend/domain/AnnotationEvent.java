package eu.dissco.backend.domain;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;

public record AnnotationEvent(
    String type,
    String motivation,
    String creator,
    Instant created,
    JsonNode target,
    JsonNode body
) {

}
