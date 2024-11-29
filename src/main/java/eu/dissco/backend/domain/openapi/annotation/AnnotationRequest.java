package eu.dissco.backend.domain.openapi.annotation;

import eu.dissco.backend.domain.FdoType;
import eu.dissco.backend.schema.AnnotationProcessingRequest;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema
public record AnnotationRequest(
    AnnotationRequestData data) {

  @Schema
  public record AnnotationRequestData(
      @Schema(description = "Type of request. For annotations, must be \"ods:Annotation\"") FdoType type,
      @Schema(description = "Desired annotation") AnnotationProcessingRequest attributes) {

  }

}
