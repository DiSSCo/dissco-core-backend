package eu.dissco.backend.domain;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;

public record DigitalSpecimen(
    String id,
    int midsLevel,
    int version,
    Instant created,
    String type,
    String physicalSpecimenId,
    String physicalSpecimenIdType,
    String specimenName,
    String organizationId,
    String datasetId,
    String physicalSpecimenCollection,
    String sourceSystemId,
    JsonNode data,
    JsonNode originalData,
    String dwcaId) {

}
