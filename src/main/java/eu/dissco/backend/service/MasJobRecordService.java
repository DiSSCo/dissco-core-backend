package eu.dissco.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.database.jooq.enums.MjrJobState;
import eu.dissco.backend.database.jooq.enums.MjrTargetType;
import eu.dissco.backend.domain.MachineAnnotationServiceRecord;
import eu.dissco.backend.domain.MasJobRecord;
import eu.dissco.backend.domain.MasJobRecordFull;
import eu.dissco.backend.domain.MjrBatchMetadata;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.domain.jsonapi.JsonApiLinks;
import eu.dissco.backend.domain.jsonapi.JsonApiLinksFull;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.repository.MasJobRecordRepository;

import eu.dissco.backend.web.HandleComponent;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MasJobRecordService {

  private final MasJobRecordRepository masJobRecordRepository;
  private final HandleComponent handleComponent;
  private final ObjectMapper mapper;

  public JsonApiWrapper getMasJobRecordById(String masJobRecordHandle, String path)
      throws NotFoundException {
    var masJobRecordOptional = masJobRecordRepository.getMasJobRecordById(masJobRecordHandle);
    if (masJobRecordOptional.isEmpty()) {
      throw new NotFoundException(
          "Unable to find MAS Job Record for job " + masJobRecordHandle);
    }
    var masJobRecord = masJobRecordOptional.get();
    var dataNode = new JsonApiData(masJobRecordHandle, "masJobRecord",
        mapper.valueToTree(masJobRecord));
    return new JsonApiWrapper(dataNode, new JsonApiLinks(path));
  }

  public JsonApiListResponseWrapper getMasJobRecordByTargetId(String targetId,
      MjrJobState state, String path, int pageNum, int pageSize) throws NotFoundException {
    var pageSizePlusOne = pageSize + 1;
    var masJobRecordListPlusOne = masJobRecordRepository.getMasJobRecordsByTargetId(targetId, state,
        pageNum, pageSizePlusOne);
    if (masJobRecordListPlusOne.isEmpty()) {
      throw new NotFoundException("No MAS Jobs for " + targetId + " found");
    }
    return packageList(masJobRecordListPlusOne, path, pageNum, pageSize);
  }

  public JsonApiListResponseWrapper getMasJobRecordsByMasId(String masId, String path,
      int pageNum, int pageSize, MjrJobState state) {
    int pageSizeToCheckNext = pageSize + 1;
    List<MasJobRecordFull> masJobRecordsPlusOne;
    masJobRecordsPlusOne = masJobRecordRepository.getMasJobRecordsByMasId(masId,
        state, pageNum, pageSizeToCheckNext);
    return packageList(masJobRecordsPlusOne, path, pageNum, pageSize);
  }

  public JsonApiListResponseWrapper getMasJobRecordsByUserId(String orcid, String path,
      int pageNum, int pageSize, MjrJobState state) {
    int pageSizeToCheckNext = pageSize + 1;
    List<MasJobRecordFull> masJobRecordsPlusOne;
    masJobRecordsPlusOne = masJobRecordRepository.getMasJobRecordsByUserId(orcid, state,
        pageNum, pageSizeToCheckNext);
    return packageList(masJobRecordsPlusOne, path, pageNum, pageSize);
  }

  private JsonApiListResponseWrapper packageList(List<MasJobRecordFull> masJobRecordListPlusOne,
      String path, int pageNum, int pageSize) {
    boolean hasNext = masJobRecordListPlusOne.size() > pageSize;
    var sublist = hasNext ? masJobRecordListPlusOne.subList(0, pageSize) : masJobRecordListPlusOne;
    List<JsonApiData> dataList = sublist.stream().map(
            mjr -> new JsonApiData(mjr.jobHandle(), "masJobRecord", mapper.valueToTree(mjr)))
        .toList();
    JsonApiLinksFull linksNode;
    if (masJobRecordListPlusOne.isEmpty()) {
      linksNode = new JsonApiLinksFull(path);
    } else {
      linksNode = new JsonApiLinksFull(pageNum, pageSize, hasNext, path);
    }
    return new JsonApiListResponseWrapper(dataList, linksNode);
  }

  public Map<String, String> createMasJobRecord(Set<MachineAnnotationServiceRecord> masRecords,
      String targetId, String orcid, MjrTargetType targetType, JsonNode flattenObjectData,
      boolean allowBatch) {
    log.info("Requesting {} handles from API", masRecords.size());
    var handles = handleComponent.postHandle(masRecords.size());
    var handleItr = handles.iterator();
    var masJobRecordList = allowBatch ?
        buildMjrWithBatchMetadata(masRecords, handleItr, targetId, orcid, targetType, flattenObjectData) :
        buildMjrListNoBatchMetadata(masRecords, handleItr, targetId, orcid, targetType);
    masJobRecordRepository.createNewMasJobRecord(masJobRecordList);
    return masJobRecordList.stream()
        .collect(Collectors.toMap(MasJobRecord::masId, MasJobRecord::jobId));
  }

  private List<MasJobRecord> buildMjrListNoBatchMetadata(
      Set<MachineAnnotationServiceRecord> masRecords, Iterator<String> handleItr, String targetId,
      String orcid, MjrTargetType targetType) {
    return masRecords.stream()
        .map(masRecord -> new MasJobRecord(handleItr.next(), MjrJobState.SCHEDULED, masRecord.id(),
            targetId, targetType,
            orcid, null))
        .toList();
  }

  private List<MasJobRecord> buildMjrWithBatchMetadata(
      Set<MachineAnnotationServiceRecord> masRecords, Iterator<String> handleItr, String targetId,
      String orcid, MjrTargetType targetType, JsonNode flattenAttributes) {
    return masRecords.stream()
        .map(masRecord -> new MasJobRecord(handleItr.next(), MjrJobState.SCHEDULED, masRecord.id(),
            targetId, targetType,
            orcid, buildBatchMetadata(flattenAttributes, masRecord)))
        .toList();
  }

  private MjrBatchMetadata buildBatchMetadata(JsonNode flattenAttributes,
      MachineAnnotationServiceRecord masRecord) {
    var masInput = masRecord.mas().masInput();
    if (masInput == null) {
      return null;
    }
    var inputFields = List.of(masInput.targetField().split("-"));
    var inputValue = getTargetValueNode(flattenAttributes, inputFields).asText();
    return new MjrBatchMetadata(masInput.inputField(), inputValue, masInput.targetField());
  }

  private JsonNode getTargetValueNode(JsonNode flattenAttributes, List<String> targetField) {
    if (targetField.size() > 1) {
      var subfields = targetField.subList(1, targetField.size() - 1);
      return getTargetValueNode(flattenAttributes.get(targetField.get(0)), subfields);
    }
    return flattenAttributes.get(targetField.get(0));
  }

  public void markMasJobRecordAsRunning(String masId, String jobId) throws NotFoundException {
    if (masJobRecordRepository.markMasJobRecordAsRunning(masId, jobId) == 0) {
      throw new NotFoundException("Unable to locate scheduled MAS job with id " + jobId);
    }
  }

  public void markMasJobRecordAsFailed(List<String> failedJobIds) {
    masJobRecordRepository.markMasJobRecordsAsFailed(failedJobIds);
  }

}
