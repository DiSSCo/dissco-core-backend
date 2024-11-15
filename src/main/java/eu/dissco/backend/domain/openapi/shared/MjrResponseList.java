package eu.dissco.backend.domain.openapi.shared;

import eu.dissco.backend.domain.MasJobRecord;
import eu.dissco.backend.domain.jsonapi.JsonApiLinksFull;
import eu.dissco.backend.domain.jsonapi.JsonApiMeta;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema
public record MjrResponseList(
    List<MjrResponseData> data,
    @Schema(description = "Links object, for pagination")
    JsonApiLinksFull links,
    @Schema(description = "Response metadata")
    JsonApiMeta meta) {

  @Schema
  private record MjrResponseData(
      @Schema(description = "ID of the resource") String id,
      @Schema(description = "Type of the resource") String type,
      MasJobRecord attributes
  ) {

  }
}
