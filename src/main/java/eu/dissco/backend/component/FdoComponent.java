package eu.dissco.backend.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import eu.dissco.backend.properties.FdoProperties;
import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FdoComponent {
  private final ObjectMapper mapper;
  private final FdoProperties fdoProperties;
  @Getter
  private JsonNode postRequest;

  @PostConstruct
  void setRequest() {
    postRequest = mapper.createObjectNode()
        .set("data", mapper.createObjectNode()
            .put("type", "jobId")
            .set("attributes", mapper.createObjectNode()
                .put("fdoProfile", fdoProperties.getProfile())
                .put("digitalObjectType", fdoProperties.getDigitalObjectType())
                .put("issuedForAgent", fdoProperties.getAgent())));
  }

  public JsonNode buildRollbackCreationRequest(String handle) {
    var dataNode = List.of(mapper.createObjectNode().put("id", handle));
    ArrayNode dataArray = mapper.valueToTree(dataNode);
    return mapper.createObjectNode().set("data", dataArray);
  }
}
