package eu.dissco.backend.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.properties.FdoProperties;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@Component
public class FdoRecordComponent {
  private final JsonNode postRequest;

  public FdoRecordComponent(ObjectMapper mapper, FdoProperties fdoProperties) {
    this.postRequest = mapper.createObjectNode()
        .set("data", mapper.createObjectNode()
            .put("type", "handle")
            .set("attributes", mapper.createObjectNode()
                .put("fdoProfile", fdoProperties.getProfile())
                .put("digitalObjectType", fdoProperties.getDigitalObjectType())
                .put("issuedForAgent", fdoProperties.getAgent())));
  }
}
