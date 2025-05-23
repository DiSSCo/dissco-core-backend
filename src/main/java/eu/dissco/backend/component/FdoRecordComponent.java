package eu.dissco.backend.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.domain.FdoType;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@Component
public class FdoRecordComponent {

  private final JsonNode postRequest;

  public FdoRecordComponent(ObjectMapper mapper) {
    this.postRequest = mapper.createObjectNode()
        .set("data", mapper.createObjectNode()
            .put("type", FdoType.MJR.getPid())
            .set("attributes", mapper.createObjectNode()));
  }
}
