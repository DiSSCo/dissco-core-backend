package eu.dissco.backend.service;

import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.ORCID;
import static eu.dissco.backend.utils.MachineAnnotationServiceUtils.givenMasRecord;
import static eu.dissco.backend.utils.MasJobRecordUtils.JOB_ID;
import static eu.dissco.backend.utils.MasJobRecordUtils.MJR_URI;
import static eu.dissco.backend.utils.MasJobRecordUtils.givenMasJobRecordFullScheduled;
import static eu.dissco.backend.utils.MasJobRecordUtils.givenMasJobRecordIdMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import eu.dissco.backend.domain.AnnotationState;
import eu.dissco.backend.domain.MasJobRecord;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.domain.jsonapi.JsonApiLinks;
import eu.dissco.backend.domain.jsonapi.JsonApiLinksFull;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.repository.MasJobRecordRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MasJobRecordServiceTest {

  private MasJobRecordService masJobRecordService;
  @Mock
  private MasJobRecordRepository masJobRecordRepository;

  @BeforeEach
  void setup() {
    masJobRecordService = new MasJobRecordService(masJobRecordRepository, MAPPER);
  }

  @Test
  void testGetMasJobRecordById() throws Exception {
    // Given
    var expected = new JsonApiWrapper(
        new JsonApiData(JOB_ID.toString(), "masJobRecord", givenMasJobRecordFullScheduled(),
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
    var linksNode = new JsonApiLinksFull(pageSize, pageNum, true, MJR_URI);
    var mjr = givenMasJobRecordFullScheduled();
    var dataList = Collections.nCopies(pageSize,
        new JsonApiData(JOB_ID.toString(), "masJobRecord", MAPPER.valueToTree(mjr)));
    var expected = new JsonApiListResponseWrapper(dataList, linksNode);
    given(
        masJobRecordRepository.getMasJobRecordsByCreator(ORCID, pageNum, pageSize + 1)).willReturn(
        Collections.nCopies(pageSize + 1, givenMasJobRecordFullScheduled()));

    // When
    var result = masJobRecordService.getMasJobRecordsByCreator(ORCID, MJR_URI, pageNum, pageSize,
        null);

    // Then
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void testGetMasJobRecordsByCreatorIdLastPage() {
    // Given
    var pageSize = 2;
    var pageNum = 1;
    var linksNode = new JsonApiLinksFull(pageSize, pageNum, false, MJR_URI);
    var mjr = givenMasJobRecordFullScheduled();
    var dataList = Collections.nCopies(pageSize,
        new JsonApiData(JOB_ID.toString(), "masJobRecord", MAPPER.valueToTree(mjr)));
    var expected = new JsonApiListResponseWrapper(dataList, linksNode);
    given(
        masJobRecordRepository.getMasJobRecordsByCreator(ORCID, pageNum, pageSize + 1)).willReturn(
        Collections.nCopies(pageSize, mjr));

    // When
    var result = masJobRecordService.getMasJobRecordsByCreator(ORCID, MJR_URI, pageNum, pageSize,
        null);

    // Then
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void testGetMasJobRecordsByCreatorIdAndState() {
    // Given
    var pageSize = 2;
    var pageNum = 1;
    var expected = new JsonApiListResponseWrapper(Collections.emptyList(),
        new JsonApiLinksFull(MJR_URI));
    given(masJobRecordRepository.getMasJobRecordsByCreatorAndStatus(ORCID,
        AnnotationState.FAILED.getState(), pageNum, pageSize + 1)).willReturn(
        Collections.emptyList());

    // When
    var result = masJobRecordService.getMasJobRecordsByCreator(ORCID, MJR_URI, pageNum, pageSize,
        AnnotationState.FAILED);

    // Then
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void testCreateMasJobRecord() {
    // Given
    var masRecord = givenMasRecord();
    var expected = givenMasJobRecordIdMap(masRecord.id());
    var masJobRecordList = List.of(new MasJobRecord(AnnotationState.SCHEDULED, masRecord.id(), ID));
    given(masJobRecordRepository.createNewMasJobRecord(masJobRecordList)).willReturn(expected);

    // When
    var result = masJobRecordService.createMasJobRecord(Set.of(masRecord), ID);

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
}
