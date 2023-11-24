package eu.dissco.backend.repository;

import static eu.dissco.backend.database.jooq.Tables.MAS_JOB_RECORD;
import static eu.dissco.backend.database.jooq.Tables.NEW_USER;
import static eu.dissco.backend.repository.RepositoryUtils.getOffset;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.domain.AnnotationState;
import eu.dissco.backend.domain.MasJobRecord;
import eu.dissco.backend.domain.MasJobRecordFull;
import eu.dissco.backend.exceptions.DisscoJsonBMappingException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record5;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MasJobRecordRepository {

  private final DSLContext context;
  private final ObjectMapper mapper;

  public Optional<MasJobRecordFull> getMasJobRecordById(UUID masJobRecordId) {
    return context.select(MAS_JOB_RECORD.asterisk())
        .from(MAS_JOB_RECORD)
        .where(MAS_JOB_RECORD.JOB_ID.eq(masJobRecordId))
        .fetchOptional(this::recordToMasJobRecord);
  }

  public List<MasJobRecordFull> getMasJobRecordsByTargetId(String targetId, int pageNum,
      int pageSize) {
    var offset = getOffset(pageNum, pageSize);
    return context.select(MAS_JOB_RECORD.asterisk())
        .from(MAS_JOB_RECORD)
        .where(MAS_JOB_RECORD.TARGET_ID.eq(targetId))
        .offset(offset)
        .limit(pageSize)
        .fetch(this::recordToMasJobRecord);
  }

  public List<MasJobRecordFull> getMasJobRecordsByTargetIdAndState(String targetId, String state,
      int pageNum, int pageSize) {
    var offset = getOffset(pageNum, pageSize);
    return context.select(MAS_JOB_RECORD.asterisk())
        .from(MAS_JOB_RECORD)
        .where(MAS_JOB_RECORD.TARGET_ID.eq(targetId))
        .and(MAS_JOB_RECORD.STATE.eq(state))
        .offset(offset)
        .limit(pageSize)
        .fetch(this::recordToMasJobRecord);
  }

  public List<MasJobRecordFull> getMasJobRecordsByCreator(String creatorId, int pageNum,
      int pageSize) {
    var offset = getOffset(pageNum, pageSize);
    return context.select(MAS_JOB_RECORD.asterisk())
        .from(MAS_JOB_RECORD)
        .where(MAS_JOB_RECORD.CREATOR_ID.eq(creatorId))
        .limit(pageSize)
        .offset(offset)
        .fetch(this::recordToMasJobRecord);
  }

  public List<MasJobRecordFull> getMasJobRecordsByUserId(String userId, int pageNum, int pageSize) {
    var offset = getOffset(pageNum, pageSize);
    return context.select(MAS_JOB_RECORD.asterisk())
        .from(MAS_JOB_RECORD)
        .join(NEW_USER)
        .on(NEW_USER.ORCID.eq(MAS_JOB_RECORD.USER_ID))
        .where(NEW_USER.ID.eq(userId))
        .limit(pageSize)
        .offset(offset)
        .fetch(this::recordToMasJobRecord);
  }

  public List<MasJobRecordFull> getMasJobRecordsByCreatorAndState(String creatorId, String state,
      int pageNum, int pageSize) {
    var offset = getOffset(pageNum, pageSize);
    return context.select(MAS_JOB_RECORD.asterisk())
        .from(MAS_JOB_RECORD)
        .where(MAS_JOB_RECORD.CREATOR_ID.eq(creatorId))
        .and(MAS_JOB_RECORD.STATE.eq(state))
        .limit(pageSize)
        .offset(offset)
        .fetch(this::recordToMasJobRecord);
  }

  public List<MasJobRecordFull> getMasJobRecordsByUserIdAndState(String userId, String state,
      int pageNum, int pageSize) {
    var offset = getOffset(pageNum, pageSize);
    return context.select(MAS_JOB_RECORD.asterisk())
        .from(MAS_JOB_RECORD)
        .join(NEW_USER)
        .on(NEW_USER.ORCID.eq(MAS_JOB_RECORD.USER_ID))
        .where(NEW_USER.ID.eq(userId))
        .and(MAS_JOB_RECORD.STATE.eq(state))
        .limit(pageSize)
        .offset(offset)
        .fetch(this::recordToMasJobRecord);
  }

  public Map<String, UUID> createNewMasJobRecord(List<MasJobRecord> masJobRecord) {
    var records = masJobRecord.stream().map(this::mjrToRecord).toList();
    return context.insertInto(MAS_JOB_RECORD, MAS_JOB_RECORD.STATE, MAS_JOB_RECORD.CREATOR_ID,
            MAS_JOB_RECORD.TARGET_ID, MAS_JOB_RECORD.TIME_STARTED, MAS_JOB_RECORD.USER_ID)
        .valuesOfRecords(records)
        .returning(MAS_JOB_RECORD.CREATOR_ID, MAS_JOB_RECORD.JOB_ID)
        .fetchMap(MAS_JOB_RECORD.CREATOR_ID, MAS_JOB_RECORD.JOB_ID);
  }

  public void markMasJobRecordsAsFailed(List<UUID> ids) {
    context.update(MAS_JOB_RECORD)
        .set(MAS_JOB_RECORD.STATE, AnnotationState.FAILED.getState())
        .set(MAS_JOB_RECORD.TIME_COMPLETED, Instant.now())
        .where(MAS_JOB_RECORD.JOB_ID.in(ids))
        .execute();
  }

  private Record5<String, String, String, Instant, String> mjrToRecord(MasJobRecord masJobRecord) {
    var dbRecord = context.newRecord(MAS_JOB_RECORD.STATE, MAS_JOB_RECORD.CREATOR_ID,
        MAS_JOB_RECORD.TARGET_ID, MAS_JOB_RECORD.TIME_STARTED, MAS_JOB_RECORD.USER_ID);
    dbRecord.set(MAS_JOB_RECORD.STATE, masJobRecord.state().getState());
    dbRecord.set(MAS_JOB_RECORD.CREATOR_ID, masJobRecord.creatorId());
    dbRecord.set(MAS_JOB_RECORD.TARGET_ID, masJobRecord.targetId());
    dbRecord.set(MAS_JOB_RECORD.TIME_STARTED, Instant.now());
    dbRecord.set(MAS_JOB_RECORD.USER_ID, masJobRecord.orcid());
    return dbRecord;
  }

  private MasJobRecordFull recordToMasJobRecord(Record dbRecord) {
    try {
      return new MasJobRecordFull(
          AnnotationState.fromString(dbRecord.get(MAS_JOB_RECORD.STATE)),
          dbRecord.get(MAS_JOB_RECORD.CREATOR_ID),
          dbRecord.get(MAS_JOB_RECORD.TARGET_ID),
          dbRecord.get(MAS_JOB_RECORD.USER_ID),
          dbRecord.get(MAS_JOB_RECORD.JOB_ID),
          dbRecord.get(MAS_JOB_RECORD.TIME_STARTED),
          dbRecord.get(MAS_JOB_RECORD.TIME_COMPLETED),
          mapper.readValue(dbRecord.get(MAS_JOB_RECORD.ANNOTATIONS).data(), JsonNode.class)
      );
    } catch (JsonProcessingException e) {
      throw new DisscoJsonBMappingException("Unable to parse annotations from MAS job record", e);
    }

  }

}
