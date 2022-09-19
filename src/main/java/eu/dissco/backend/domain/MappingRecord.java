package eu.dissco.backend.domain;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;

public record MappingRecord(
    String id,
    int version,
    Instant created,
    String creator,
    String name,
    String description,
    JsonNode mapping
) {

}
