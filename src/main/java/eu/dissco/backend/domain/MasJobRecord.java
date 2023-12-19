package eu.dissco.backend.domain;

public record MasJobRecord(
    String jobId,
    MasJobState state,
    String masId,
    String targetId,
    String orcid
) {

}
