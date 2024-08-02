package eu.dissco.backend.domain.annotation.batch;

import eu.dissco.backend.schema.AnnotationProcessingRequest;
import java.util.List;

public record AnnotationEventRequest(List<AnnotationProcessingRequest> annotationRequests,
                                     List<BatchMetadata> batchMetadata) {

}
