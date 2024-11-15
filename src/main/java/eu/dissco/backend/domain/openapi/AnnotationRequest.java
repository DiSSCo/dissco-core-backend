package eu.dissco.backend.domain.openapi;

import eu.dissco.backend.schema.AnnotationProcessingRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

@Schema
@Value
public class AnnotationRequest {

  String type;
  Data data;

  @Value
  private class Data {
    AnnotationProcessingRequest attributes;
  }

}
