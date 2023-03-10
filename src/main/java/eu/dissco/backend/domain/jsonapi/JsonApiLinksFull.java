package eu.dissco.backend.domain.jsonapi;

import lombok.Value;

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

}
