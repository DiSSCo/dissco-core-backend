package eu.dissco.backend.domain.jsonapi;

import lombok.Value;

@Value
public class JsonApiWrapper {

  JsonApiData data;
  JsonApiLinks links;
  JsonApiMeta meta;

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
