package eu.dissco.backend.domain;

import com.fasterxml.jackson.databind.JsonNode;
import eu.dissco.backend.schema.DigitalEntity;

public record DigitalMediaObjectWrapper(
    DigitalEntity digitalEntity,
    JsonNode originalData) {

}
