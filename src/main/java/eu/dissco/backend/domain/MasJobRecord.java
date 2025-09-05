package eu.dissco.backend.domain;

import com.fasterxml.jackson.annotation.JsonAlias;
import eu.dissco.backend.database.jooq.enums.JobState;
import eu.dissco.backend.database.jooq.enums.MjrTargetType;

public record MasJobRecord(
    String jobId,
    JobState state,
    String masId,
    String targetId,
    MjrTargetType targetType,
    @JsonAlias("agentId")
    String orcid,
    boolean batchingRequested,
    Integer timeToLive) {

}
