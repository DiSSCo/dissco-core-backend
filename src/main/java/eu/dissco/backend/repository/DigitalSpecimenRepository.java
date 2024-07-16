package eu.dissco.backend.repository;

import static eu.dissco.backend.database.jooq.Tables.DIGITAL_SPECIMEN;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.exceptions.DisscoJsonBMappingException;
import eu.dissco.backend.schema.DigitalSpecimen;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class DigitalSpecimenRepository {

  private final DSLContext context;
  private final ObjectMapper mapper;

  public DigitalSpecimen getLatestSpecimenById(String id) {
    return context.select(DIGITAL_SPECIMEN.asterisk())
        .from(DIGITAL_SPECIMEN)
        .where(DIGITAL_SPECIMEN.ID.eq(id))
        .fetchOne(this::mapToDigitalSpecimen);
  }

  private DigitalSpecimen mapToDigitalSpecimen(Record dbRecord) {
    try {
      return mapper.readValue(dbRecord.get(DIGITAL_SPECIMEN.DATA).data(), DigitalSpecimen.class)
          .withId("https://doi.org/" + dbRecord.get(DIGITAL_SPECIMEN.ID))
          .withType("ods:DigitalSpecimen")
          .withOdsID("https://doi.org/" + dbRecord.get(DIGITAL_SPECIMEN.ID))
          .withOdsType(dbRecord.get(DIGITAL_SPECIMEN.TYPE))
          .withOdsMidsLevel(dbRecord.get(DIGITAL_SPECIMEN.MIDSLEVEL).intValue())
          .withDctermsCreated(Date.from(dbRecord.get(DIGITAL_SPECIMEN.CREATED)))
          .withOdsVersion(dbRecord.get(DIGITAL_SPECIMEN.VERSION));
    } catch (JsonProcessingException e) {
      throw new DisscoJsonBMappingException(
          "Failed to parse jsonb field to json: " + dbRecord.get(DIGITAL_SPECIMEN.DATA).data(), e);
    }
  }

}
