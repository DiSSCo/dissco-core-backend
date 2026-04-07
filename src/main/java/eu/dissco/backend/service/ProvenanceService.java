package eu.dissco.backend.service;

import static eu.dissco.backend.schema.Agent.Type.PROV_PERSON;
import static eu.dissco.backend.utils.AgentUtils.createAgent;
import static eu.dissco.backend.utils.AgentUtils.createServiceAgent;

import com.flipkart.zjsonpatch.Jackson3JsonDiff;
import eu.dissco.backend.properties.ApplicationProperties;
import eu.dissco.backend.schema.Agent;
import eu.dissco.backend.schema.CreateUpdateTombstoneEvent;
import eu.dissco.backend.schema.OdsChangeValue;
import eu.dissco.backend.schema.ProvActivity;
import eu.dissco.backend.schema.ProvEntity;
import eu.dissco.backend.schema.ProvValue;
import eu.dissco.backend.schema.ProvWasAssociatedWith;
import eu.dissco.backend.schema.ProvWasAssociatedWith.ProvHadRole;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Service
@RequiredArgsConstructor
public class ProvenanceService {

	private final JsonMapper mapper;

	private final ApplicationProperties properties;

	private static String getRdfsComment(ProvActivity.Type activityType) {
		switch (activityType) {
			case ODS_CREATE -> {
				return "Object newly created";
			}
			case ODS_UPDATE -> {
				return "Object updated";
			}
			case ODS_TOMBSTONE -> {
				return "Object tombstoned";
			}
		}
		return null;
	}

	public CreateUpdateTombstoneEvent generateCreateEvent(JsonNode digitalObject, Agent agent) {
		return generateCreateUpdateTombStoneEvent(digitalObject, ProvActivity.Type.ODS_CREATE, null, agent);
	}

	public CreateUpdateTombstoneEvent generateTombstoneEvent(JsonNode tombstoneObject, JsonNode currentObject,
			Agent agent) {
		var patch = createJsonPatch(tombstoneObject, currentObject);
		return generateCreateUpdateTombStoneEvent(tombstoneObject, ProvActivity.Type.ODS_TOMBSTONE, patch, agent);
	}

	private CreateUpdateTombstoneEvent generateCreateUpdateTombStoneEvent(JsonNode digitalObject,
			ProvActivity.Type activityType, JsonNode jsonPatch, Agent agent) {
		var entityID = digitalObject.get("@id").asString() + "/" + digitalObject.get("schema:version").asString();
		var activityID = UUID.randomUUID().toString();
		return new CreateUpdateTombstoneEvent().withId(entityID)
			.withType("ods:CreateUpdateTombstoneEvent")
			.withDctermsIdentifier(entityID)
			.withOdsFdoType(properties.getCreateUpdateTombstoneEventType())
			.withProvActivity(new ProvActivity().withId(activityID)
				.withType(activityType)
				.withOdsChangeValue(mapJsonPatch(jsonPatch))
				.withProvEndedAtTime(Date.from(Instant.now()))
				.withProvWasAssociatedWith(List.of(
						new ProvWasAssociatedWith().withId(agent.getId()).withProvHadRole(ProvHadRole.REQUESTOR),
						new ProvWasAssociatedWith().withId(agent.getId()).withProvHadRole(ProvHadRole.APPROVER),
						new ProvWasAssociatedWith().withId(properties.getPid()).withProvHadRole(ProvHadRole.GENERATOR)))
				.withProvUsed(entityID)
				.withRdfsComment(getRdfsComment(activityType)))
			.withProvEntity(new ProvEntity().withId(entityID)
				.withType(digitalObject.get("@type").asString())
				.withProvValue(mapEntityToProvValue(digitalObject))
				.withProvWasGeneratedBy(activityID))
			.withOdsHasAgents(
					List.of(createAgent(agent.getSchemaName(), agent.getId(), "creator", "orcid", PROV_PERSON),
							createServiceAgent(properties)));
	}

	private List<OdsChangeValue> mapJsonPatch(JsonNode jsonPatch) {
		if (jsonPatch == null) {
			return null;
		}
		return mapper.convertValue(jsonPatch, new TypeReference<>() {
		});
	}

	public CreateUpdateTombstoneEvent generateUpdateEvent(JsonNode digitalObject, JsonNode currentDigitalObject,
			Agent agent) {
		var jsonPatch = createJsonPatch(digitalObject, currentDigitalObject);
		return generateCreateUpdateTombStoneEvent(digitalObject, ProvActivity.Type.ODS_UPDATE, jsonPatch, agent);
	}

	private ProvValue mapEntityToProvValue(JsonNode jsonNode) {
		var provValue = new ProvValue();
		var node = mapper.convertValue(jsonNode, new TypeReference<Map<String, Object>>() {
		});
		for (var entry : node.entrySet()) {
			provValue.setAdditionalProperty(entry.getKey(), entry.getValue());
		}
		return provValue;
	}

	private JsonNode createJsonPatch(JsonNode digitalObject, JsonNode currentDigitalObject) {
		return Jackson3JsonDiff.asJson(currentDigitalObject, digitalObject);
	}

}
