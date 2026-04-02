package eu.dissco.backend.component;

import static eu.dissco.backend.utils.ProxyUtils.removeHandleProxy;

import eu.dissco.backend.domain.FdoType;
import eu.dissco.backend.schema.VirtualCollection;
import eu.dissco.backend.schema.VirtualCollectionRequest;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

@Getter
@Component
@AllArgsConstructor
public class FdoRecordComponent {

	private static final String ATTRIBUTES = "attributes";

	private static final String TYPE = "type";

	private static final String DATA = "data";

	private static final String ID = "id";

	private final JsonMapper mapper;

	public JsonNode getPostRequestMjr() {
		return mapper.createObjectNode()
			.set(DATA,
					mapper.createObjectNode()
						.put(TYPE, FdoType.MJR.getPid())
						.set(ATTRIBUTES, mapper.createObjectNode()));
	}

	public JsonNode getPostRequestVirtualCollection(VirtualCollectionRequest virtualCollection) {
		return mapper.createObjectNode()
			.set(DATA,
					mapper.createObjectNode()
						.put(TYPE, FdoType.VIRTUAL_COLLECTION.getPid())
						.set(ATTRIBUTES, getAttributes(virtualCollection.getLtcCollectionName(),
								virtualCollection.getLtcBasisOfScheme().value())));
	}

	private ObjectNode getAttributes(String name, String basisOfScheme) {
		return mapper.createObjectNode().put("collectionName", name).put("basisOfScheme", basisOfScheme);
	}

	public JsonNode getRollbackCreateRequest(String handle) {
		var dataNode = List.of(mapper.createObjectNode().put("id", removeHandleProxy(handle)));
		ArrayNode dataArrayNode = mapper.valueToTree(dataNode);
		return mapper.createObjectNode().set("data", dataArrayNode);
	}

	public JsonNode getPatchHandleRequest(VirtualCollection virtualCollection) {
		return mapper.createObjectNode()
			.set(DATA,
					mapper.createObjectNode()
						.put(ID, removeHandleProxy(virtualCollection.getId()))
						.put(TYPE, FdoType.VIRTUAL_COLLECTION.getPid())
						.set(ATTRIBUTES, getAttributes(virtualCollection.getLtcCollectionName(),
								virtualCollection.getLtcBasisOfScheme().value())));
	}

	public JsonNode getTombstoneRequest(String handle) {
		return mapper.createObjectNode()
			.set(DATA, mapper.createObjectNode()
				.put(ID, removeHandleProxy(handle))
				.put(TYPE, FdoType.VIRTUAL_COLLECTION.getPid())
				.set(ATTRIBUTES, mapper.createObjectNode()
					.put("tombstoneText",
							FdoType.VIRTUAL_COLLECTION.getName() + " tombstoned by agent through the dissco backend")));
	}

}
