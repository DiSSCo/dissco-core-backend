package eu.dissco.backend.domain.jsonapi;

import lombok.Value;
import org.springframework.util.MultiValueMap;

@Value
public class JsonApiLinksFull {

  private static final String PAGE_NUMBER = "pageNumber=";
  private static final String PAGE_SIZE = "pageSize=";

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

  private static String buildPathPages(String baseString, int pageNum, int pageSize) {
    var linkPath = new StringBuilder(baseString);
    if (!baseString.contains("?")) {
      linkPath.append("?");
    }
    if (!linkPath.toString().contains(PAGE_NUMBER)) {
      linkPath.append("&").append(PAGE_NUMBER).append(pageNum);
    } else {
      var str = linkPath.toString();
      linkPath=new StringBuilder(str.replaceAll(PAGE_NUMBER+"\\d+", PAGE_NUMBER+pageNum));
    }
    if (!linkPath.toString().contains(PAGE_SIZE)) {
      linkPath.append("&").append(PAGE_SIZE).append(pageSize);
    }
    return linkPath.toString();
  }
}
