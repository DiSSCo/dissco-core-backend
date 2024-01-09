package eu.dissco.backend.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.properties.FdoProperties;
import lombok.Getter;

@Getter
public class FdoRecordBuilder {
  private final JsonNode postRequest;

  public FdoRecordBuilder(ObjectMapper mapper, FdoProperties fdoProperties) {
    this.postRequest = mapper.createObjectNode()
        .set("data", mapper.createObjectNode()
            .put("type", "handle")
            .set("attributes", mapper.createObjectNode()
                .put("fdoProfile", fdoProperties.getProfile())
                .put("digitalObjectType", fdoProperties.getDigitalObjectType())
                .put("issuedForAgent", fdoProperties.getAgent())));
  }
}
