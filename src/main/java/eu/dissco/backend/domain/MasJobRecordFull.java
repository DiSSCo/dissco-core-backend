package eu.dissco.backend.domain;

import com.fasterxml.jackson.databind.JsonNode;
import eu.dissco.backend.database.jooq.enums.MjrJobState;
import eu.dissco.backend.database.jooq.enums.MjrTargetType;
import java.time.Instant;

public record MasJobRecordFull(
    MjrJobState state,
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
