package eu.dissco.backend.domain.jsonapi;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Value;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

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
      JsonMapper mapper) {
    this.id = id;
    this.type = type;
    this.attributes = mapper.valueToTree(domainObject);
  }
}
