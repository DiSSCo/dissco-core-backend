package eu.dissco.backend.utils;

import static eu.dissco.backend.TestUtils.CREATED;
import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.ID_ALT;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.ORCID;

import eu.dissco.backend.domain.MasJobRecordFull;
import eu.dissco.backend.domain.MasJobState;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.domain.jsonapi.JsonApiLinksFull;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MasJobRecordUtils {

  public static final String JOB_ID = "20.5000.1025/TR9-6D6-Z4A";
  public static final String JOB_ID_ALT = "20.5000.1025/P3D-22A-MRY";
  public static final String MJR_URI = "/api/v1/mjr/";


  public static Map<String, String> givenMasJobRecordIdMap(String creatorId) {
    var jobIdMap = new HashMap<String, String>();
    jobIdMap.put(creatorId, JOB_ID);
    return jobIdMap;
  }

  public static MasJobRecordFull givenMasJobRecordFullScheduled() {
    return new MasJobRecordFull(
        MasJobState.SCHEDULED,
        ID_ALT,
        ID,
        ORCID,
        JOB_ID,
        CREATED,
        null,
        null
    );
  }

  public static MasJobRecordFull givenMasJobRecordFullCompleted() {
    return givenMasJobRecordFullCompleted(ID_ALT);
  }

  public static MasJobRecordFull givenMasJobRecordFullCompleted(String creator) {
    return new MasJobRecordFull(
        MasJobState.COMPLETED,
        creator,
        ID,
        ORCID,
        JOB_ID_ALT,
        CREATED,
        CREATED,
        MAPPER.createObjectNode().put("annotation", "value")
    );
  }

  public static JsonApiListResponseWrapper givenMjrListResponse(int pageSize, int pageNum, boolean hasNext){
    var linksNode = new JsonApiLinksFull(pageSize, pageNum, hasNext, MJR_URI);
    var mjr = givenMasJobRecordFullScheduled();
    var dataList = Collections.nCopies(pageSize,
        new JsonApiData(JOB_ID, "masJobRecord", MAPPER.valueToTree(mjr)));
    return new JsonApiListResponseWrapper(dataList, linksNode);
  }


}
