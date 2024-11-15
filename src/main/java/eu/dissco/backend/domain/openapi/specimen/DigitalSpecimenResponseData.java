package eu.dissco.backend.domain.openapi.specimen;

import eu.dissco.backend.schema.DigitalSpecimen;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema
public record DigitalSpecimenResponseData(
    @Schema(description = "DOI of the Digital Specimen") String id,
    @Schema(description = "Fdo type of the object") String type,
    DigitalSpecimen attributes) {

}
