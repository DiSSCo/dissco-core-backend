package eu.dissco.backend.repository;

import static eu.dissco.backend.TestUtils.CREATED;
import static eu.dissco.backend.TestUtils.HANDLE;
import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.ID_ALT;
import static eu.dissco.backend.database.jooq.Tables.MAS_JOB_RECORD;
import static eu.dissco.backend.utils.MasJobRecordUtils.JOB_ID;
import static eu.dissco.backend.utils.MasJobRecordUtils.givenMasJobRecordIdMap;
import static org.assertj.core.api.Assertions.assertThat;

import eu.dissco.backend.domain.AnnotationState;
import eu.dissco.backend.domain.MasJobRecord;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MasJobRecordRepositoryIT extends BaseRepositoryIT {
  private MasJobRecordRepository masJobRecordRepository;

  @BeforeEach
  void setup(){
    masJobRecordRepository = new MasJobRecordRepository(context);
  }

  @Test
  void testCreateNewMasJobRecord(){
    // Given
    var mjr = new MasJobRecord(AnnotationState.SCHEDULED, ID, ID_ALT);

    // When
    var result = masJobRecordRepository.createNewMasJobRecord(List.of(mjr));

    // Then
    assertThat(result.get(mjr.creatorId())).isNotNull();
  }

  @Test
  void testMarkMasJobRecordsAsFailed(){
    // Given
    context.insertInto(MAS_JOB_RECORD)
        .set(MAS_JOB_RECORD.JOB_ID, JOB_ID)
        .set(MAS_JOB_RECORD.STATE, AnnotationState.SCHEDULED.getState())
        .set(MAS_JOB_RECORD.CREATOR_ID, ID)
        .set(MAS_JOB_RECORD.TIME_STARTED, CREATED)
        .set(MAS_JOB_RECORD.TARGET_ID, ID_ALT)
        .execute();

    // When
    masJobRecordRepository.markMasJobRecordsAsFailed(List.of(JOB_ID));
    var result = context.select(MAS_JOB_RECORD.JOB_ID, MAS_JOB_RECORD.STATE, MAS_JOB_RECORD.TIME_COMPLETED)
        .from(MAS_JOB_RECORD)
        .where(MAS_JOB_RECORD.JOB_ID.eq(JOB_ID))
        .fetchOne();
    var timestamp = result.get(MAS_JOB_RECORD.TIME_COMPLETED);
    var state = result.get(MAS_JOB_RECORD.STATE);

    // Then
    assertThat(timestamp).isNotNull();
    assertThat(state).isEqualTo(AnnotationState.FAILED.getState());
  }


}
