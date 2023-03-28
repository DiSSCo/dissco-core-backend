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

  public JsonApiListResponseWrapper(List<JsonApiData> domainObjectPlusOne,  int pageNumber, int pageSize, String path) {
    boolean hasNextPage = domainObjectPlusOne.size() > pageSize;
    this.data = hasNextPage ? domainObjectPlusOne.subList(0, pageSize) : domainObjectPlusOne;
    this.links = new JsonApiLinksFull(pageNumber, pageSize, hasNextPage, path);
  }

}
