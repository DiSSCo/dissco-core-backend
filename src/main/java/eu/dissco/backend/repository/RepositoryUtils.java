package eu.dissco.backend.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class RepositoryUtils {

  public static final String HANDLE_STRING = "https://hdl.handle.net/";
  private static final String SOURCE_SYSTEM_ID = "ods:sourceSystemId";

  private RepositoryUtils() {
    // Utility class
  }

  public static void addUrlToAttributes(JsonNode attributes){
    if (attributes.get(SOURCE_SYSTEM_ID) != null){
      ((ObjectNode) attributes).put(SOURCE_SYSTEM_ID,
          HANDLE_STRING + attributes.get(SOURCE_SYSTEM_ID).asText());
    }
  }

  protected static int getOffset(int pageNumber, int pageSize) {
    int offset = 0;
    if (pageNumber > 1) {
      offset = offset + (pageSize * (pageNumber - 1));
    }
    return offset;
  }

}
