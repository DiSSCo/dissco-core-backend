package eu.dissco.backend.domain.jsonapi;

import java.util.List;
import lombok.Value;

@Value
public class JsonApiListResponseWrapper {

  List<JsonApiData> data;
  JsonApiLinksFull links;
  JsonApiMeta meta;

  public JsonApiListResponseWrapper(List<JsonApiData> dataNode, JsonApiLinksFull linksNode) {
    this.data = dataNode;
    this.links = linksNode;
    this.meta = null;
  }

  public JsonApiListResponseWrapper(List<JsonApiData> dataNode, JsonApiLinksFull linksNode,
      JsonApiMeta metadata) {
    this.data = dataNode;
    this.links = linksNode;
    this.meta = metadata;
  }

  public JsonApiListResponseWrapper(List<JsonApiData> domainObjectPlusOne, int pageNumber,
      int pageSize, String path) {
    boolean hasNextPage = domainObjectPlusOne.size() > pageSize;
    this.data = hasNextPage ? domainObjectPlusOne.subList(0, pageSize) : domainObjectPlusOne;
    this.links = new JsonApiLinksFull(pageNumber, pageSize, hasNextPage, path);
    this.meta = null;
  }

  public JsonApiListResponseWrapper(List<JsonApiData> domainObjectPlusOne, int pageNumber,
      int pageSize, String path, JsonApiMeta meta) {
    boolean hasNextPage = domainObjectPlusOne.size() > pageSize;
    this.data = hasNextPage ? domainObjectPlusOne.subList(0, pageSize) : domainObjectPlusOne;
    this.links = new JsonApiLinksFull(pageNumber, pageSize, hasNextPage, path);
    this.meta = meta;
  }

}
