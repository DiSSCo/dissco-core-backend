package eu.dissco.backend.domain.openapi;

import eu.dissco.backend.domain.jsonapi.JsonApiLinksFull;
import eu.dissco.backend.domain.jsonapi.JsonApiMeta;
import eu.dissco.backend.schema.Annotation;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Value;

@Schema
@Value
public class AnnotationResponseList {
  List<Data> data;
  JsonApiLinksFull links;
  JsonApiMeta meta;


  private record Data(
      String id,
      String type,
      Annotation attributes
  ) {}


}
