package eu.dissco.backend.domain.openapi;

import eu.dissco.backend.schema.AnnotationProcessingRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

@Schema
@Value
public class AnnotationRequest {

  @Schema(description = "data!")
  Data data;

  @Schema
  public record Data(
      @Schema(description = "Type of request. For annotations, must be \"ods:Annotation\"")
      String type,
      AnnotationProcessingRequest attributes) {
  }

}
