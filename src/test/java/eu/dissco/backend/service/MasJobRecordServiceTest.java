package eu.dissco.backend.service;

import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.ID_ALT;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.ORCID;
import static eu.dissco.backend.TestUtils.TARGET_ID;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationResponse;
import static eu.dissco.backend.utils.AnnotationUtils.givenOaTarget;
import static eu.dissco.backend.utils.MachineAnnotationServiceUtils.givenMasJobRequest;
import static eu.dissco.backend.utils.MasJobRecordUtils.JOB_ID;
import static eu.dissco.backend.utils.MasJobRecordUtils.MJR_URI;
import static eu.dissco.backend.utils.MasJobRecordUtils.TTL_DEFAULT;
import static eu.dissco.backend.utils.MasJobRecordUtils.givenMasJobRecordFullScheduled;
import static eu.dissco.backend.utils.MasJobRecordUtils.givenMasJobRecordIdMap;
import static eu.dissco.backend.utils.MasJobRecordUtils.givenMjrListResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import eu.dissco.backend.database.jooq.enums.JobState;
import eu.dissco.backend.database.jooq.enums.MjrTargetType;
import eu.dissco.backend.domain.MasJobRecord;
import eu.dissco.backend.domain.annotation.AnnotationTargetType;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.domain.jsonapi.JsonApiLinks;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.repository.MasJobRecordRepository;
import eu.dissco.backend.utils.MachineAnnotationServiceUtils;
import eu.dissco.backend.web.HandleComponent;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MasJobRecordServiceTest {

  @Mock
  HandleComponent handleComponent;
  private MasJobRecordService masJobRecordService;
  @Mock
  private MasJobRecordRepository masJobRecordRepository;

  @BeforeEach
  void setup() {
    masJobRecordService = new MasJobRecordService(masJobRecordRepository, handleComponent, MAPPER);
  }

  @Test
  void testGetMasJobRecordById() throws Exception {
    // Given
    var expected = new JsonApiWrapper(
        new JsonApiData(JOB_ID, "masJobRecord", givenMasJobRecordFullScheduled(),
            MAPPER),
        new JsonApiLinks(MJR_URI)
    );
    given(masJobRecordRepository.getMasJobRecordById(JOB_ID)).willReturn(
        Optional.of(givenMasJobRecordFullScheduled()));

    // When
    var result = masJobRecordService.getMasJobRecordById(JOB_ID, MJR_URI);

    // Then
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void testGetMasJobRecordNotFound() {
    // Given
    given(masJobRecordRepository.getMasJobRecordsByTargetId(ID,
        JobState.SCHEDULED, 1, 2)).willReturn(Collections.emptyList());

    // Then
    assertThrows(NotFoundException.class,
        () -> masJobRecordService.getMasJobRecordByTargetId(ID, JobState.SCHEDULED, MJR_URI,
            1, 1));
  }

  @Test
  void testGetMasJobRecordByIdEmpty() {
    // Given
    given(masJobRecordRepository.getMasJobRecordById(JOB_ID)).willReturn(Optional.empty());

    // Then
    assertThrows(
        NotFoundException.class, () -> masJobRecordService.getMasJobRecordById(JOB_ID, MJR_URI));
  }

  @Test
  void testGetMasJobRecordsByCreatorIdHasNext() {
    // Given
    var pageSize = 2;
    var pageNum = 1;
    var expected = givenMjrListResponse(pageSize, pageNum, true);
    given(
        masJobRecordRepository.getMasJobRecordsByMasId(ID_ALT, null, pageNum,
            pageSize + 1)).willReturn(
        Collections.nCopies(pageSize + 1, givenMasJobRecordFullScheduled()));

    // When
    var result = masJobRecordService.getMasJobRecordsByMasId(ID_ALT, MJR_URI, pageNum, pageSize,
        null);

    // Then
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void testGetMasJobRecordsByCreatorIdLastPage() {
    // Given
    var pageSize = 2;
    var pageNum = 2;
    var mjr = givenMasJobRecordFullScheduled();
    var expected = givenMjrListResponse(pageSize, pageNum, false);

    given(
        masJobRecordRepository.getMasJobRecordsByMasId(ID_ALT, null, pageNum,
            pageSize + 1)).willReturn(
        Collections.nCopies(pageSize, mjr));

    // When
    var result = masJobRecordService.getMasJobRecordsByMasId(ID_ALT, MJR_URI, pageNum, pageSize,
        null);

    // Then
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void testCreateMasJobRecord() {
    // Given
    var masRecord = MachineAnnotationServiceUtils.givenMas();
    var expected = givenMasJobRecordIdMap(masRecord.getId());
    given(handleComponent.postHandle(1)).willReturn(List.of(JOB_ID));

    // When
    var result = masJobRecordService.createMasJobRecord(Set.of(masRecord), ID_ALT, ORCID,
        MjrTargetType.DIGITAL_SPECIMEN, Map.of(masRecord.getId(), givenMasJobRequest()));

    // Then
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void testCreateMasJobRecordForDissco() {
    // Given
    var masRecord = new MasJobRecord(
        JOB_ID,
        JobState.RUNNING,
        "DISSCOVER",
        TARGET_ID,
        MjrTargetType.DIGITAL_SPECIMEN,
        ORCID,
        true,
        TTL_DEFAULT
    );
    given(handleComponent.postHandle(1)).willReturn(List.of(JOB_ID));

    // When
    var result = masJobRecordService.createJobRecordForDisscover(givenAnnotationResponse(), ORCID);

    // Then
    assertThat(result).isEqualTo(JOB_ID);
    then(masJobRecordRepository).should().createNewMasJobRecord(List.of(masRecord));
  }

  @Test
  void testCreateMasJobRecordForDisscoMedia() {
    // Given
    var masRecord = new MasJobRecord(
        JOB_ID,
        JobState.RUNNING,
        "DISSCOVER",
        TARGET_ID,
        MjrTargetType.MEDIA_OBJECT,
        ORCID,
        true,
        TTL_DEFAULT
    );
    var annotation = givenAnnotationResponse()
        .withOaHasTarget(givenOaTarget(TARGET_ID)
            .withOdsFdoType(AnnotationTargetType.DIGITAL_MEDIA.getName()));
    given(handleComponent.postHandle(1)).willReturn(List.of(JOB_ID));

    // When
    var result = masJobRecordService.createJobRecordForDisscover(annotation, ORCID);

    // Then
    assertThat(result).isEqualTo(JOB_ID);
    then(masJobRecordRepository).should().createNewMasJobRecord(List.of(masRecord));
  }

  @Test
  void testCreateMasJobRecordCustomTTL() {
    // Given
    var masRecord = MachineAnnotationServiceUtils.givenMas();
    var expected = givenMasJobRecordIdMap(masRecord.getId());
    given(handleComponent.postHandle(1)).willReturn(List.of(JOB_ID));

    // When
    var result = masJobRecordService.createMasJobRecord(Set.of(masRecord), ID_ALT, ORCID,
        MjrTargetType.DIGITAL_SPECIMEN, Map.of(masRecord.getId(), givenMasJobRequest(false)));

    // Then
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void testMarkMasJobRecordAsFailed() {
    // When
    masJobRecordService.markMasJobRecordAsFailed(List.of(JOB_ID));

    // Then
    then(masJobRecordRepository).should().markMasJobRecordsAsFailed(List.of(JOB_ID));
  }

  @Test
  void testMarkMasJobRecordAsRunning() throws Exception {
    // Given
    given(masJobRecordRepository.markMasJobRecordAsRunning(ID, JOB_ID)).willReturn(1);

    // When
    masJobRecordService.markMasJobRecordAsRunning(ID, JOB_ID);

    // Then
    then(masJobRecordRepository).should().markMasJobRecordAsRunning(ID, JOB_ID);
  }

  @Test
  void testMarkMasJobRecordAsRunningNotFound() {
    // Then
    assertThrows(NotFoundException.class,
        () -> masJobRecordService.markMasJobRecordAsRunning(ID, JOB_ID));
  }

}
