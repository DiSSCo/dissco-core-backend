package eu.dissco.backend.utils;

import static eu.dissco.backend.TestUtils.CREATED;
import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.ID_ALT;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.ORCID;

import eu.dissco.backend.database.jooq.enums.MjrJobState;
import eu.dissco.backend.database.jooq.enums.MjrTargetType;
import eu.dissco.backend.domain.MasJobRecord;
import eu.dissco.backend.domain.MasJobRecordFull;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.domain.jsonapi.JsonApiLinksFull;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MasJobRecordUtils {

  public static final String JOB_ID = "20.5000.1025/TR9-6D6-Z4A";
  public static final String JOB_SUFFIX = "TR9-6D6-Z4A";
  public static final String JOB_ID_ALT = "20.5000.1025/P3D-22A-MRY";
  public static final String MJR_URI = "/api/v1/mjr/";
  public static Integer TTL_DEFAULT = 86400;


  public static Map<String, MasJobRecord> givenMasJobRecordIdMap(String masId){
    return givenMasJobRecordIdMap(masId, false);
  }
  public static Map<String, MasJobRecord> givenMasJobRecordIdMap(String masId, boolean batching) {
    var jobIdMap = new HashMap<String, MasJobRecord>();
    jobIdMap.put(masId, givenMasJobRecord(batching));
    return jobIdMap;
  }

  public static MasJobRecordFull givenMasJobRecordFullScheduled() {
    return givenMasJobRecordFullScheduled(MjrTargetType.DIGITAL_SPECIMEN);
  }

  public static MasJobRecordFull givenMasJobRecordFullScheduled(MjrTargetType targetType) {
    return new MasJobRecordFull(
        MjrJobState.SCHEDULED,
        ID_ALT,
        ID,
        targetType,
        ORCID,
        JOB_ID,
        CREATED,
        null,
        null,
        false,
        TTL_DEFAULT
    );
  }


  public static MasJobRecordFull givenMasJobRecordFullCompleted() {
    return givenMasJobRecordFullCompleted(ID_ALT);
  }

  public static MasJobRecordFull givenMasJobRecordFullCompleted(String creator) {
    return new MasJobRecordFull(
        MjrJobState.COMPLETED,
        creator,
        ID,
        MjrTargetType.DIGITAL_SPECIMEN,
        ORCID,
        JOB_ID_ALT,
        CREATED,
        CREATED,
        MAPPER.createObjectNode().put("annotation", "value"),
        false,
        TTL_DEFAULT
    );
  }

  public static MasJobRecord givenMasJobRecord(){
    return givenMasJobRecord(false);
  }

  public static MasJobRecord givenMasJobRecord(boolean batching){
    return new MasJobRecord(
        JOB_ID,
        MjrJobState.SCHEDULED,
        ID,
        ID_ALT,
        MjrTargetType.DIGITAL_SPECIMEN,
        ORCID,
        batching,
        TTL_DEFAULT
    );
  }

  public static JsonApiListResponseWrapper givenMjrListResponse(int pageSize, int pageNum, boolean hasNext){
    var linksNode = new JsonApiLinksFull(pageNum, pageSize, hasNext, MJR_URI);
    var mjr = givenMasJobRecordFullScheduled();
    var dataList = Collections.nCopies(pageSize,
        new JsonApiData(JOB_ID, "masJobRecord", MAPPER.valueToTree(mjr)));
    return new JsonApiListResponseWrapper(dataList, linksNode);
  }


}
