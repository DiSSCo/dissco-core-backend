package eu.dissco.backend.domain;

import eu.dissco.backend.database.jooq.enums.MjrTargetType;

public record MasScheduleJobRequest(
    String masId,
    String targetId,
    boolean batching,
    String agentId,
    MjrTargetType targetType
) {

}
