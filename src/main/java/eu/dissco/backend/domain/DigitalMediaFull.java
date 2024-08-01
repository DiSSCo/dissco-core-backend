package eu.dissco.backend.domain;

import eu.dissco.backend.schema.Annotation;
import eu.dissco.backend.schema.DigitalMedia;
import java.util.List;

public record DigitalMediaFull(
    DigitalMedia digitalMediaObject,
    List<Annotation> annotations
) {

}
