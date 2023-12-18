package eu.dissco.backend.domain;

public record MasJobRecord(
    String jobId,
    AnnotationState state,
    String creatorId,
    String targetId,
    String orcid
) {

}
