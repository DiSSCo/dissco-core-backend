package eu.dissco.backend.domain;

import eu.dissco.backend.database.jooq.enums.MjrJobState;
import eu.dissco.backend.database.jooq.enums.MjrTargetType;

public record MasJobRecord(
    String jobId,
    MjrJobState state,
    String masId,
    String targetId,
    MjrTargetType targetType,
    String orcid,
    boolean batchingRequested,
    Integer timeToLive) {

}
