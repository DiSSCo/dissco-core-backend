package eu.dissco.backend.domain;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;

public record MasJobRecordFull(
    MasJobState state,
    String masId,
    String targetId,
    MjrTargetType targetType,
    String orcid,
    String jobHandle,
    Instant timeStarted,
    Instant timeCompleted,
    JsonNode annotations
) {

}
