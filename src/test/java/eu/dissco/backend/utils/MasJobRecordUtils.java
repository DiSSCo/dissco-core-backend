package eu.dissco.backend.utils;

import static eu.dissco.backend.TestUtils.CREATED;
import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.ID_ALT;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.ORCID;

import eu.dissco.backend.domain.AnnotationState;
import eu.dissco.backend.domain.MasJobRecordFull;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.domain.jsonapi.JsonApiLinksFull;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MasJobRecordUtils {

  public static final UUID JOB_ID = UUID.fromString("58e0a7d7-eebc-11d8-9669-0800200c9a66");
  public static final String JOB_HANDLE = "20.5000.1025/TR9-6D6-Z4A";
  public static final String JOB_HANDLE_ALT = "20.5000.1025/P3D-22A-MRY";
  public static final UUID JOB_ID_ALT = UUID.fromString("60e0a7d7-eebc-11d8-9669-0800200c9a66");

  public static final String MJR_URI = "/api/v1/mjr/";


  public static Map<String, String> givenMasJobRecordIdMap(String creatorId) {
    var jobIdMap = new HashMap<String, String>();
    jobIdMap.put(creatorId, JOB_HANDLE);
    return jobIdMap;
  }

  public static MasJobRecordFull givenMasJobRecordFullScheduled() {
    return new MasJobRecordFull(
        AnnotationState.SCHEDULED,
        ID_ALT,
        ID,
        ORCID,
        JOB_HANDLE,
        CREATED,
        null,
        MAPPER.createObjectNode()
    );
  }

  public static MasJobRecordFull givenMasJobRecordFullCompleted() {
    return givenMasJobRecordFullCompleted(ID_ALT);
  }

  public static MasJobRecordFull givenMasJobRecordFullCompleted(String creator) {
    return new MasJobRecordFull(
        AnnotationState.COMPLETED,
        creator,
        ID,
        ORCID,
        JOB_HANDLE_ALT,
        CREATED,
        CREATED,
        MAPPER.createObjectNode()
    );
  }

  public static JsonApiListResponseWrapper givenMjrListResponse(int pageSize, int pageNum, boolean hasNext){
    var linksNode = new JsonApiLinksFull(pageSize, pageNum, hasNext, MJR_URI);
    var mjr = givenMasJobRecordFullScheduled();
    var dataList = Collections.nCopies(pageSize,
        new JsonApiData(JOB_HANDLE, "masJobRecord", MAPPER.valueToTree(mjr)));
    return new JsonApiListResponseWrapper(dataList, linksNode);
  }


}
