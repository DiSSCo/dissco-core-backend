package eu.dissco.backend.domain.openapi.shared;

import eu.dissco.backend.domain.MasJobRecord;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema
public record MjrResponseData(
    @Schema(description = "ID of the resource") String id,
    @Schema(description = "Type of the resource") String type,
    MasJobRecord attributes) {

}
