package eu.dissco.backend.domain;

import com.fasterxml.jackson.databind.JsonNode;

public record JsonApiWrapper(JsonApiData data, JsonApiLinks links) {

}
