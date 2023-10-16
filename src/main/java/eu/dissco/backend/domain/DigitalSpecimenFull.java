package eu.dissco.backend.domain;

import com.fasterxml.jackson.databind.JsonNode;
import eu.dissco.backend.domain.annotation.Annotation;
import eu.dissco.backend.schema.DigitalSpecimen;
import java.util.List;

public record DigitalSpecimenFull(
    DigitalSpecimen digitalSpecimen,
    JsonNode originalData,
    List<DigitalMediaObjectFull> digitalMediaObjects,
    List<Annotation> annotations
) {

}
