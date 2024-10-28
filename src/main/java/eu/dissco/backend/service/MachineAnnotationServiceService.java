package eu.dissco.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import eu.dissco.backend.database.jooq.enums.MjrTargetType;
import eu.dissco.backend.domain.MasJobRecord;
import eu.dissco.backend.domain.MasJobRequest;
import eu.dissco.backend.domain.MasTarget;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.domain.jsonapi.JsonApiLinksFull;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiMeta;
import eu.dissco.backend.exceptions.BatchingNotPermittedException;
import eu.dissco.backend.exceptions.ConflictException;
import eu.dissco.backend.repository.MachineAnnotationServiceRepository;
import eu.dissco.backend.schema.MachineAnnotationService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
      MachineAnnotationService machineAnnotationService) {
    var filters = machineAnnotationService.getOdsTargetDigitalObjectFilter();
    var fields = filters.getAdditionalProperties();
    var complies = true;
    for (var stringObjectEntry : fields.entrySet()) {
      var allowedValues = (List<Object>) stringObjectEntry.getValue();
      var fieldKey = stringObjectEntry.getKey();
      try {
        var values = JsonPath.read(jsonNode.toString(), fieldKey);
        if (values instanceof List<?>) {
          var valueList = (List<Object>) values;
          if (valueList.isEmpty() || (!allowedValues.contains("*") && !allowedValues.contains(
              valueList))) {
            complies = false;
          }
        } else if (values instanceof Object && (!allowedValues.contains(values) && !allowedValues.contains("*"))) {
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
            new JsonApiData(masRecord.getId(), "ods:MachineAnnotationService", masRecord, mapper));
      }
    }
    var links = new JsonApiLinksFull(path);
    return new JsonApiListResponseWrapper(availableMass, links,
        new JsonApiMeta(availableMass.size()));
  }

  public JsonApiListResponseWrapper scheduleMass(JsonNode flattenObjectData,
      Map<String, MasJobRequest> masRequests, String path, Object object, String targetId,
      String orcid, MjrTargetType targetType) throws ConflictException {
    var machineAnnotationServices = repository.getMasRecords(masRequests.keySet());
    validateBatchingRequest(masRequests, machineAnnotationServices);
    var scheduledJobs = new ArrayList<JsonApiData>();
    List<String> failedRecords = new ArrayList<>();
    var availableRecords = filterAvailableRecords(machineAnnotationServices, flattenObjectData, object);
    Map<String, MasJobRecord> masJobRecordIdMap = null;
    if (!availableRecords.isEmpty()) {
      masJobRecordIdMap = mjrService.createMasJobRecord(availableRecords, targetId, orcid,
          targetType, masRequests);
    }
    for (var machineAnnotationService : availableRecords) {
      var mjr = masJobRecordIdMap.get(machineAnnotationService.getId());
      try {
        var targetObject = new MasTarget(object, mjr.jobId(),
            masRequests.get(machineAnnotationService.getId()).batching());
        kafkaPublisherService.sendObjectToQueue(machineAnnotationService.getOdsTopicName(), targetObject);
        scheduledJobs.add(
            new JsonApiData(mjr.jobId(), "MachineAnnotationServiceJobRecord", mjr, mapper));
      } catch (JsonProcessingException e) {
        log.error("Failed to send masRecord: {}  to kafka", machineAnnotationService.getId());
        failedRecords.add(mjr.jobId());
      }
    }
    if (!failedRecords.isEmpty()) {
      mjrService.markMasJobRecordAsFailed(failedRecords);
    }
    var links = new JsonApiLinksFull(path);
    return new JsonApiListResponseWrapper(scheduledJobs, links,
        new JsonApiMeta(scheduledJobs.size()));
  }

  private void validateBatchingRequest(Map<String, MasJobRequest> mass,
      List<MachineAnnotationService> masRecords) throws ConflictException {
    for (var masRecord : masRecords) {
      var batchingRequested = mass.get(masRecord.getId()).batching();
      if (Boolean.FALSE.equals(batchingRequested)) {
        return;
      }
      if (Boolean.FALSE.equals(masRecord.getOdsBatchingPermitted())) {
        log.error(
            "User is attempting to schedule batch annotationRequests with a mas that does not allow this. MAS id: {}",
            masRecord.getId());
        throw new BatchingNotPermittedException();
      }
    }
  }

  private Set<MachineAnnotationService> filterAvailableRecords(
      List<MachineAnnotationService> masRecords, JsonNode flattenObjectData, Object object) {
    var availableRecords = new HashSet<MachineAnnotationService>();
    for (var masRecord : masRecords) {
      if (checkIfMasComplies(flattenObjectData, masRecord)) {
        availableRecords.add(masRecord);
      } else {
        log.warn("Requested massRecords: {} are not available for the object: {}", masRecord.getId(),
            object);
      }
    }
    return availableRecords;
  }

}
