package eu.dissco.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import eu.dissco.backend.database.jooq.enums.MjrTargetType;
import eu.dissco.backend.domain.MachineAnnotationServiceRecord;
import eu.dissco.backend.domain.MasTarget;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.domain.jsonapi.JsonApiLinksFull;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiMeta;
import eu.dissco.backend.exceptions.ConflictException;
import eu.dissco.backend.repository.MachineAnnotationServiceRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MachineAnnotationServiceService {

  private final MachineAnnotationServiceRepository repository;
  private final KafkaPublisherService kafkaPublisherService;
  private final MasJobRecordService mjrService;
  private final ObjectMapper mapper;

  private boolean checkIfMasComplies(JsonNode jsonNode,
      MachineAnnotationServiceRecord masRecord) {
    var filters = masRecord.mas().targetDigitalObjectFilters();
    var fields = filters.fields();
    var complies = true;
    while (fields.hasNext() && complies) {
      var field = fields.next();
      var fieldKey = field.getKey();
      var allowedValues = mapper.convertValue(field.getValue(), new TypeReference<List<Object>>() {
      });
      try {
        var values = JsonPath.read(jsonNode.toString(), fieldKey);
        if (values instanceof List<?>) {
          var valueList = (List<Object>) values;
          if (valueList.isEmpty() || (!allowedValues.contains("*") && !allowedValues.contains(
              valueList))) {
            complies = false;
          }
        } else if (values instanceof Object && !allowedValues.contains(values)) {
          complies = false;
        }
      } catch (PathNotFoundException e) {
        log.warn("Key: {} not found in json: {}", fieldKey, jsonNode);
        complies = false;
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
      String path, Object object, String targetId, String orcid, MjrTargetType targetType,
      boolean batchingRequested)
      throws ConflictException {
    var masRecords = repository.getMasRecords(mass);
    validateBatchingRequest(batchingRequested, masRecords);
    var scheduledMasRecords = new ArrayList<JsonApiData>();
    List<String> failedRecords = new ArrayList<>();
    var availableRecords = filterAvailableRecords(masRecords, flattenObjectData, object);
    var masRecordJobIds = mjrService.createMasJobRecord(availableRecords, targetId, orcid,
        targetType, batchingRequested);
    for (var masRecord : availableRecords) {
      try {
        var targetObject = new MasTarget(object, masRecordJobIds.get(masRecord.id()));
        kafkaPublisherService.sendObjectToQueue(masRecord.mas().topicName(), targetObject);
        scheduledMasRecords.add(
            new JsonApiData(masRecord.id(), "MachineAnnotationService", masRecord, mapper));
      } catch (JsonProcessingException e) {
        log.error("Failed to send masRecord: {}  to kafka", masRecord.id());
        failedRecords.add(masRecordJobIds.get(masRecord.id()));
      }
    }
    if (!failedRecords.isEmpty()) {
      mjrService.markMasJobRecordAsFailed(failedRecords);
    }
    var links = new JsonApiLinksFull(path);
    return new JsonApiListResponseWrapper(scheduledMasRecords, links,
        new JsonApiMeta(scheduledMasRecords.size()));
  }

  private void validateBatchingRequest(Boolean batchingRequested,
      List<MachineAnnotationServiceRecord> masRecords)
      throws ConflictException {
    if (Boolean.FALSE.equals(batchingRequested)) {
      return;
    }
    for (var masRecord : masRecords) {
      if (Boolean.FALSE.equals(masRecord.mas().batchingRequested())) {
        log.error(
            "User is attempting to schedule batch annotations with a mas that does not allow this. MAS id: {}",
            masRecord.id());
        throw new ConflictException();
      }
    }
  }

  private Set<MachineAnnotationServiceRecord> filterAvailableRecords(
      List<MachineAnnotationServiceRecord> masRecords, JsonNode flattenObjectData, Object object) {
    var availableRecords = new HashSet<MachineAnnotationServiceRecord>();
    for (var masRecord : masRecords) {
      if (checkIfMasComplies(flattenObjectData, masRecord)) {
        availableRecords.add(masRecord);
      } else {
        log.warn("Requested massRecords: {} are not available for the object: {}", masRecord.id(),
            object);
      }
    }
    return availableRecords;
  }

}
