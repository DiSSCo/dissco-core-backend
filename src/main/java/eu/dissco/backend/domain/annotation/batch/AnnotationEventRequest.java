package eu.dissco.backend.domain.annotation.batch;

import eu.dissco.backend.schema.AnnotationRequest;
import java.util.List;

public record AnnotationEventRequest(List<AnnotationRequest> annotationRequests,
                                     List<BatchMetadata> batchMetadata) {

}
