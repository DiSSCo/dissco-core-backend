package eu.dissco.backend.domain.annotation.batch;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.dissco.backend.schema.Annotation;
import java.util.List;

public record AnnotationEvent(@JsonProperty("annotations") List<Annotation> annotationRequests,
                              List<BatchMetadata> batchMetadata, String jobId) {

}
