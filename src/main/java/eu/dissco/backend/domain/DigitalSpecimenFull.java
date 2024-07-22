package eu.dissco.backend.domain;

import eu.dissco.backend.schema.Annotation;
import eu.dissco.backend.schema.DigitalSpecimen;
import java.util.List;

public record DigitalSpecimenFull(
    DigitalSpecimen digitalSpecimen,
    List<DigitalMediaFull> digitalMedia,
    List<Annotation> annotations
) {

}
