package eu.dissco.backend.domain;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema
public record MasJobRequest(
    @Parameter(description = "ID of the MAS") String masId,
    @Parameter(description = "If MAS should be run as a batch") boolean batching
) {
}
