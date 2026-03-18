package eu.dissco.backend.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import tools.jackson.databind.JsonNode;

public record DigitalSpecimenJsonLD(
    @JsonProperty("@id") String id,
    @JsonProperty("@type") String type,
    @JsonProperty("@context") JsonNode context,
    @JsonProperty("ods:primarySpecimenData") JsonNode primaryData,
    @JsonProperty("ods:hasSpecimenMedia") List<String> digitalMediaObjects
) {

}
