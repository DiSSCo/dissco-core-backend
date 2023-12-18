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

import eu.dissco.backend.web.HandleComponent;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
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
      AnnotationState state, String path, int pageNum, int pageSize) throws NotFoundException {
    var pageSizePlusOne = pageSize + 1;
    var masJobRecordListPlusOne = masJobRecordRepository.getMasJobRecordsByTargetId(targetId, state,
        pageNum, pageSizePlusOne);
    if (masJobRecordListPlusOne.isEmpty()) {
      throw new NotFoundException("No MAS Jobs for " + targetId + " found");
    }
    return packageList(masJobRecordListPlusOne, path, pageNum, pageSize);
  }

  public JsonApiListResponseWrapper getMasJobRecordsByCreator(String creatorId, String path,
      int pageNum, int pageSize, AnnotationState state) {
    int pageSizeToCheckNext = pageSize + 1;
    List<MasJobRecordFull> masJobRecordsPlusOne;
    masJobRecordsPlusOne = masJobRecordRepository.getMasJobRecordsByCreatorId(creatorId,
        state, pageNum, pageSizeToCheckNext);
    return packageList(masJobRecordsPlusOne, path, pageNum, pageSize);
  }

  public JsonApiListResponseWrapper getMasJobRecordsByUserId(String orcid, String path,
      int pageNum, int pageSize, AnnotationState state) {
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
      linksNode = new JsonApiLinksFull(pageSize, pageNum, hasNext, path);
    }
    return new JsonApiListResponseWrapper(dataList, linksNode);
  }

  public Map<String, String> createMasJobRecord(Set<MachineAnnotationServiceRecord> masRecords,
      String targetId, String orcid) {
    var handles = handleComponent.postHandle(masRecords.size()).iterator();
    var masJobRecordList = masRecords.stream()
        .map(masRecord -> new MasJobRecord(handles.next(), AnnotationState.SCHEDULED, masRecord.id(), targetId,
            orcid))
        .toList();
    return masJobRecordRepository.createNewMasJobRecord(masJobRecordList); // Map<Mas Id, Job Id>
  }

  public void markMasJobRecordAsRunning(String creatorId, String masJobHandle) throws NotFoundException {
    if (masJobRecordRepository.markMasJobRecordAsRunning(creatorId, masJobHandle) == 0) {
      throw new NotFoundException("Unable to locate scheduled MAS job with id " + masJobHandle);
    }
  }

  public void markMasJobRecordAsFailed(List<String> failedJobIds) {
    masJobRecordRepository.markMasJobRecordsAsFailed(failedJobIds);
  }

}
