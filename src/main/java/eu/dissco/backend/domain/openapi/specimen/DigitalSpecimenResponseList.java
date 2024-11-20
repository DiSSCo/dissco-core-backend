package eu.dissco.backend.domain.openapi.specimen;

import eu.dissco.backend.domain.jsonapi.JsonApiLinksFull;
import eu.dissco.backend.domain.jsonapi.JsonApiMeta;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema
public record DigitalSpecimenResponseList(
    DigitalSpecimenResponseData data,
    @Schema(description = "Links object, for pagination") JsonApiLinksFull links,
    @Schema(description = "Response metadata") JsonApiMeta meta) {

}
