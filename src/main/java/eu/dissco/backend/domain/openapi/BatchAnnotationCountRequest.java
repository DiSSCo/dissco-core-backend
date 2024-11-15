package eu.dissco.backend.domain.openapi;

import eu.dissco.backend.domain.annotation.AnnotationTargetType;
import eu.dissco.backend.domain.annotation.batch.BatchMetadata;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

@Schema
@Value
public class BatchAnnotationCountRequest {

  String type;

  private record Data(Attributes attributes){

  }

  private record Attributes(
      BatchMetadata batchMetadata,
      AnnotationTargetType annotationTargetType
  ){

  }

}
