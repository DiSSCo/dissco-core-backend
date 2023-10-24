package eu.dissco.backend.domain;

import eu.dissco.backend.domain.annotation.Annotation;
import java.util.List;

public record DigitalMediaObjectFull(
    DigitalMediaObjectWrapper digitalMediaObject,
    List<Annotation> annotations
) {

}
