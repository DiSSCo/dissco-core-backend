package eu.dissco.backend.service;

import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.utils.MachineAnnotationServiceUtils.givenMasRecord;
import static eu.dissco.backend.utils.MasJobRecordUtils.JOB_ID;
import static eu.dissco.backend.utils.MasJobRecordUtils.givenMasJobRecordIdMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import eu.dissco.backend.domain.AnnotationState;
import eu.dissco.backend.domain.MasJobRecord;
import eu.dissco.backend.repository.MasJobRecordRepository;
import java.util.List;
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
    masJobRecordService = new MasJobRecordService(masJobRecordRepository);
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
