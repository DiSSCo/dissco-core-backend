package eu.dissco.backend.domain.jsonapi;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Value;

@Value
public class JsonApiWrapper {

  JsonApiData data;
  JsonApiLinks links;
  JsonApiMeta meta;

  @JsonCreator
  public JsonApiWrapper(JsonApiData data, JsonApiLinks links) {
    this.data = data;
    this.links = links;
    this.meta = null;
  }

  public JsonApiWrapper(JsonApiData data, JsonApiLinks links, JsonApiMeta meta) {
    this.data = data;
    this.links = links;
    this.meta = meta;
  }
}
