package eu.dissco.backend.domain.annotation;

import eu.dissco.backend.schema.Agent;
import eu.dissco.backend.schema.Annotation;

public record AnnotationTombstoneWrapper(
    Annotation annotation,
    Agent tombstoningAgent
) {

}
