package eu.dissco.backend.domain.annotation.batch;

import eu.dissco.backend.domain.annotation.Annotation;
import java.util.List;

public record AnnotationEvent(List<Annotation> annotations, List<BatchMetadata> batchMetadata) {

}
