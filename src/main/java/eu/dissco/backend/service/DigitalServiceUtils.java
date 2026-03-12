package eu.dissco.backend.service;

import java.util.List;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

public class DigitalServiceUtils {

  private DigitalServiceUtils() {
  }

  protected static JsonNode createVersionNode(List<Integer> versions, JsonMapper mapper) {
    var versionsNode = mapper.createObjectNode();
    var arrayNode = versionsNode.putArray("versions");
    versions.forEach(arrayNode::add);
    return versionsNode;
  }

}
