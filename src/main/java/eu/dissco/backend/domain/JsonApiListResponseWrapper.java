package eu.dissco.backend.domain;

import java.util.List;

public record JsonApiListResponseWrapper(List<JsonApiData> data, JsonApiLinksFull links) {

}
