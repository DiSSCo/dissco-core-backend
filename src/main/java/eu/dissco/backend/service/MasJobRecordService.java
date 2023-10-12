package eu.dissco.backend.service;

import eu.dissco.backend.domain.AnnotationState;
import eu.dissco.backend.domain.MachineAnnotationServiceRecord;
import eu.dissco.backend.domain.MasJobRecord;
import eu.dissco.backend.repository.MasJobRecordRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MasJobRecordService {

  private final MasJobRecordRepository masJobRecordRepository;

  public Map<String, UUID> createMasJobRecord(Set<MachineAnnotationServiceRecord> masRecords, String targetId) {
    var masJobRecordList = masRecords.stream()
        .map(masRecord -> new MasJobRecord(AnnotationState.SCHEDULED, masRecord.id(), targetId))
        .toList();
    return masJobRecordRepository.createNewMasJobRecord(masJobRecordList);
  }

  public void markMasJobRecordAsFailed(List<UUID> failedJobIds){
    masJobRecordRepository.markMasJobRecordsAsFailed(failedJobIds);
  }

}
