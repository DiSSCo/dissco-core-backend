package eu.dissco.backend.domain.openapi.media;

import eu.dissco.backend.domain.jsonapi.JsonApiLinks;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema
public record DigitalMediaResponseSingle(
    @Schema(description = "Links object, self-referencing") JsonApiLinks links,
    DigitalMediaResponseData data
) {

}
