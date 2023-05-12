package eu.dissco.backend.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class RepositoryUtils {

  public static final String HANDLE_STRING = "https://hdl.handle.net/";

  private RepositoryUtils() {
    // Utility class
  }

  public static void addUrlToAttributes(JsonNode attributes){
    ((ObjectNode) attributes).put("ods:sourceSystemId",
        HANDLE_STRING + attributes.get("ods:sourceSystemId").asText());
  }

  protected static int getOffset(int pageNumber, int pageSize) {
    int offset = 0;
    if (pageNumber > 1) {
      offset = offset + (pageSize * (pageNumber - 1));
    }
    return offset;
  }

}
