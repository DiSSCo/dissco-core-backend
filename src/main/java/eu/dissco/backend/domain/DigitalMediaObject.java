package eu.dissco.backend.domain;

import com.fasterxml.jackson.databind.JsonNode;

public record DigitalMediaObject(
    String id,
    int version,
    String type,
    String digitalSpecimenId,
    String mediaUrl,
    String format,
    String sourceSystemId,
    JsonNode data,
    JsonNode originalData) {

}
