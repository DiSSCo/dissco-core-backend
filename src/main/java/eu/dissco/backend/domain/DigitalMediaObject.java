package eu.dissco.backend.domain;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;

public record DigitalMediaObject(
    String id,
    int version,
    Instant created,
    String type,
    String digitalSpecimenId,
    String mediaUrl,
    String format,
    String sourceSystemId,
    JsonNode data,
    JsonNode originalData) {

}
