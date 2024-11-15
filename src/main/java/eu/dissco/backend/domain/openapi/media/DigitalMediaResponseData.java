package eu.dissco.backend.domain.openapi.media;

import eu.dissco.backend.schema.DigitalMedia;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema
public record DigitalMediaResponseData(
    @Schema(description = "DOI of the Digital Media") String id,
    @Schema(description = "Fdo type of the object") String type,
    DigitalMedia attributes
) {

}
