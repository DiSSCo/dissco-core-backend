package eu.dissco.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.database.jooq.enums.JobState;
import eu.dissco.backend.database.jooq.enums.MjrTargetType;
import eu.dissco.backend.domain.FdoType;
import eu.dissco.backend.domain.MasJobRecord;
import eu.dissco.backend.domain.MasJobRecordFull;
import eu.dissco.backend.domain.MasJobRequest;
import eu.dissco.backend.domain.annotation.AnnotationTargetType;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.domain.jsonapi.JsonApiLinks;
import eu.dissco.backend.domain.jsonapi.JsonApiLinksFull;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.repository.MasJobRecordRepository;
import eu.dissco.backend.schema.Annotation;
import eu.dissco.backend.schema.MachineAnnotationService;
import eu.dissco.backend.web.HandleComponent;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
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
  private static final Integer TTL = 86400;

  public JsonApiWrapper getMasJobRecordById(String masJobRecordHandle, String path)
      throws NotFoundException {
    var masJobRecordOptional = masJobRecordRepository.getMasJobRecordById(masJobRecordHandle);
    if (masJobRecordOptional.isEmpty()) {
      throw new NotFoundException(
          "Unable to find MAS Job Record for job " + masJobRecordHandle);
    }
    var masJobRecord = masJobRecordOptional.get();
    var dataNode = new JsonApiData(masJobRecordHandle, FdoType.MJR.getName(),
        mapper.valueToTree(masJobRecord));
    return new JsonApiWrapper(dataNode, new JsonApiLinks(path));
  }

  public JsonApiListResponseWrapper getMasJobRecordByTargetId(String targetId,
      JobState state, String path, int pageNum, int pageSize) throws NotFoundException {
    var pageSizePlusOne = pageSize + 1;
    var masJobRecordListPlusOne = masJobRecordRepository.getMasJobRecordsByTargetId(targetId, state,
        pageNum, pageSizePlusOne);
    if (masJobRecordListPlusOne.isEmpty()) {
      throw new NotFoundException("No MAS Jobs for " + targetId + " found");
    }
    return packageList(masJobRecordListPlusOne, path, pageNum, pageSize);
  }

  public JsonApiListResponseWrapper getMasJobRecordsByMasId(String masId, String path,
      int pageNum, int pageSize, JobState state) {
    int pageSizeToCheckNext = pageSize + 1;
    List<MasJobRecordFull> masJobRecordsPlusOne;
    masJobRecordsPlusOne = masJobRecordRepository.getMasJobRecordsByMasId(masId,
        state, pageNum, pageSizeToCheckNext);
    return packageList(masJobRecordsPlusOne, path, pageNum, pageSize);
  }

  private JsonApiListResponseWrapper packageList(List<MasJobRecordFull> masJobRecordListPlusOne,
      String path, int pageNum, int pageSize) {
    boolean hasNext = masJobRecordListPlusOne.size() > pageSize;
    var sublist = hasNext ? masJobRecordListPlusOne.subList(0, pageSize) : masJobRecordListPlusOne;
    List<JsonApiData> dataList = sublist.stream().map(
            mjr -> new JsonApiData(mjr.jobHandle(), FdoType.MJR.getName(), mapper.valueToTree(mjr)))
        .toList();
    JsonApiLinksFull linksNode;
    if (masJobRecordListPlusOne.isEmpty()) {
      linksNode = new JsonApiLinksFull(path);
    } else {
      linksNode = new JsonApiLinksFull(pageNum, pageSize, hasNext, path);
    }
    return new JsonApiListResponseWrapper(dataList, linksNode);
  }

  public Map<String, MasJobRecord> createMasJobRecord(
      Set<MachineAnnotationService> masRecords,
      String targetId, String orcid, MjrTargetType targetType,
      Map<String, MasJobRequest> masRequests) {
    log.info("Requesting {} handles from API", masRecords.size());
    var handles = handleComponent.postHandle(masRecords.size());
    var handleItr = handles.iterator();
    var masJobRecordList = masRecords.stream()
        .map(masRecord -> {
          var request = masRequests.get(masRecord.getId());
          return new MasJobRecord(
              handleItr.next(),
              JobState.SCHEDULED,
              masRecord.getId(),
              targetId,
              targetType,
              orcid,
              request.batching(),
              masRecord.getOdsTimeToLive()
          );
        })
        .toList();
    masJobRecordRepository.createNewMasJobRecord(masJobRecordList);
    return masJobRecordList.stream()
        .collect(Collectors.toMap(MasJobRecord::masId, Function.identity()));
  }

  public String createJobRecordForDisscover(Annotation annotation, String orcid) {
    var handle = handleComponent.postHandle(1).get(0);
    var mjr = new MasJobRecord(
        handle,
        JobState.RUNNING,
        "DISSCOVER",
        annotation.getOaHasTarget().getId(),
        getMjrTargetType(annotation),
        orcid,
        true,
        TTL
    );
    masJobRecordRepository.createNewMasJobRecord(List.of(mjr));
    return handle;
  }

  public void markMasJobRecordAsRunning(String masId, String jobId) throws NotFoundException {
    if (masJobRecordRepository.markMasJobRecordAsRunning(masId, jobId) == 0) {
      throw new NotFoundException("Unable to locate scheduled MAS job with id " + jobId);
    }
  }

  public void markMasJobRecordAsFailed(List<String> failedJobIds) {
    masJobRecordRepository.markMasJobRecordsAsFailed(failedJobIds);
  }

  private MjrTargetType getMjrTargetType(Annotation annotation){
    var targetType = AnnotationTargetType.fromString(annotation.getOaHasTarget().getOdsFdoType());
    if (AnnotationTargetType.DIGITAL_SPECIMEN.equals(targetType)){
      return MjrTargetType.DIGITAL_SPECIMEN;
    }
    return MjrTargetType.MEDIA_OBJECT;
  }

}
