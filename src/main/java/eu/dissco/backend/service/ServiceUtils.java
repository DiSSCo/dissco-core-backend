package eu.dissco.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

public class ServiceUtils {
  private ServiceUtils(){}


  protected static JsonNode createVersionNode(List<Integer> versions, ObjectMapper mapper){
    var versionsNode = mapper.createObjectNode();
    var arrayNode = versionsNode.putArray("versions");
    versions.forEach(arrayNode::add);
    return versionsNode;
  }

}
