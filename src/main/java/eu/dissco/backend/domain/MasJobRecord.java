package eu.dissco.backend.domain;

import eu.dissco.backend.database.jooq.enums.JobStates;
import eu.dissco.backend.database.jooq.enums.TargetTypes;

public record MasJobRecord(
    String jobId,
    JobStates state,
    String masId,
    String targetId,
    TargetTypes targetType,
    String orcid
) {

}
