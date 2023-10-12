package eu.dissco.backend.domain;

import java.util.List;

public record DigitalMediaObjectFull(
    DigitalMediaObjectWrapper digitalMediaObject,
    List<AnnotationResponse> annotations
) {

}
