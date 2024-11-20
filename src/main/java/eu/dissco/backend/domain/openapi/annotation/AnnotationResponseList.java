package eu.dissco.backend.domain.openapi.annotation;

import eu.dissco.backend.domain.jsonapi.JsonApiLinksFull;
import eu.dissco.backend.domain.jsonapi.JsonApiMeta;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema
public record AnnotationResponseList(
    List<AnnotationResponseData> data,
    @Schema(description = "Links object, for pagination") JsonApiLinksFull links,
    @Schema(description = "Response metadata") JsonApiMeta meta) {

}
