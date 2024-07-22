package eu.dissco.backend.domain.annotation.batch;

import eu.dissco.backend.schema.Annotation;
import java.util.List;

public record AnnotationEvent(List<Annotation> annotationRequests,
                              List<BatchMetadata> batchMetadata) {

}
