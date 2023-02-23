package eu.dissco.backend.repository;

import static eu.dissco.backend.database.jooq.Tables.NEW_DIGITAL_SPECIMEN;
import static eu.dissco.backend.repository.RepositoryUtils.getOffset;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.domain.DigitalSpecimen;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record1;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class SpecimenRepository {

  private final DSLContext context;
  private final ObjectMapper mapper;

  public List<DigitalSpecimen> getSpecimensLatest(int pageNumber, int pageSize) {
    int offset = getOffset(pageNumber, pageSize);
    return context.select(NEW_DIGITAL_SPECIMEN.asterisk())
        .from(NEW_DIGITAL_SPECIMEN)
        .offset(offset)
        .limit(pageSize)
        .fetch(this::mapper);
  }

  public DigitalSpecimen getSpecimenById(String id) {
    return context.select(NEW_DIGITAL_SPECIMEN.asterisk())
        .from(NEW_DIGITAL_SPECIMEN)
        .where(NEW_DIGITAL_SPECIMEN.ID.eq(id))
        .fetchOne(this::mapper);
  }

  private DigitalSpecimen mapper(Record dbRecord) {
    try {
      return new DigitalSpecimen(
          dbRecord.get(NEW_DIGITAL_SPECIMEN.ID),
          dbRecord.get(NEW_DIGITAL_SPECIMEN.MIDSLEVEL),
          dbRecord.get(NEW_DIGITAL_SPECIMEN.VERSION),
          dbRecord.get(NEW_DIGITAL_SPECIMEN.CREATED),
          dbRecord.get(NEW_DIGITAL_SPECIMEN.TYPE),
          dbRecord.get(NEW_DIGITAL_SPECIMEN.PHYSICAL_SPECIMEN_ID),
          dbRecord.get(NEW_DIGITAL_SPECIMEN.PHYSICAL_SPECIMEN_TYPE),
          dbRecord.get(NEW_DIGITAL_SPECIMEN.SPECIMEN_NAME),
          dbRecord.get(NEW_DIGITAL_SPECIMEN.ORGANIZATION_ID),
          dbRecord.get(NEW_DIGITAL_SPECIMEN.DATASET),
          dbRecord.get(NEW_DIGITAL_SPECIMEN.PHYSICAL_SPECIMEN_COLLECTION),
          dbRecord.get(NEW_DIGITAL_SPECIMEN.SOURCE_SYSTEM_ID),
          mapper.readTree(dbRecord.get(NEW_DIGITAL_SPECIMEN.DATA).data()),
          mapper.readTree(dbRecord.get(NEW_DIGITAL_SPECIMEN.ORIGINAL_DATA).data()),
          dbRecord.get(NEW_DIGITAL_SPECIMEN.DWCA_ID));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

}
