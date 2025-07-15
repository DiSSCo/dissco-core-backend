package eu.dissco.backend.domain.openapi.virtual_collection;

import eu.dissco.backend.domain.jsonapi.JsonApiLinks;
import eu.dissco.backend.domain.openapi.annotation.AnnotationResponseData;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema
public record VirtualCollectionResponseSingle(
    @Schema(description = "Links object, self-referencing") JsonApiLinks links,
    VirtualCollectionResponseData data) {

}
