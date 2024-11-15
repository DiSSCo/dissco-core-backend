package eu.dissco.backend.domain.openapi.annotation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema
public record AnnotationVersionResponse(
    eu.dissco.backend.domain.openapi.annotation.AnnotationVersionResponse.AnnotationVersionResponseData data) {

  @Schema
  private record AnnotationVersionResponseData(
      @Schema(description = "Handle of the target annotation") String id,
      @Schema(description = "Type of response, in this case \"annotationVersions\"") String type,
      @Schema(description = "Versions of the target annotations") AnnotationVersionResponse.AnnotationVersionResponseData.AnnotationVersionResponseAttributes attributes) {

    @Schema
    private record AnnotationVersionResponseAttributes(List<Integer> versions) {

    }
  }

}
