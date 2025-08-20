package eu.dissco.backend.utils;

import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.domain.jsonapi.JsonApiLinksFull;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiMeta;
import java.util.List;

public class JsonApiUtils {

  private JsonApiUtils() {
    // Utility class, no instantiation needed
  }

  public static JsonApiListResponseWrapper wrapListResponse(
      List<JsonApiData> dataNodePlusOne, int pageSize, int pageNumber, String path) {
    boolean hasNext = dataNodePlusOne.size() > pageSize;
    var linksNode = new JsonApiLinksFull(pageNumber, pageSize, hasNext, path);
    var dataNode = hasNext ? dataNodePlusOne.subList(0, pageSize) : dataNodePlusOne;
    return new JsonApiListResponseWrapper(dataNode, linksNode, null);
  }

  public static JsonApiListResponseWrapper wrapListResponse(
      List<JsonApiData> dataNodePlusOne, long totalCount,
      int pageSize, int pageNumber, String path) {
    boolean hasNext = dataNodePlusOne.size() > pageSize;
    var linksNode = new JsonApiLinksFull(pageNumber, pageSize, hasNext, path);
    var dataNode = hasNext ? dataNodePlusOne.subList(0, pageSize) : dataNodePlusOne;
    var metaNode = new JsonApiMeta(totalCount);
    return new JsonApiListResponseWrapper(dataNode, linksNode, metaNode);
  }
}
