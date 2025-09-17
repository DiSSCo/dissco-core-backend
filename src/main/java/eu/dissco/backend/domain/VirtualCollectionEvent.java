package eu.dissco.backend.domain;

import eu.dissco.backend.schema.VirtualCollection;

public record VirtualCollectionEvent(
    VirtualCollectionAction action,
    VirtualCollection virtualCollection
) {
}
