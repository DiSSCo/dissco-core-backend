package eu.dissco.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

public class ServiceUtils {
  private ServiceUtils(){}

  @Autowired
  private static ObjectMapper mapper;

  protected static JsonNode createVersionNode(List<Integer> versions){
    var versionsNode = mapper.createObjectNode();
    var arrayNode = versionsNode.putArray("versions");
    versions.forEach(arrayNode::add);
    return versionsNode;
  }

}
