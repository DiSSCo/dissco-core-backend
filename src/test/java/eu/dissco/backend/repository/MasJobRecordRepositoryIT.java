package eu.dissco.backend.repository;

import static eu.dissco.backend.TestUtils.CREATED;
import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.ID_ALT;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.ORCID;
import static eu.dissco.backend.TestUtils.USER_ID_TOKEN;
import static eu.dissco.backend.TestUtils.givenUser;
import static eu.dissco.backend.database.jooq.Tables.MAS_JOB_RECORD;
import static eu.dissco.backend.database.jooq.Tables.USER;
import static eu.dissco.backend.utils.MasJobRecordUtils.JOB_ID;
import static eu.dissco.backend.utils.MasJobRecordUtils.givenMasJobRecordFullCompleted;
import static eu.dissco.backend.utils.MasJobRecordUtils.givenMasJobRecordFullScheduled;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.dissco.backend.domain.AnnotationState;
import eu.dissco.backend.domain.MasJobRecord;
import eu.dissco.backend.domain.MasJobRecordFull;
import eu.dissco.backend.exceptions.NotFoundException;
import java.util.List;
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
    context.truncate(USER).execute();
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
    var result = masJobRecordRepository.getMasJobRecordsByCreatorId(ID_ALT, null, 1, 10);

    // Then
    assertThat(result).isEqualTo(List.of(expected));
  }

  @Test
  void testGetMasJobRecordsByCreatorAndStatus() throws JsonProcessingException {
    // Given
    var expected = givenMasJobRecordFullScheduled();
    postMasJobRecordFull(List.of(expected, givenMasJobRecordFullCompleted()));

    // When
    var resultStatusFailed = masJobRecordRepository.getMasJobRecordsByCreatorId(ID_ALT,
        AnnotationState.FAILED, 1, 10);
    var resultStatusScheduled = masJobRecordRepository.getMasJobRecordsByCreatorId(ID_ALT,
        AnnotationState.SCHEDULED, 1, 10);

    // Then
    assertThat(resultStatusFailed).isEmpty();
    assertThat(resultStatusScheduled).isEqualTo(List.of(expected));
  }

  @Test
  void testGetMasJobRecordsByTargetId() throws JsonProcessingException {
    // Given
    var expected = givenMasJobRecordFullScheduled();
    postMasJobRecordFull(List.of(expected, givenMasJobRecordFullCompleted()));

    // When
    var resultStatusFailed = masJobRecordRepository.getMasJobRecordsByTargetId(ID_ALT,
        AnnotationState.FAILED, 1, 10);
    var resultStatusScheduled = masJobRecordRepository.getMasJobRecordsByCreatorId(ID_ALT,
        AnnotationState.SCHEDULED, 1, 10);

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
    var result = masJobRecordRepository.getMasJobRecordsByUserId(USER_ID_TOKEN, null, 1, 1);

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
    var result = masJobRecordRepository.getMasJobRecordsByUserId(USER_ID_TOKEN,
        AnnotationState.SCHEDULED, 1, 1);

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
  void testMarkMasJobRecordsAsFailed() throws Exception {
    // Given
    postMasJobRecordFull(List.of(givenMasJobRecordFullScheduled()));

    // When
    masJobRecordRepository.markMasJobRecordsAsFailed(List.of(JOB_ID));
    var result = context.select(MAS_JOB_RECORD.JOB_ID, MAS_JOB_RECORD.STATE,
            MAS_JOB_RECORD.TIME_COMPLETED)
        .from(MAS_JOB_RECORD)
        .where(MAS_JOB_RECORD.JOB_ID.eq(JOB_ID))
        .fetchSingle();
    var timestamp = result.get(MAS_JOB_RECORD.TIME_COMPLETED);
    var state = result.get(MAS_JOB_RECORD.STATE);

    // Then
    assertThat(timestamp).isNotNull();
    assertThat(state).isEqualTo(AnnotationState.FAILED.getState());
  }

  @Test
  void testMarkMasJobRecordAsRunning() throws Exception {
    // Given
    postMasJobRecordFull(List.of(givenMasJobRecordFullScheduled()));

    // When
    masJobRecordRepository.markMasJobRecordAsRunning(ID_ALT, JOB_ID);
    var result = context.select(MAS_JOB_RECORD.JOB_ID, MAS_JOB_RECORD.STATE)
        .from(MAS_JOB_RECORD)
        .where(MAS_JOB_RECORD.JOB_ID.eq(JOB_ID))
        .fetchSingle();
    var state = result.get(MAS_JOB_RECORD.STATE);

    // Then
    assertThat(state).isEqualTo(AnnotationState.RUNNING.getState());
  }

  @Test
  void testMarkMasJobRecordAsRunningNotFound(){
    // Then
    assertThrows(NotFoundException.class,
        () -> masJobRecordRepository.markMasJobRecordAsRunning(ID_ALT, JOB_ID));
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
    context.insertInto(USER)
        .set(USER.ID, USER_ID_TOKEN)
        .set(USER.ORCID, user.orcid())
        .set(USER.FIRST_NAME, user.firstName())
        .set(USER.LAST_NAME, user.lastName())
        .set(USER.ORGANIZATION, user.organisation())
        .set(USER.CREATED, CREATED)
        .set(USER.UPDATED, CREATED)
        .execute();
  }

}
