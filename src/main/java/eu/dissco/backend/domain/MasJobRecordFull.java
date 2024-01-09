package eu.dissco.backend.domain;

import com.fasterxml.jackson.databind.JsonNode;
import eu.dissco.backend.database.jooq.enums.JobStates;
import eu.dissco.backend.database.jooq.enums.TargetTypes;
import java.time.Instant;

public record MasJobRecordFull(
    JobStates state,
    String masId,
    String targetId,
    TargetTypes targetType,
    String orcid,
    String jobHandle,
    Instant timeStarted,
    Instant timeCompleted,
    JsonNode annotations
) {

}
