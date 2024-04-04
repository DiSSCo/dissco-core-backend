package eu.dissco.backend.domain;

public record MasJobRequest(
    String masId,
    boolean batching,
    Long timeToLive) {
}
