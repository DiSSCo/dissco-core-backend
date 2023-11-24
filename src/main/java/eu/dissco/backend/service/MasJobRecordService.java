package eu.dissco.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.domain.AnnotationState;
import eu.dissco.backend.domain.MachineAnnotationServiceRecord;
import eu.dissco.backend.domain.MasJobRecord;
import eu.dissco.backend.domain.MasJobRecordFull;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.domain.jsonapi.JsonApiLinks;
import eu.dissco.backend.domain.jsonapi.JsonApiLinksFull;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.repository.MasJobRecordRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MasJobRecordService {

  private final MasJobRecordRepository masJobRecordRepository;
  private final ObjectMapper mapper;

  public JsonApiWrapper getMasJobRecordById(UUID masJobRecordId, String path)
      throws NotFoundException {
    var masJobRecordOptional = masJobRecordRepository.getMasJobRecordById(masJobRecordId);
    if (masJobRecordOptional.isEmpty()) {
      throw new NotFoundException(
          "Unable to find MAS Job Record for job " + masJobRecordId.toString());
    }
    var masJobRecord = masJobRecordOptional.get();
    var dataNode = new JsonApiData(masJobRecordId.toString(), "masJobRecord",
        mapper.valueToTree(masJobRecord));
    return new JsonApiWrapper(dataNode, new JsonApiLinks(path));
  }

  public JsonApiListResponseWrapper getMasJobRecordByTargetId(String targetId,
      AnnotationState state, String path, int pageNum, int pageSize) throws NotFoundException {
    var pageSizePlusOne = pageSize + 1;
    List<MasJobRecordFull> masJobRecordListPlusOne;

    if (state == null) {
      masJobRecordListPlusOne = masJobRecordRepository.getMasJobRecordsByTargetId(targetId, pageNum,
          pageSizePlusOne);
    } else {
      masJobRecordListPlusOne = masJobRecordRepository.getMasJobRecordsByTargetIdAndState(targetId,
          state.getState(), pageNum, pageSizePlusOne);
    }
    if (masJobRecordListPlusOne.isEmpty()) {
      throw new NotFoundException("No MAS Jobs for " + targetId + " found");
    }
    return packageList(masJobRecordListPlusOne, path, pageNum, pageSize);
  }

  public JsonApiListResponseWrapper getMasJobRecordsByCreator(String creatorId, String path,
      int pageNum, int pageSize, AnnotationState state) {
    int pageSizeToCheckNext = pageSize + 1;
    List<MasJobRecordFull> masJobRecordsPlusOne;
    if (state == null) {
      masJobRecordsPlusOne = masJobRecordRepository.getMasJobRecordsByCreator(creatorId, pageNum,
          pageSizeToCheckNext);
    } else {
      masJobRecordsPlusOne = masJobRecordRepository.getMasJobRecordsByCreatorAndState(creatorId,
          state.getState(), pageNum, pageSizeToCheckNext);
    }
    return packageList(masJobRecordsPlusOne, path, pageNum, pageSize);
  }

  private JsonApiListResponseWrapper packageList(List<MasJobRecordFull> masJobRecordListPlusOne,
      String path, int pageNum, int pageSize) {
    boolean hasNext = masJobRecordListPlusOne.size() > pageSize;
    var sublist = hasNext ? masJobRecordListPlusOne.subList(0, pageSize) : masJobRecordListPlusOne;
    List<JsonApiData> dataList = sublist.stream().map(
            mjr -> new JsonApiData(mjr.jobId().toString(), "masJobRecord", mapper.valueToTree(mjr)))
        .toList();
    JsonApiLinksFull linksNode;
    if (masJobRecordListPlusOne.isEmpty()) {
      linksNode = new JsonApiLinksFull(path);
    } else {
      linksNode = new JsonApiLinksFull(pageSize, pageNum, hasNext, path);
    }
    return new JsonApiListResponseWrapper(dataList, linksNode);
  }

  public Map<String, UUID> createMasJobRecord(Set<MachineAnnotationServiceRecord> masRecords,
      String targetId, String orcid) {
    var masJobRecordList = masRecords.stream()
        .map(masRecord -> new MasJobRecord(AnnotationState.SCHEDULED, masRecord.id(), targetId, orcid))
        .toList();
    return masJobRecordRepository.createNewMasJobRecord(masJobRecordList);
  }

  public void markMasJobRecordAsFailed(List<UUID> failedJobIds) {
    masJobRecordRepository.markMasJobRecordsAsFailed(failedJobIds);
  }

}
