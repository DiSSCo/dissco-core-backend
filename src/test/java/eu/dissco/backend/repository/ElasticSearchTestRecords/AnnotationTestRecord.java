package eu.dissco.backend.repository.ElasticSearchTestRecords;

import eu.dissco.backend.domain.AnnotationResponse;
import java.time.Instant;

public record AnnotationTestRecord(
    String id,
    int version,
    Instant created,
    AnnotationResponse annotation
) {

}
