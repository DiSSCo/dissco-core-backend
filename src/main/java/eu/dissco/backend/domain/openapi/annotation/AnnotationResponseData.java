package eu.dissco.backend.domain.openapi.annotation;

import eu.dissco.backend.schema.Annotation;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema
public record AnnotationResponseData(
    @Schema(description = "Handle of the annotation") String id,
    @Schema(description = "Type of the object, in this case \"ods:Annotation\"") String type,
    @Schema(description = "Annotation") Annotation attributes) {

}
