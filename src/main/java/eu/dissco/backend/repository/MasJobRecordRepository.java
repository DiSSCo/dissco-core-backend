package eu.dissco.backend.repository;

import static eu.dissco.backend.database.jooq.Tables.MAS_JOB_RECORD;

import eu.dissco.backend.domain.AnnotationState;
import eu.dissco.backend.domain.MasJobRecord;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Record4;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MasJobRecordRepository {

  private final DSLContext context;

  public Map<String, UUID> createNewMasJobRecord(List<MasJobRecord> masJobRecord){
    var records = masJobRecord.stream().map(this::mjrToRecord).toList();

    return context.insertInto(MAS_JOB_RECORD, MAS_JOB_RECORD.STATE, MAS_JOB_RECORD.CREATOR_ID, MAS_JOB_RECORD.TARGET_ID, MAS_JOB_RECORD.TIME_STARTED)
        .valuesOfRecords(records)
        .returning(MAS_JOB_RECORD.CREATOR_ID, MAS_JOB_RECORD.JOB_ID)
        .fetchMap(MAS_JOB_RECORD.CREATOR_ID, MAS_JOB_RECORD.JOB_ID);
  }

  public void markMasJobRecordsAsFailed(List<UUID> ids){
    context.update(MAS_JOB_RECORD)
        .set(MAS_JOB_RECORD.STATE, AnnotationState.FAILED.getState())
        .set(MAS_JOB_RECORD.TIME_COMPLETED, Instant.now())
        .where(MAS_JOB_RECORD.JOB_ID.in(ids))
        .execute();
  }

  private Record4<String, String, String, Instant> mjrToRecord(MasJobRecord masJobRecord){
    var dbRecord = context.newRecord(MAS_JOB_RECORD.STATE, MAS_JOB_RECORD.CREATOR_ID, MAS_JOB_RECORD.TARGET_ID, MAS_JOB_RECORD.TIME_STARTED);
    dbRecord.set(MAS_JOB_RECORD.STATE, masJobRecord.state().getState());
    dbRecord.set(MAS_JOB_RECORD.CREATOR_ID, masJobRecord.creatorId());
    dbRecord.set(MAS_JOB_RECORD.TARGET_ID, masJobRecord.targetId());
    dbRecord.set(MAS_JOB_RECORD.TIME_STARTED, Instant.now());
    return dbRecord;
  }

}
