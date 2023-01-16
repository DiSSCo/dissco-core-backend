package eu.dissco.backend.domain;

import java.util.List;

public record JsonApiMetaWrapper (List<JsonApiData> data, JsonApiLinksFull links, JsonApiMeta meta) {

}