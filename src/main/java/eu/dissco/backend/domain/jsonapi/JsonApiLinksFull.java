package eu.dissco.backend.domain.jsonapi;

import lombok.Value;
import org.springframework.util.MultiValueMap;

@Value
public class JsonApiLinksFull {

  String self;
  String first;
  String next;
  String prev;

  public JsonApiLinksFull(int pageNum, int pageSize, boolean hasNext, String path) {
    this.self = buildPathPages(path, pageNum, pageSize);
    this.first = buildPathPages(path, 1, pageSize);
    this.prev = (pageNum <= 1) ? null : buildPathPages(path, (pageNum - 1), pageSize);
    this.next = (hasNext) ? buildPathPages(path, (pageNum + 1), pageSize) : null;
  }

  public JsonApiLinksFull(String path) {
    this.self = path;
    this.first = null;
    this.next = null;
    this.prev = null;
  }

  public JsonApiLinksFull(MultiValueMap<String, String> params, int pageNum, int pageSize,
      boolean hasNext, String path) {
    var builder = new StringBuilder(path);
    var isFirst = true;
    for (var entry : params.entrySet()) {
      for (var value : entry.getValue()) {
        if (isFirst) {
          builder.append("?").append(entry.getKey()).append("=").append(value);
          isFirst = false;
        } else {
          builder.append("&").append(entry.getKey()).append("=").append(value);
        }
      }
    }
    var pathFull = builder.toString();

    this.self = buildPathPages(pathFull, pageNum, pageSize, isFirst);
    this.first = buildPathPages(pathFull, 1, pageSize, isFirst);
    this.prev = (pageNum <= 1) ? null : buildPathPages(pathFull, (pageNum - 1), pageSize, isFirst);
    this.next = (hasNext) ? buildPathPages(pathFull, (pageNum + 1), pageSize, isFirst) : null;
  }

  private String buildPathPages(String baseString, int pageNum, int pageSize) {
    return buildPathPages(baseString, pageNum, pageSize, true);
  }

  private String buildPathPages(String baseString, int pageNum, int pageSize, boolean isFirst) {
    var builder = new StringBuilder(baseString);
    if (isFirst) {
      builder.append("?");
    } else {
      builder.append("&");
    }
    builder.append("pageNumber=").append(pageNum).append("&pageSize=").append(pageSize);
    return builder.toString();
  }
}
