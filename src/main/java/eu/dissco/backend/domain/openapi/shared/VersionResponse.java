package eu.dissco.backend.domain.openapi.shared;

import eu.dissco.backend.domain.jsonapi.JsonApiLinks;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema
public record VersionResponse(
    VersionResponseData data,
    @Schema(description = "Links object, self-referencing") JsonApiLinks links) {

  @Schema
  private record VersionResponseData(
      @Schema(description = "Handle of the target annotation") String id,
      @Schema(description = "Type of response") String type,
      @Schema(description = "Versions of the target object") VersionResponseAttributes attributes) {

    @Schema
    private record VersionResponseAttributes(List<Integer> versions) {

    }
  }

}
