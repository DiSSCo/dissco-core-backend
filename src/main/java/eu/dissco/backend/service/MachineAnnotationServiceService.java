package eu.dissco.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.domain.MachineAnnotationServiceRecord;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.domain.jsonapi.JsonApiLinksFull;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiMeta;
import eu.dissco.backend.repository.MachineAnnotationServiceRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MachineAnnotationServiceService {

  private final MachineAnnotationServiceRepository repository;
  private final KafkaPublisherService kafkaPublisherService;
  private final ObjectMapper mapper;

  private static boolean checkIfMasComplies(JsonNode jsonNode,
      MachineAnnotationServiceRecord masRecord) {
    var filters = masRecord.mas().targetDigitalObjectFilters();
    var fields = filters.fields();
    var complies = true;
    while (fields.hasNext()) {
      var field = fields.next();
      var fieldKey = field.getKey();
      var valueList = new ArrayList<String>();
      field.getValue().elements().forEachRemaining(value -> valueList.add(value.asText()));
      var valueJson = jsonNode.findValue(fieldKey);
      if (valueJson == null || !valueJson.isTextual() || valueJson.asText().strip().equals("")) {
        complies = false;
      } else {
        var value = valueJson.asText();
        if (!valueList.contains("*") && !valueList.contains(value)) {
          complies = false;
        }
      }
    }
    return complies;
  }

  public JsonApiListResponseWrapper getMassForObject(JsonNode jsonNode, String path) {
    var availableMass = new ArrayList<JsonApiData>();
    var masRecords = repository.getAllMas();

    for (var masRecord : masRecords) {
      boolean complies = checkIfMasComplies(jsonNode, masRecord);
      if (complies) {
        availableMass.add(
            new JsonApiData(masRecord.id(), "MachineAnnotationService", masRecord, mapper));
      }
    }
    var links = new JsonApiLinksFull(path);
    return new JsonApiListResponseWrapper(availableMass, links,
        new JsonApiMeta(availableMass.size()));
  }

  public JsonApiListResponseWrapper scheduleMass(JsonNode flattenObjectData, List<String> mass,
      String path, Object object) {
    var masRecords = repository.getMasRecords(mass);
    var scheduledMasRecords = new ArrayList<JsonApiData>();
    for (var masRecord : masRecords) {
      if (checkIfMasComplies(flattenObjectData, masRecord)) {
        try {
          kafkaPublisherService.sendObjectToQueue(masRecord.mas().topicName(), object);
          scheduledMasRecords.add(
              new JsonApiData(masRecord.id(), "MachineAnnotationService", masRecord, mapper));
        } catch (JsonProcessingException e) {
          log.error("Failed to send object to kafka");
        }
      } else {
        log.warn("Requested massRecord: {} is not available for the object: {}", masRecord.id(),
            object);
      }
    }
    var links = new JsonApiLinksFull(path);
    return new JsonApiListResponseWrapper(scheduledMasRecords, links,
        new JsonApiMeta(scheduledMasRecords.size()));
  }
}
