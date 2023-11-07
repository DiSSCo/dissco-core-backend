package eu.dissco.backend.utils;

import static eu.dissco.backend.TestUtils.CREATED;
import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.ID_ALT;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.ORCID;

import eu.dissco.backend.domain.AnnotationState;
import eu.dissco.backend.domain.MasJobRecordFull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MasJobRecordUtils {

  public static final UUID JOB_ID = UUID.fromString("58e0a7d7-eebc-11d8-9669-0800200c9a66");
  public static final UUID JOB_ID_ALT = UUID.fromString("60e0a7d7-eebc-11d8-9669-0800200c9a66");

  public static final String MJR_URI = "/api/v1/mjr/";


  public static Map<String, UUID> givenMasJobRecordIdMap(String creatorId) {
    var jobIdMap = new HashMap<String, UUID>();
    jobIdMap.put(creatorId, JOB_ID);
    return jobIdMap;
  }

  public static MasJobRecordFull givenMasJobRecordFullScheduled() {
    return new MasJobRecordFull(
        AnnotationState.SCHEDULED,
        ORCID,
        ID,
        JOB_ID,
        CREATED,
        null,
        MAPPER.createObjectNode()
    );
  }

  public static MasJobRecordFull givenMasJobRecordFullCompleted() {
    return new MasJobRecordFull(
        AnnotationState.COMPLETED,
        ID_ALT,
        ID,
        JOB_ID_ALT,
        CREATED,
        CREATED,
        MAPPER.createObjectNode()
    );
  }


}
