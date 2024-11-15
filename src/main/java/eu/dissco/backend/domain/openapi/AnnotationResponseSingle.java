package eu.dissco.backend.domain.openapi;

import eu.dissco.backend.domain.jsonapi.JsonApiLinks;
import eu.dissco.backend.schema.Annotation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

@Schema
@Value
public class AnnotationResponseSingle {

  JsonApiLinks links;
  Data data;

  private record Data(String id, String type, Annotation attributes) {

  }


}
