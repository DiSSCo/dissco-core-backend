package eu.dissco.backend.domain;

import java.time.Instant;

public record SourceSystemRecord(
        String id,
        Instant created,
        String type,
        String name,
        String endpoint,
        String description,
        String mappingId
    ) {

}
