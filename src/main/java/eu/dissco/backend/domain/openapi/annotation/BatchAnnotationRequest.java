package eu.dissco.backend.domain.openapi.annotation;

import eu.dissco.backend.domain.annotation.batch.AnnotationEventRequest;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema
public record BatchAnnotationRequest(
    eu.dissco.backend.domain.openapi.annotation.BatchAnnotationRequest.BatchAnnotationRequestData data) {

  @Schema
  public record BatchAnnotationRequestData(
      @Schema(description = "Type of request, in this case \"ods:Annotation\"") String type,
      AnnotationEventRequest attributes) {

  }

}
