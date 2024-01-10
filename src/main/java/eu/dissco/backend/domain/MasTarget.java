package eu.dissco.backend.domain;


public record MasTarget(
    Object object,
    String jobId,
    boolean allowBatch
) {


}
