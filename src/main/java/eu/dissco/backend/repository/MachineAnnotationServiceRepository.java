package eu.dissco.backend.repository;

import static eu.dissco.backend.database.jooq.Tables.MACHINE_ANNOTATION_SERVICES_TMP;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.database.jooq.tables.records.MachineAnnotationServicesTmpRecord;
import eu.dissco.backend.domain.MachineAnnotationService;
import eu.dissco.backend.domain.MachineAnnotationServiceRecord;
import eu.dissco.backend.exceptions.DisscoJsonBMappingException;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MachineAnnotationServiceRepository {

  private final DSLContext context;
  private final ObjectMapper mapper;

  public List<MachineAnnotationServiceRecord> getAllMas() {
    return context.selectFrom(MACHINE_ANNOTATION_SERVICES_TMP)
        .where(MACHINE_ANNOTATION_SERVICES_TMP.DELETED_ON.isNull())
        .fetch(this::mapToMasRecord);
  }

  private MachineAnnotationServiceRecord mapToMasRecord(
      MachineAnnotationServicesTmpRecord machineAnnotationServicesTmpRecord) {
    return new MachineAnnotationServiceRecord(
        machineAnnotationServicesTmpRecord.getId(),
        machineAnnotationServicesTmpRecord.getVersion(),
        machineAnnotationServicesTmpRecord.getCreated(),
        machineAnnotationServicesTmpRecord.getAdministrator(),
        new MachineAnnotationService(
            machineAnnotationServicesTmpRecord.getName(),
            machineAnnotationServicesTmpRecord.getContainerImage(),
            machineAnnotationServicesTmpRecord.getContainerImageTag(),
            mapToJson(machineAnnotationServicesTmpRecord.getTargetDigitalObjectFilters()),
            machineAnnotationServicesTmpRecord.getServiceDescription(),
            machineAnnotationServicesTmpRecord.getServiceState(),
            machineAnnotationServicesTmpRecord.getSourceCodeRepository(),
            machineAnnotationServicesTmpRecord.getServiceAvailability(),
            machineAnnotationServicesTmpRecord.getCodeMaintainer(),
            machineAnnotationServicesTmpRecord.getCodeLicense(),
            machineAnnotationServicesTmpRecord.getDependencies() != null ? Arrays.stream(
                machineAnnotationServicesTmpRecord.getDependencies()).toList() : null,
            machineAnnotationServicesTmpRecord.getSupportContact(),
            machineAnnotationServicesTmpRecord.getSlaDocumentation(),
            machineAnnotationServicesTmpRecord.getTopicname(),
            machineAnnotationServicesTmpRecord.getMaxreplicas(),
            machineAnnotationServicesTmpRecord.getBatchingPermitted()
        ),
        machineAnnotationServicesTmpRecord.getDeletedOn()
    );
  }

  private JsonNode mapToJson(JSONB jsonb) {
    try {
      if (jsonb != null) {
        return mapper.readTree(jsonb.data());
      }
    } catch (JsonProcessingException e) {
      throw new DisscoJsonBMappingException("Failed to parse jsonb field to json: " + jsonb.data(),
          e);
    }
    return mapper.createObjectNode();
  }

  public List<MachineAnnotationServiceRecord> getMasRecords(List<String> mass) {
    return context.selectFrom(MACHINE_ANNOTATION_SERVICES_TMP)
        .where(MACHINE_ANNOTATION_SERVICES_TMP.ID.in(mass))
        .and(MACHINE_ANNOTATION_SERVICES_TMP.DELETED_ON.isNull())
        .fetch(this::mapToMasRecord);
  }
}
