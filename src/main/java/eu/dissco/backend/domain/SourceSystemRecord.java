package eu.dissco.backend.domain;

import java.time.Instant;

public record SourceSystemRecord(
        String id,
        Instant created,
        String name,
        String endpoint,
        String description,
        String mappingId
    ) {

}
