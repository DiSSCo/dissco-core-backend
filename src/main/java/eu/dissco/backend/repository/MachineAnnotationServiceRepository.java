package eu.dissco.backend.repository;

import static eu.dissco.backend.database.jooq.Tables.MACHINE_ANNOTATION_SERVICES;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.database.jooq.tables.records.MachineAnnotationServicesRecord;
import eu.dissco.backend.domain.MachineAnnotationService;
import eu.dissco.backend.domain.MachineAnnotationServiceRecord;
import eu.dissco.backend.exceptions.DisscoJsonBMappingException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
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
    return context.selectFrom(MACHINE_ANNOTATION_SERVICES)
        .where(MACHINE_ANNOTATION_SERVICES.DELETED_ON.isNull())
        .fetch(this::mapToMasRecord);
  }

  private MachineAnnotationServiceRecord mapToMasRecord(
      MachineAnnotationServicesRecord machineAnnotationServicesRecord) {
    return new MachineAnnotationServiceRecord(
        machineAnnotationServicesRecord.getId(),
        machineAnnotationServicesRecord.getVersion(),
        machineAnnotationServicesRecord.getCreated(),
        machineAnnotationServicesRecord.getAdministrator(),
        new MachineAnnotationService(
            machineAnnotationServicesRecord.getName(),
            machineAnnotationServicesRecord.getContainerImage(),
            machineAnnotationServicesRecord.getContainerImageTag(),
            mapToJson(machineAnnotationServicesRecord.getTargetDigitalObjectFilters()),
            machineAnnotationServicesRecord.getServiceDescription(),
            machineAnnotationServicesRecord.getServiceState(),
            machineAnnotationServicesRecord.getSourceCodeRepository(),
            machineAnnotationServicesRecord.getServiceAvailability(),
            machineAnnotationServicesRecord.getCodeMaintainer(),
            machineAnnotationServicesRecord.getCodeLicense(),
            machineAnnotationServicesRecord.getDependencies() != null ? Arrays.stream(
                machineAnnotationServicesRecord.getDependencies()).toList() : null,
            machineAnnotationServicesRecord.getSupportContact(),
            machineAnnotationServicesRecord.getSlaDocumentation(),
            machineAnnotationServicesRecord.getTopicname(),
            machineAnnotationServicesRecord.getMaxreplicas(),
            machineAnnotationServicesRecord.getBatchingPermitted()),
        machineAnnotationServicesRecord.getDeletedOn(),
        machineAnnotationServicesRecord.getTimeToLive()
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

  public List<MachineAnnotationServiceRecord> getMasRecords(Set<String> mass) {
    return context.selectFrom(MACHINE_ANNOTATION_SERVICES)
        .where(MACHINE_ANNOTATION_SERVICES.ID.in(mass))
        .and(MACHINE_ANNOTATION_SERVICES.DELETED_ON.isNull())
        .fetch(this::mapToMasRecord);
  }
}
