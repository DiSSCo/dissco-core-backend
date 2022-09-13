package eu.dissco.backend.domain;

import java.util.List;

public record DigitalMediaObjectFull(
    DigitalMediaObject digitalMediaObject,
    List<AnnotationResponse> annotations
) {

}
