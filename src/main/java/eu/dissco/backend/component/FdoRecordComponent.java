package eu.dissco.backend.component;

import static eu.dissco.backend.utils.HandleProxyUtils.removeProxy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import eu.dissco.backend.domain.FdoType;
import eu.dissco.backend.schema.VirtualCollectionRequest;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@Component
@AllArgsConstructor
public class FdoRecordComponent {

  private final ObjectMapper mapper;

  public JsonNode getPostRequest() {
    return mapper.createObjectNode()
        .set("data", mapper.createObjectNode()
            .put("type", FdoType.MJR.getPid())
            .set("attributes", mapper.createObjectNode()));
  }

  public JsonNode getPostRequest(VirtualCollectionRequest virtualCollection) {
    return mapper.createObjectNode()
        .set("data", mapper.createObjectNode()
            .put("type", FdoType.VIRTUAL_COLLECTION.getPid())
            .set("attributes", mapper.createObjectNode()
                .put("collectionName", virtualCollection.getLtcCollectionName())
                .put("basisOfScheme", virtualCollection.getLtcBasisOfScheme().value())));
  }

  public JsonNode getRollbackCreateRequest(String handle) {
    var dataNode = List.of(mapper.createObjectNode().put("id", removeProxy(handle)));
    ArrayNode dataArrayNode = mapper.valueToTree(dataNode);
    return mapper.createObjectNode().set("data", dataArrayNode);
  }
}
