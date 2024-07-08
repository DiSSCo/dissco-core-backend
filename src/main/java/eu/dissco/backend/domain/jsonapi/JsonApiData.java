package eu.dissco.backend.domain.jsonapi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Value;

@Value
public class JsonApiData {

  String id;
  String type;
  JsonNode attributes;

  @JsonCreator
  public JsonApiData(String id, String type, JsonNode attributes) {
    this.id = id;
    this.type = type;
    this.attributes = attributes;
  }

  public <T extends Object> JsonApiData(String id, String type, T domainObject,
      ObjectMapper mapper) {
    this.id = id;
    this.type = type;
    this.attributes = mapper.valueToTree(domainObject);
  }
}
