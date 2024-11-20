package eu.dissco.backend.domain.openapi.annotation;

import eu.dissco.backend.domain.annotation.batch.BatchMetadata;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema
public record BatchAnnotationCountResponse(
    BatchAnnotationCountResponseData data) {

  @Schema
  private record BatchAnnotationCountResponseData(
      @Schema(description = "Type of response. In this case, \"batchAnnotationCount\"")
      String type,
      BatchAnnotationCountResponseAttributes attributes
  ) {

    @Schema
    private record BatchAnnotationCountResponseAttributes(
        @Schema(description = "Number of objects affected by given search parameters")
        Long objectAffected,
        @Schema(description = "Provided search parameters")
        BatchMetadata batchMetadata) {

    }
  }

}
