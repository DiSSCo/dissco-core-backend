package eu.dissco.backend.domain.openapi;

import eu.dissco.backend.schema.AnnotationProcessingRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

@Schema
@Value
public class AnnotationRequest {

  Data data;

  public record Data(
      String type,
      AnnotationProcessingRequest attributes) {

  }

}
