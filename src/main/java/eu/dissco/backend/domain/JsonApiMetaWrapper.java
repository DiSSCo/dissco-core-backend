package eu.dissco.backend.domain;

public record JsonApiMetaWrapper (JsonApiData data, JsonApiLinks links, JsonApiMeta pageCount) {

}
