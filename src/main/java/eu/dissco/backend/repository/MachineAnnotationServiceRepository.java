package eu.dissco.backend.repository;

import static eu.dissco.backend.database.jooq.Tables.MACHINE_ANNOTATION_SERVICE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.exceptions.DisscoJsonBMappingException;
import eu.dissco.backend.schema.MachineAnnotationService;
import eu.dissco.backend.utils.ProxyUtils;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.jooq.Record1;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MachineAnnotationServiceRepository {

  private final DSLContext context;
  private final ObjectMapper mapper;

  public List<MachineAnnotationService> getAllMas() {
    return context.select(MACHINE_ANNOTATION_SERVICE.DATA)
        .from(MACHINE_ANNOTATION_SERVICE)
        .where(MACHINE_ANNOTATION_SERVICE.TOMBSTONED.isNull())
        .fetch(this::mapToMas);
  }

  private MachineAnnotationService mapToMas(Record1<JSONB> record1) {
    try {
      return mapper.readValue(record1.get(MACHINE_ANNOTATION_SERVICE.DATA).data(),
          MachineAnnotationService.class);
    } catch (JsonProcessingException e) {
      throw new DisscoJsonBMappingException("Unable to convert jsonb to machine annotation service",
          e);
    }
  }

  public List<MachineAnnotationService> getMasRecords(Set<String> mass) {
    var massIds = mass.stream().map(ProxyUtils::removeHandleProxy).toList();
    return context.select(MACHINE_ANNOTATION_SERVICE.DATA)
        .from(MACHINE_ANNOTATION_SERVICE)
        .where(MACHINE_ANNOTATION_SERVICE.ID.in(massIds))
        .and(MACHINE_ANNOTATION_SERVICE.TOMBSTONED.isNull())
        .fetch(this::mapToMas);
  }

}
