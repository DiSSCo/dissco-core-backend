package eu.dissco.backend.domain.openapi.annotation;

import eu.dissco.backend.domain.annotation.AnnotationTargetType;
import eu.dissco.backend.domain.annotation.batch.BatchMetadata;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema
public record BatchAnnotationCountRequest(
    eu.dissco.backend.domain.openapi.annotation.BatchAnnotationCountRequest.BatchAnnotationCountRequestData data) {

  @Schema
  public record BatchAnnotationCountRequestData(
      @Schema(description = "Type of request, in this case \"batchAnnotationCountRequest\"") String type,
      BatchAnnotationCountRequest.BatchAnnotationCountRequestData.BatchAnnotationCountRequestAttributes attributes) {

    @Schema
    public record BatchAnnotationCountRequestAttributes(
        BatchMetadata batchMetadata,
        @Schema(description = "Type of target, either https://doi.org/21.T11148/894b1e6cad57e921764e (digital specimen) or https://doi.org21.T11148/bbad8c4e101e8af01115 (digital media)") AnnotationTargetType annotationTargetType) {

    }

  }

}
