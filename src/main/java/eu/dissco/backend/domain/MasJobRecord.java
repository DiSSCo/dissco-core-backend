package eu.dissco.backend.domain;

public record MasJobRecord(
    AnnotationState state,
    String creatorId,
    String targetId
) {

}
