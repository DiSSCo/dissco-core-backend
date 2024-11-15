package eu.dissco.backend.domain.openapi.shared;

import eu.dissco.backend.domain.jsonapi.JsonApiLinks;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema
public record MjrResponseSingle(
    MjrResponseData data,
    @Schema(description = "Links object, self-referencing") JsonApiLinks links
) {



}
