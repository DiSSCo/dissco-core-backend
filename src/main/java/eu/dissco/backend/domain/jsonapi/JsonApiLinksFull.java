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
    String pn = "?pageNumber=";
    String ps = "&pageSize=";
    this.self = path + pn + pageNum + ps + pageSize;
    this.first = path + pn + "1" + ps + pageSize;
    this.prev = (pageNum <= 1) ? null : path + pn + (pageNum - 1) + ps + pageSize;
    this.next = (hasNext) ? path + pn + (pageNum + 1) + ps + pageSize : null;
  }

  public JsonApiLinksFull(String path) {
    this.self = path;
    this.first = null;
    this.next = null;
    this.prev = null;
  }

  public JsonApiLinksFull(MultiValueMap<String, String> params, int pageNum, int pageSize,
      boolean hasNext, String path) {
    params.remove("pageSize");
    params.remove("pageNumber");
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
