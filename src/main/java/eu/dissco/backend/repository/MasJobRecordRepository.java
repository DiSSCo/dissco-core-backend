package eu.dissco.backend.repository;

import static eu.dissco.backend.database.jooq.Tables.MAS_JOB_RECORD_TMP;
import static eu.dissco.backend.database.jooq.Tables.USER;
import static eu.dissco.backend.repository.RepositoryUtils.getOffset;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.database.jooq.enums.MjrJobState;
import eu.dissco.backend.domain.MasJobRecord;
import eu.dissco.backend.domain.MasJobRecordFull;
import eu.dissco.backend.exceptions.DisscoJsonBMappingException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MasJobRecordRepository {

  private final DSLContext context;
  private final ObjectMapper mapper;

  public Optional<MasJobRecordFull> getMasJobRecordById(String masJobRecordHandle) {
    return context.select(MAS_JOB_RECORD_TMP.asterisk())
        .from(MAS_JOB_RECORD_TMP)
        .where(MAS_JOB_RECORD_TMP.JOB_ID.eq(masJobRecordHandle))
        .fetchOptional(this::recordToMasJobRecord);
  }

  public List<MasJobRecordFull> getMasJobRecordsByTargetId(String targetId, MjrJobState state,
      int pageNum, int pageSize) {
    var offset = getOffset(pageNum, pageSize);
    var condition = MAS_JOB_RECORD_TMP.TARGET_ID.eq(targetId);
    if (state != null) {
      condition = condition.and(MAS_JOB_RECORD_TMP.JOB_STATE.eq(state));
    }

    return context.select(MAS_JOB_RECORD_TMP.asterisk())
        .from(MAS_JOB_RECORD_TMP)
        .where(condition)
        .offset(offset)
        .limit(pageSize)
        .fetch(this::recordToMasJobRecord);
  }

  public List<MasJobRecordFull> getMasJobRecordsByMasId(String masId, MjrJobState state,
      int pageNum, int pageSize) {
    var offset = getOffset(pageNum, pageSize);
    var condition = MAS_JOB_RECORD_TMP.MAS_ID.eq((masId));
    if (state != null) {
      condition = condition.and(MAS_JOB_RECORD_TMP.JOB_STATE.eq(state));
    }
    return context.select(MAS_JOB_RECORD_TMP.asterisk())
        .from(MAS_JOB_RECORD_TMP)
        .where(condition)
        .limit(pageSize)
        .offset(offset)
        .fetch(this::recordToMasJobRecord);
  }

  public List<MasJobRecordFull> getMasJobRecordsByUserId(String userId, MjrJobState state,
      int pageNum, int pageSize) {
    var offset = getOffset(pageNum, pageSize);
    var condition = USER.ID.eq((userId));
    if (state != null) {
      condition = condition.and(MAS_JOB_RECORD_TMP.JOB_STATE.eq(state));
    }
    return context.select(MAS_JOB_RECORD_TMP.asterisk())
        .from(MAS_JOB_RECORD_TMP)
        .join(USER)
        .on(USER.ORCID.eq(MAS_JOB_RECORD_TMP.USER_ID))
        .where(condition)
        .limit(pageSize)
        .offset(offset)
        .fetch(this::recordToMasJobRecord);
  }

  public void createNewMasJobRecord(List<MasJobRecord> masJobRecord) {
    var queries = masJobRecord.stream().map(this::mjrToQuery).toList();
    context.batch(queries).execute();
  }

  public void markMasJobRecordsAsFailed(List<String> ids) {
    context.update(MAS_JOB_RECORD_TMP)
        .set(MAS_JOB_RECORD_TMP.JOB_STATE, MjrJobState.FAILED)
        .set(MAS_JOB_RECORD_TMP.TIME_COMPLETED, Instant.now())
        .where(MAS_JOB_RECORD_TMP.JOB_ID.in(ids))
        .execute();
  }

  public int markMasJobRecordAsRunning(String masId, String jobId) {
    return context.update(MAS_JOB_RECORD_TMP)
        .set(MAS_JOB_RECORD_TMP.JOB_STATE, MjrJobState.RUNNING)
        .where(MAS_JOB_RECORD_TMP.JOB_ID.eq(jobId))
        .and(MAS_JOB_RECORD_TMP.MAS_ID.eq(masId))
        .and(MAS_JOB_RECORD_TMP.JOB_STATE.eq(MjrJobState.SCHEDULED))
        .execute();
  }

  private Query mjrToQuery(MasJobRecord masJobRecord) {
    return context.insertInto(MAS_JOB_RECORD_TMP)
        .set(MAS_JOB_RECORD_TMP.JOB_ID, masJobRecord.jobId())
        .set(MAS_JOB_RECORD_TMP.JOB_STATE, masJobRecord.state())
        .set(MAS_JOB_RECORD_TMP.MAS_ID, masJobRecord.masId())
        .set(MAS_JOB_RECORD_TMP.USER_ID, masJobRecord.orcid())
        .set(MAS_JOB_RECORD_TMP.TARGET_ID, masJobRecord.targetId())
        .set(MAS_JOB_RECORD_TMP.TARGET_TYPE, masJobRecord.targetType())
        .set(MAS_JOB_RECORD_TMP.TIME_STARTED, Instant.now())
        .set(MAS_JOB_RECORD_TMP.BATCHING_REQUESTED, masJobRecord.batchingRequested());
  }

  private MasJobRecordFull recordToMasJobRecord(Record dbRecord) {
    try {
      var dataNode = dbRecord.get(MAS_JOB_RECORD_TMP.ANNOTATIONS) != null ?
          mapper.readValue(dbRecord.get(MAS_JOB_RECORD_TMP.ANNOTATIONS).data(), JsonNode.class) :
          null;
      return new MasJobRecordFull(
          dbRecord.get(MAS_JOB_RECORD_TMP.JOB_STATE),
          dbRecord.get(MAS_JOB_RECORD_TMP.MAS_ID),
          dbRecord.get(MAS_JOB_RECORD_TMP.TARGET_ID),
          dbRecord.get(MAS_JOB_RECORD_TMP.TARGET_TYPE),
          dbRecord.get(MAS_JOB_RECORD_TMP.USER_ID),
          dbRecord.get(MAS_JOB_RECORD_TMP.JOB_ID),
          dbRecord.get(MAS_JOB_RECORD_TMP.TIME_STARTED),
          dbRecord.get(MAS_JOB_RECORD_TMP.TIME_COMPLETED),
          dataNode,
          dbRecord.get(MAS_JOB_RECORD_TMP.BATCHING_REQUESTED)
      );
    } catch (JsonProcessingException e) {
      throw new DisscoJsonBMappingException("Unable to parse annotations from MAS job record", e);
    }

  }

}
