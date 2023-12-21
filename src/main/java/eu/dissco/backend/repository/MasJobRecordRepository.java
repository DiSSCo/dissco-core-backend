package eu.dissco.backend.repository;

import static eu.dissco.backend.database.jooq.Tables.MAS_JOB_RECORD_NEW;
import static eu.dissco.backend.database.jooq.Tables.USER;
import static eu.dissco.backend.repository.RepositoryUtils.getOffset;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.domain.MasJobState;
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
    return context.select(MAS_JOB_RECORD_NEW.asterisk())
        .from(MAS_JOB_RECORD_NEW)
        .where(MAS_JOB_RECORD_NEW.JOB_ID.eq(masJobRecordHandle))
        .fetchOptional(this::recordToMasJobRecord);
  }

  public List<MasJobRecordFull> getMasJobRecordsByTargetId(String targetId, MasJobState state,
      int pageNum, int pageSize) {
    var offset = getOffset(pageNum, pageSize);
    var condition = MAS_JOB_RECORD_NEW.TARGET_ID.eq(targetId);
    if (state != null) {
      condition = condition.and(MAS_JOB_RECORD_NEW.JOB_STATE.eq(state));
    }

    return context.select(MAS_JOB_RECORD_NEW.asterisk())
        .from(MAS_JOB_RECORD_NEW)
        .where(condition)
        .offset(offset)
        .limit(pageSize)
        .fetch(this::recordToMasJobRecord);
  }

  public List<MasJobRecordFull> getMasJobRecordsByMasId(String masId, MasJobState state,
      int pageNum, int pageSize) {
    var offset = getOffset(pageNum, pageSize);
    var condition = MAS_JOB_RECORD_NEW.MAS_ID.eq((masId));
    if (state != null) {
      condition = condition.and(MAS_JOB_RECORD_NEW.JOB_STATE.eq(state));
    }
    return context.select(MAS_JOB_RECORD_NEW.asterisk())
        .from(MAS_JOB_RECORD_NEW)
        .where(condition)
        .limit(pageSize)
        .offset(offset)
        .fetch(this::recordToMasJobRecord);
  }

  public List<MasJobRecordFull> getMasJobRecordsByUserId(String userId, MasJobState state,
      int pageNum, int pageSize) {
    var offset = getOffset(pageNum, pageSize);
    var condition = USER.ID.eq((userId));
    if (state != null) {
      condition = condition.and(MAS_JOB_RECORD_NEW.JOB_STATE.eq(state));
    }
    return context.select(MAS_JOB_RECORD_NEW.asterisk())
        .from(MAS_JOB_RECORD_NEW)
        .join(USER)
        .on(USER.ORCID.eq(MAS_JOB_RECORD_NEW.USER_ID))
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
    context.update(MAS_JOB_RECORD_NEW)
        .set(MAS_JOB_RECORD_NEW.JOB_STATE, MasJobState.FAILED)
        .set(MAS_JOB_RECORD_NEW.TIME_COMPLETED, Instant.now())
        .where(MAS_JOB_RECORD_NEW.JOB_ID.in(ids))
        .execute();
  }

  public int markMasJobRecordAsRunning(String masId, String jobId) {
    return context.update(MAS_JOB_RECORD_NEW)
        .set(MAS_JOB_RECORD_NEW.JOB_STATE, MasJobState.RUNNING)
        .where(MAS_JOB_RECORD_NEW.JOB_ID.eq(jobId))
        .and(MAS_JOB_RECORD_NEW.MAS_ID.eq(masId))
        .and(MAS_JOB_RECORD_NEW.JOB_STATE.eq(MasJobState.SCHEDULED))
        .execute();
  }

  private Query mjrToQuery(MasJobRecord masJobRecord) {
    return context.insertInto(MAS_JOB_RECORD_NEW)
        .set(MAS_JOB_RECORD_NEW.JOB_ID, masJobRecord.jobId())
        .set(MAS_JOB_RECORD_NEW.JOB_STATE, masJobRecord.state())
        .set(MAS_JOB_RECORD_NEW.MAS_ID, masJobRecord.masId())
        .set(MAS_JOB_RECORD_NEW.USER_ID, masJobRecord.orcid())
        .set(MAS_JOB_RECORD_NEW.TARGET_ID, masJobRecord.targetId())
        .set(MAS_JOB_RECORD_NEW.TARGET_TYPE, masJobRecord.targetType())
        .set(MAS_JOB_RECORD_NEW.TIME_STARTED, Instant.now());
  }

  private MasJobRecordFull recordToMasJobRecord(Record dbRecord) {
    try {
      var dataNode = dbRecord.get(MAS_JOB_RECORD_NEW.ANNOTATIONS) != null ?
          mapper.readValue(dbRecord.get(MAS_JOB_RECORD_NEW.ANNOTATIONS).data(), JsonNode.class) :
          null;
      return new MasJobRecordFull(
          dbRecord.get(MAS_JOB_RECORD_NEW.JOB_STATE),
          dbRecord.get(MAS_JOB_RECORD_NEW.MAS_ID),
          dbRecord.get(MAS_JOB_RECORD_NEW.TARGET_ID),
          dbRecord.get(MAS_JOB_RECORD_NEW.TARGET_TYPE),
          dbRecord.get(MAS_JOB_RECORD_NEW.USER_ID),
          dbRecord.get(MAS_JOB_RECORD_NEW.JOB_ID),
          dbRecord.get(MAS_JOB_RECORD_NEW.TIME_STARTED),
          dbRecord.get(MAS_JOB_RECORD_NEW.TIME_COMPLETED),
          dataNode
      );
    } catch (JsonProcessingException e) {
      throw new DisscoJsonBMappingException("Unable to parse annotations from MAS job record", e);
    }

  }

}
