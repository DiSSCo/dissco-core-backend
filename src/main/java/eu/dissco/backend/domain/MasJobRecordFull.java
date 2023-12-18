package eu.dissco.backend.domain;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;

public record MasJobRecordFull(
    AnnotationState state,
    String creatorId,
    String targetId,
    String orcid,
    String jobHandle,
    Instant timeStarted,
    Instant timeCompleted,
    JsonNode annotations
) {

}
