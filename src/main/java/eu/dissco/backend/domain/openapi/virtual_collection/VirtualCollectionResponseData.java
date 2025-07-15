package eu.dissco.backend.domain.openapi.virtual_collection;

import eu.dissco.backend.schema.VirtualCollection;
import io.swagger.v3.oas.annotations.media.Schema;

public record VirtualCollectionResponseData(
    @Schema(description = "Handle of the virtual collection") String id,
    @Schema(description = "Type of the object, in this case \"ods:VirtualCollection\"") String type,
    @Schema(description = "Annotation")
    VirtualCollection attributes) {

}
