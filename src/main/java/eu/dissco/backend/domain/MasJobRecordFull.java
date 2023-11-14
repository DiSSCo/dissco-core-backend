package eu.dissco.backend.domain;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.UUID;

public record MasJobRecordFull(
    AnnotationState state,
    String creatorId,
    String targetId,
    UUID jobId,
    Instant timeStarted,
    Instant timeCompleted,
    JsonNode annotations
) {

}
