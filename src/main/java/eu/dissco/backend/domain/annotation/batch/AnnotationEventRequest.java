package eu.dissco.backend.domain.annotation.batch;

import eu.dissco.backend.schema.AnnotationProcessingRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema
public record AnnotationEventRequest(
    List<AnnotationProcessingRequest> annotationRequests,
    List<BatchMetadata> batchMetadata) {

}
