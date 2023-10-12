package eu.dissco.backend.domain;

import com.fasterxml.jackson.databind.JsonNode;
import eu.dissco.backend.schema.DigitalSpecimen;

public record DigitalSpecimenWrapper(
    DigitalSpecimen digitalSpecimen,
    JsonNode originalData) {

}
