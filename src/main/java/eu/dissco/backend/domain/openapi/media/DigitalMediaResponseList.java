package eu.dissco.backend.domain.openapi.media;

import eu.dissco.backend.domain.jsonapi.JsonApiLinksFull;
import eu.dissco.backend.domain.jsonapi.JsonApiMeta;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema
public record DigitalMediaResponseList(
    List<DigitalMediaResponseData> data,
    @Schema(description = "Links object, for pagination") JsonApiLinksFull links,
    @Schema(description = "Response metadata") JsonApiMeta meta) {

}
