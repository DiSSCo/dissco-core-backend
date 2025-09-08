package eu.dissco.backend.service;

import static eu.dissco.backend.utils.ProxyUtils.DOI_PROXY;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import eu.dissco.backend.client.MasClient;
import eu.dissco.backend.database.jooq.enums.MjrTargetType;
import eu.dissco.backend.domain.FdoType;
import eu.dissco.backend.domain.MasJobRecord;
import eu.dissco.backend.domain.MasJobRequest;
import eu.dissco.backend.domain.MasScheduleJobRequest;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.domain.jsonapi.JsonApiLinksFull;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiMeta;
import eu.dissco.backend.exceptions.MasSchedulingException;
import eu.dissco.backend.repository.MachineAnnotationServiceRepository;
import eu.dissco.backend.schema.MachineAnnotationService;
import feign.FeignException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MachineAnnotationServiceService {

  private final MachineAnnotationServiceRepository repository;
  private final ObjectMapper mapper;
  private final MasClient masClient;

  private boolean checkIfMasComplies(JsonNode jsonNode,
      MachineAnnotationService machineAnnotationService) {
    var filters = machineAnnotationService.getOdsHasTargetDigitalObjectFilter();
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
        } else if (values instanceof Object && (!allowedValues.contains(values)
            && !allowedValues.contains("*"))) {
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
            new JsonApiData(masRecord.getId(), FdoType.MAS.getName(), masRecord, mapper));
      }
    }
    var links = new JsonApiLinksFull(path);
    return new JsonApiListResponseWrapper(availableMass, links,
        new JsonApiMeta(availableMass.size()));
  }

  public JsonApiListResponseWrapper scheduleMas(String targetId, List<MasJobRequest> masRequests,
      String orcid,
      MjrTargetType targetType, String path)
      throws MasSchedulingException {
    var masScheduleJobRequests = masRequests.stream()
        .map(masRequest -> new MasScheduleJobRequest(
            masRequest.masId(),
            DOI_PROXY + targetId,
            masRequest.batching(),
            orcid,
            targetType
        )).collect(Collectors.toSet());
    try {
      var result = masClient.scheduleMas(masScheduleJobRequests);
      return formatMasScheduleResponse(mapper.treeToValue(result, new TypeReference<>() {
      }), path);
    } catch (FeignException e) {
      log.error("An error has occurred with MAS Scheduler Client", e);
      var msg = e.contentUTF8().isBlank() ? e.getMessage() : e.contentUTF8();
      throw new MasSchedulingException(msg);
    } catch (JsonProcessingException e) {
      log.error("Unable to read response from mas scheduler");
      throw new MasSchedulingException("Unable to read response from mas scheduler");
    }
  }

  private JsonApiListResponseWrapper formatMasScheduleResponse(List<MasJobRecord> masJobRecords,
      String path) {
    var dataNode = masJobRecords.stream().map(
        mjr -> new JsonApiData(
            mjr.jobId(),
            "MachineAnnotationServiceJobRecord",
            mjr,
            mapper
        )).toList();
    return new JsonApiListResponseWrapper(dataNode, new JsonApiLinksFull(path));
  }

}
