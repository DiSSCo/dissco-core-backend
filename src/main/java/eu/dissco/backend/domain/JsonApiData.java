package eu.dissco.backend.domain;

import com.fasterxml.jackson.databind.JsonNode;

public record JsonApiData (String id, String type, JsonNode attributes) {

}
