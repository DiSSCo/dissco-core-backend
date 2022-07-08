package eu.dissco.backend.repository;

import static eu.dissco.backend.database.jooq.Tables.DIGITAL_SPECIMEN;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.domain.DigitalSpecimen;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.jooq.Record2;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class SpecimenRepository {

  private final DSLContext context;
  private final ObjectMapper mapper;

  public List<DigitalSpecimen> getSpecimen(int pageNumber, int pageSize) {
    var offset = 0;
    if (pageNumber > 1) {
      offset = offset + (pageSize * (pageNumber-1));
    }
    return context.select(DIGITAL_SPECIMEN.ID, DIGITAL_SPECIMEN.DATA).from(DIGITAL_SPECIMEN)
        .offset(offset)
        .limit(pageSize)
        .fetch(this::mapper);
  }

  public DigitalSpecimen getSpecimenById(String id) {
    return context.select(DIGITAL_SPECIMEN.ID, DIGITAL_SPECIMEN.DATA).from(DIGITAL_SPECIMEN)
        .where(DIGITAL_SPECIMEN.ID.eq(id)).fetchOne(this::mapper);
  }

  private DigitalSpecimen mapper(Record2<String, JSONB> dbRecord) {
    var id = dbRecord.get(DIGITAL_SPECIMEN.ID);
    try {
      var digitalSpecimen = mapper.readValue(dbRecord.get(DIGITAL_SPECIMEN.DATA).data(),
          DigitalSpecimen.class);
      digitalSpecimen.setId(id);
      return digitalSpecimen;
    } catch (JsonProcessingException e) {
      log.error("Failed to parse object from database for id: {}", id);
      return null;
    }
  }

}
