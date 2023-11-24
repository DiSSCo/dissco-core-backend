package eu.dissco.backend.repository;

import static eu.dissco.backend.TestUtils.CREATED;
import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.ID_ALT;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.ORCID;
import static eu.dissco.backend.TestUtils.USER_ID_TOKEN;
import static eu.dissco.backend.TestUtils.givenUser;
import static eu.dissco.backend.database.jooq.Tables.MAS_JOB_RECORD;
import static eu.dissco.backend.database.jooq.Tables.NEW_USER;
import static eu.dissco.backend.utils.MasJobRecordUtils.JOB_ID;
import static eu.dissco.backend.utils.MasJobRecordUtils.JOB_ID_ALT;
import static eu.dissco.backend.utils.MasJobRecordUtils.givenMasJobRecordFullCompleted;
import static eu.dissco.backend.utils.MasJobRecordUtils.givenMasJobRecordFullScheduled;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.dissco.backend.domain.AnnotationState;
import eu.dissco.backend.domain.MasJobRecord;
import eu.dissco.backend.domain.MasJobRecordFull;
import java.util.List;
import org.checkerframework.checker.units.qual.A;
import org.checkerframework.checker.units.qual.N;
import org.jooq.JSONB;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MasJobRecordRepositoryIT extends BaseRepositoryIT {

  private MasJobRecordRepository masJobRecordRepository;

  @BeforeEach
  void setup() {
    masJobRecordRepository = new MasJobRecordRepository(context, MAPPER);
  }

  @AfterEach
  void destroy() {
    context.truncate(MAS_JOB_RECORD).execute();
    context.truncate(NEW_USER).execute();
  }

  @Test
  void testGetMasJobRecordById() throws JsonProcessingException {
    // Given
    var expected = givenMasJobRecordFullScheduled();
    postMasJobRecordFull(List.of(expected, givenMasJobRecordFullCompleted()));

    // When
    var result = masJobRecordRepository.getMasJobRecordById(expected.jobId());

    // Then
    assertThat(result).isPresent().contains(expected);
  }

  @Test
  void testGetMasJobRecordByIdNotPresent() throws JsonProcessingException {
    // Given
    postMasJobRecordFull(List.of(givenMasJobRecordFullCompleted()));

    // When
    var result = masJobRecordRepository.getMasJobRecordById(JOB_ID);

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  void testGetMasJobRecordsByCreator() throws JsonProcessingException {
    // Given
    var expected = givenMasJobRecordFullScheduled();
    postMasJobRecordFull(List.of(expected, givenMasJobRecordFullCompleted(ID)));

    // When
    var result = masJobRecordRepository.getMasJobRecordsByCreator(ID_ALT, 1, 10);

    // Then
    assertThat(result).isEqualTo(List.of(expected));
  }

  @Test
  void testGetMasJobRecordsByCreatorAndStatus() throws JsonProcessingException {
    // Given
    var expected = givenMasJobRecordFullScheduled();
    postMasJobRecordFull(List.of(expected, givenMasJobRecordFullCompleted()));

    // When
    var resultStatusFailed = masJobRecordRepository.getMasJobRecordsByCreatorAndState(ID_ALT,
        AnnotationState.FAILED.getState(), 1, 10);
    var resultStatusScheduled = masJobRecordRepository.getMasJobRecordsByCreatorAndState(ID_ALT,
        AnnotationState.SCHEDULED.getState(), 1, 10);

    // Then
    assertThat(resultStatusFailed).isEmpty();
    assertThat(resultStatusScheduled).isEqualTo(List.of(expected));
  }

  @Test
  void testGetMasJobRecordsByTargetId() throws JsonProcessingException {
    // Given
    var expected = new MasJobRecordFull(AnnotationState.SCHEDULED, ORCID, ID_ALT, ORCID, JOB_ID, CREATED, null, MAPPER.createObjectNode());
    postMasJobRecordFull(List.of(expected, givenMasJobRecordFullCompleted(ID)));

    // When
    var result = masJobRecordRepository.getMasJobRecordsByTargetId(ID_ALT, 1, 10);

    // Then
    assertThat(result).isEqualTo(List.of(expected));
  }

  @Test
  void testGetMasJobRecordsByTargetAndStatus() throws JsonProcessingException {
    // Given
    var expected = givenMasJobRecordFullScheduled();
    postMasJobRecordFull(List.of(expected, givenMasJobRecordFullCompleted()));

    // When
    var resultStatusFailed = masJobRecordRepository.getMasJobRecordsByTargetIdAndState(ID_ALT,
        AnnotationState.FAILED.getState(), 1, 10);
    var resultStatusScheduled = masJobRecordRepository.getMasJobRecordsByCreatorAndState(ID_ALT,
        AnnotationState.SCHEDULED.getState(), 1, 10);

    // Then
    assertThat(resultStatusFailed).isEmpty();
    assertThat(resultStatusScheduled).isEqualTo(List.of(expected));
  }

  @Test
  void testGetMasJobRecordByUserId() throws JsonProcessingException {
    // Given
    var expected = List.of(givenMasJobRecordFullScheduled());
    postMasJobRecordFull(expected);
    postUser();

    // When
    var result = masJobRecordRepository.getMasJobRecordsByUserId(USER_ID_TOKEN, 1, 1);

    // Then
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void testGetMasJobRecordByUserIdAndState() throws JsonProcessingException {
    // Given
    var expected = givenMasJobRecordFullScheduled();
    postMasJobRecordFull(List.of(expected, givenMasJobRecordFullCompleted()));
    postUser();

    // When
    var result = masJobRecordRepository.getMasJobRecordsByUserIdAndState(USER_ID_TOKEN, AnnotationState.SCHEDULED.getState(), 1, 1);

    // Then
    assertThat(result).isEqualTo(List.of(expected));
  }

  @Test
  void testCreateNewMasJobRecord() {
    // Given
    var mjr = new MasJobRecord(AnnotationState.SCHEDULED, ID, ID_ALT, ORCID);

    // When
    var result = masJobRecordRepository.createNewMasJobRecord(List.of(mjr));

    // Then
    assertThat(result.get(mjr.creatorId())).isNotNull();
  }

  @Test
  void testMarkMasJobRecordsAsFailed() {
    // Given
    context.insertInto(MAS_JOB_RECORD)
        .set(MAS_JOB_RECORD.JOB_ID, JOB_ID)
        .set(MAS_JOB_RECORD.STATE, AnnotationState.SCHEDULED.getState())
        .set(MAS_JOB_RECORD.CREATOR_ID, ID)
        .set(MAS_JOB_RECORD.TIME_STARTED, CREATED)
        .set(MAS_JOB_RECORD.TARGET_ID, ID_ALT)
        .set(MAS_JOB_RECORD.USER_ID, ORCID)
        .execute();

    // When
    masJobRecordRepository.markMasJobRecordsAsFailed(List.of(JOB_ID));
    var result = context.select(MAS_JOB_RECORD.JOB_ID, MAS_JOB_RECORD.STATE,
            MAS_JOB_RECORD.TIME_COMPLETED)
        .from(MAS_JOB_RECORD)
        .where(MAS_JOB_RECORD.JOB_ID.eq(JOB_ID))
        .fetchOne();
    var timestamp = result.get(MAS_JOB_RECORD.TIME_COMPLETED);
    var state = result.get(MAS_JOB_RECORD.STATE);

    // Then
    assertThat(timestamp).isNotNull();
    assertThat(state).isEqualTo(AnnotationState.FAILED.getState());
  }

  private void postMasJobRecordFull(List<MasJobRecordFull> mjrList) throws JsonProcessingException {
    for (var mjr : mjrList) {
      context.insertInto(MAS_JOB_RECORD)
          .set(MAS_JOB_RECORD.STATE, mjr.state().getState())
          .set(MAS_JOB_RECORD.CREATOR_ID, mjr.creatorId())
          .set(MAS_JOB_RECORD.TARGET_ID, mjr.targetId())
          .set(MAS_JOB_RECORD.JOB_ID, mjr.jobId())
          .set(MAS_JOB_RECORD.TIME_STARTED, mjr.timeStarted())
          .set(MAS_JOB_RECORD.TIME_COMPLETED, mjr.timeCompleted())
          .set(MAS_JOB_RECORD.ANNOTATIONS,
              JSONB.jsonb(MAPPER.writeValueAsString(mjr.annotations())))
          .set(MAS_JOB_RECORD.USER_ID, mjr.orcid())
          .execute();
    }
  }

  private void postUser() {
    var user = givenUser();
    context.insertInto(NEW_USER)
        .set(NEW_USER.ID, USER_ID_TOKEN)
        .set(NEW_USER.ORCID, user.orcid())
        .set(NEW_USER.FIRST_NAME, user.firstName())
        .set(NEW_USER.LAST_NAME, user.lastName())
        .set(NEW_USER.ORGANIZATION, user.organisation())
        .set(NEW_USER.CREATED, CREATED)
        .set(NEW_USER.UPDATED, CREATED)
        .execute();
  }

}
