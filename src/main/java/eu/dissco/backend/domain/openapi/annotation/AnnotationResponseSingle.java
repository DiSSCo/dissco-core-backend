package eu.dissco.backend.domain.openapi.annotation;

import eu.dissco.backend.domain.jsonapi.JsonApiLinks;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema
public record AnnotationResponseSingle(
    @Schema(description = "Links object, self-referencing") JsonApiLinks links,
    AnnotationResponseData data) {

}
