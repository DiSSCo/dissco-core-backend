package eu.dissco.backend.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

public record DigitalSpecimenJsonLD(
    @JsonProperty("@id") String id,
    @JsonProperty("@type") String type,
    @JsonProperty("@context") JsonNode context,
    @JsonProperty("ods:primarySpecimenData") JsonNode primaryData,
    @JsonProperty("ods:sourceSystemId") String sourceSystemId,
    @JsonProperty("ods:hasSpecimenMedia") List<String> digitalMediaObjects
) {

}
