package eu.dissco.backend.domain.jsonapi;

import java.util.List;
import lombok.Value;

@Value
public class JsonApiListResponseWrapper {
  List<JsonApiData> data;
  JsonApiLinksFull links;

  public JsonApiListResponseWrapper(List<JsonApiData> dataNode, JsonApiLinksFull linksNode){
    this.data = dataNode;
    this.links = linksNode;
  }

  public JsonApiListResponseWrapper(List<JsonApiData> annotationsPlusOne,  int pageNumber, int pageSize, String path) {
    boolean hasNextPage = annotationsPlusOne.size() > pageSize;
    this.data = hasNextPage ? annotationsPlusOne.subList(0, pageSize) : annotationsPlusOne;
    this.links = new JsonApiLinksFull(pageNumber, pageSize, hasNextPage, path);
  }

}
