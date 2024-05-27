package eu.dissco.backend.domain.annotation.batch;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.dissco.backend.domain.annotation.Annotation;
import lombok.Value;

public record AnnotationEvent(Annotation annotation, BatchMetadata batchMetadata) {

}
