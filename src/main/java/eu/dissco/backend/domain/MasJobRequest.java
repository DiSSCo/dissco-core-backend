package eu.dissco.backend.domain;

public record MasJobRequest(
    String masId,
    boolean batching,
    Integer timeToLive) {
}
