package eu.dissco.backend.utils;

import eu.dissco.backend.database.jooq.Public;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MasJobRecordUtils {

  public static final UUID JOB_ID = UUID.fromString("58e0a7d7-eebc-11d8-9669-0800200c9a66");

  public static Map<String, UUID> givenMasJobRecordIdMap(String creatorId) {
    var jobIdMap = new HashMap<String, UUID>();
    jobIdMap.put(creatorId, JOB_ID);
    return jobIdMap;
  }

}
