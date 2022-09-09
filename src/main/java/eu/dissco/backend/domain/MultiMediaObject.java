package eu.dissco.backend.domain;

import com.fasterxml.jackson.databind.JsonNode;

public record MultiMediaObject(
    String id,
    int version,
    String type,
    String physicalSpecimenId,
    String mediaUrl,
    String format,
    String sourceSystemId,
    JsonNode data,
    JsonNode originalData) {

}
