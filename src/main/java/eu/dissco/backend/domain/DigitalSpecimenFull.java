package eu.dissco.backend.domain;

import java.util.List;

public record DigitalSpecimenFull(
    DigitalSpecimen digitalSpecimen,
    List<DigitalMediaObjectFull> digitalMediaObjects,
    List<AnnotationResponse> annotations
) {

}
