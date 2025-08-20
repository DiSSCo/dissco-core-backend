package eu.dissco.backend.domain.openapi.virtual_collection;

import eu.dissco.backend.domain.FdoType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema
public record VirtualCollectionRequest(
    VirtualCollectionRequestData data) {

  @Schema
  public record VirtualCollectionRequestData(
      @Schema(description = "Type of request. For virtual collection, must be \"ods:VirtualCollection\"") FdoType type,
      @Schema(description = "Desired virtual collection") eu.dissco.backend.schema.VirtualCollectionRequest attributes) {

  }

}
