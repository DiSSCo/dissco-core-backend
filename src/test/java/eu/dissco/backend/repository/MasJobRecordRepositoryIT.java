package eu.dissco.backend.repository;

import static eu.dissco.backend.TestUtils.CREATED;
import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.ID_ALT;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.ORCID;
import static eu.dissco.backend.TestUtils.USER_ID_TOKEN;
import static eu.dissco.backend.TestUtils.givenUser;
import static eu.dissco.backend.database.jooq.Tables.MAS_JOB_RECORD_NEW;
import static eu.dissco.backend.database.jooq.Tables.USER;
import static eu.dissco.backend.utils.MasJobRecordUtils.JOB_ID;
import static eu.dissco.backend.utils.MasJobRecordUtils.givenMasJobRecordFullCompleted;
import static eu.dissco.backend.utils.MasJobRecordUtils.givenMasJobRecordFullScheduled;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.dissco.backend.domain.MasJobRecord;
import eu.dissco.backend.domain.MasJobRecordFull;
import eu.dissco.backend.domain.MasJobState;
import eu.dissco.backend.domain.MjrTargetType;
import java.util.ArrayList;
import java.util.List;
import org.jooq.JSONB;
import org.jooq.Query;
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
    context.truncate(MAS_JOB_RECORD_NEW).execute();
    context.truncate(USER).execute();
  }

  @Test
  void testGetMasJobRecordById() throws JsonProcessingException {
    // Given
    var expected = givenMasJobRecordFullScheduled();
    postMasJobRecordFull(List.of(expected, givenMasJobRecordFullCompleted()));

    // When
    var result = masJobRecordRepository.getMasJobRecordById(expected.jobHandle());

    // Then
    assertThat(result).isPresent().contains(expected);
  }

  @Test
  void testGetMasJobRecordByIdCompleted() throws JsonProcessingException {
    // Given
    var expected = givenMasJobRecordFullCompleted();
    postMasJobRecordFull(List.of(expected));

    // When
    var result = masJobRecordRepository.getMasJobRecordById(expected.jobHandle());

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
    var result = masJobRecordRepository.getMasJobRecordsByMasId(ID_ALT, null, 1, 10);

    // Then
    assertThat(result).isEqualTo(List.of(expected));
  }

  @Test
  void testGetMasJobRecordsByCreatorAndStatus() throws JsonProcessingException {
    // Given
    var expected = givenMasJobRecordFullScheduled();
    postMasJobRecordFull(List.of(expected, givenMasJobRecordFullCompleted()));

    // When
    var resultStatusFailed = masJobRecordRepository.getMasJobRecordsByMasId(ID_ALT,
        MasJobState.FAILED, 1, 10);
    var resultStatusScheduled = masJobRecordRepository.getMasJobRecordsByMasId(ID_ALT,
        MasJobState.SCHEDULED, 1, 10);

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
        MasJobState.FAILED, 1, 10);
    var resultStatusScheduled = masJobRecordRepository.getMasJobRecordsByMasId(ID_ALT,
        MasJobState.SCHEDULED, 1, 10);

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
        MasJobState.SCHEDULED, 1, 1);

    // Then
    assertThat(result).isEqualTo(List.of(expected));
  }

  @Test
  void testCreateNewMasJobRecord() {
    // Given
    var mjr = new MasJobRecord(JOB_ID, MasJobState.SCHEDULED, ID, ID_ALT,
        MjrTargetType.DIGITAL_SPECIMEN, ORCID);

    // When
    masJobRecordRepository.createNewMasJobRecord(List.of(mjr));
    var result = context.select(MAS_JOB_RECORD_NEW.JOB_ID, MAS_JOB_RECORD_NEW.JOB_STATE)
        .from(MAS_JOB_RECORD_NEW).where(MAS_JOB_RECORD_NEW.JOB_ID.eq(JOB_ID)).fetchOne();

    // Then
    assertThat(result.get(MAS_JOB_RECORD_NEW.JOB_ID)).isEqualTo(JOB_ID);
    assertThat(result.get(MAS_JOB_RECORD_NEW.JOB_STATE)).isEqualTo(MasJobState.SCHEDULED);
  }

  @Test
  void testMarkMasJobRecordsAsFailed() throws Exception {
    // Given
    postMasJobRecordFull(List.of(givenMasJobRecordFullScheduled()));

    // When
    masJobRecordRepository.markMasJobRecordsAsFailed(List.of(JOB_ID));
    var result = context.select(MAS_JOB_RECORD_NEW.JOB_ID, MAS_JOB_RECORD_NEW.JOB_STATE,
            MAS_JOB_RECORD_NEW.TIME_COMPLETED)
        .from(MAS_JOB_RECORD_NEW)
        .where(MAS_JOB_RECORD_NEW.JOB_ID.eq(JOB_ID))
        .fetchSingle();
    var timestamp = result.get(MAS_JOB_RECORD_NEW.TIME_COMPLETED);
    var state = result.get(MAS_JOB_RECORD_NEW.JOB_STATE);

    // Then
    assertThat(timestamp).isNotNull();
    assertThat(state).isEqualTo(MasJobState.FAILED);
  }

  @Test
  void testMarkMasJobRecordAsRunning() throws Exception {
    // Given
    postMasJobRecordFull(List.of(givenMasJobRecordFullScheduled()));

    // When
    masJobRecordRepository.markMasJobRecordAsRunning(ID_ALT, JOB_ID);
    var result = context.select(MAS_JOB_RECORD_NEW.JOB_ID, MAS_JOB_RECORD_NEW.JOB_STATE)
        .from(MAS_JOB_RECORD_NEW)
        .where(MAS_JOB_RECORD_NEW.JOB_ID.eq(JOB_ID))
        .fetchSingle();
    var state = result.get(MAS_JOB_RECORD_NEW.JOB_STATE);

    // Then
    assertThat(state).isEqualTo(MasJobState.RUNNING);
  }

  @Test
  void testMarkMasJobRecordAsRunningCompleted() throws Exception {
    // Given
    postMasJobRecordFull(List.of(givenMasJobRecordFullCompleted()));

    // When
    masJobRecordRepository.markMasJobRecordAsRunning(ID_ALT, JOB_ID);
    var result = context.select(MAS_JOB_RECORD_NEW.JOB_ID, MAS_JOB_RECORD_NEW.JOB_STATE)
        .from(MAS_JOB_RECORD_NEW)
        .where(MAS_JOB_RECORD_NEW.JOB_ID.eq(JOB_ID))
        .fetchOptional();

    // Then
    assertThat(result).isEmpty();
  }

  private void postMasJobRecordFull(List<MasJobRecordFull> mjrList) throws JsonProcessingException {
    ArrayList<Query> queryList = new ArrayList<>();
    for (var mjr : mjrList) {
      var dataNode = mjr.annotations() != null ?
          JSONB.jsonb(MAPPER.writeValueAsString(mjr.annotations())) : null;
      queryList.add(context.insertInto(MAS_JOB_RECORD_NEW)
          .set(MAS_JOB_RECORD_NEW.JOB_ID, mjr.jobHandle())
          .set(MAS_JOB_RECORD_NEW.JOB_STATE, mjr.state())
          .set(MAS_JOB_RECORD_NEW.MAS_ID, mjr.masId())
          .set(MAS_JOB_RECORD_NEW.TARGET_ID, mjr.targetId())
          .set(MAS_JOB_RECORD_NEW.TARGET_TYPE, mjr.targetType())
          .set(MAS_JOB_RECORD_NEW.JOB_ID, mjr.jobHandle())
          .set(MAS_JOB_RECORD_NEW.TIME_STARTED, mjr.timeStarted())
          .set(MAS_JOB_RECORD_NEW.TIME_COMPLETED, mjr.timeCompleted())
          .set(MAS_JOB_RECORD_NEW.ANNOTATIONS, dataNode)
          .set(MAS_JOB_RECORD_NEW.USER_ID, mjr.orcid()));
    }
    context.batch(queryList).execute();
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
