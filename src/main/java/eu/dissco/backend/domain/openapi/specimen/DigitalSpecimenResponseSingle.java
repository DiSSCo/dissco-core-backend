package eu.dissco.backend.domain.openapi.specimen;

import eu.dissco.backend.domain.jsonapi.JsonApiLinksFull;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema
public record DigitalSpecimenResponseSingle(
    DigitalSpecimenResponseData data,
    @Schema(description = "Links object, self-referencing") JsonApiLinksFull links)
{

}
