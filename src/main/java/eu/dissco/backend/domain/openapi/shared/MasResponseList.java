package eu.dissco.backend.domain.openapi.shared;

import eu.dissco.backend.domain.jsonapi.JsonApiLinksFull;
import eu.dissco.backend.domain.jsonapi.JsonApiMeta;
import eu.dissco.backend.schema.MachineAnnotationService;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema
public record MasResponseList(
    List<MasResponseData> data,
    @Schema(description = "Links object, for pagination") JsonApiLinksFull links,
    @Schema(description = "Response metadata") JsonApiMeta meta) {

  @Schema
  private record MasResponseData (
      @Schema(description = "ID of the resource") String id,
      @Schema(description = "Type of the resource") String type,
      MachineAnnotationService attributes
  ){

  }


}
