package eu.dissco.backend.repository;

import static eu.dissco.backend.database.jooq.Tables.DIGITAL_SPECIMEN;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.domain.DigitalSpecimenWrapper;
import eu.dissco.backend.exceptions.DisscoJsonBMappingException;
import eu.dissco.backend.schema.DigitalSpecimen;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class SpecimenRepository {

  private final DSLContext context;
  private final ObjectMapper mapper;

  public DigitalSpecimenWrapper getLatestSpecimenById(String id) {
    return context.select(DIGITAL_SPECIMEN.asterisk())
        .from(DIGITAL_SPECIMEN)
        .where(DIGITAL_SPECIMEN.ID.eq(id))
        .fetchOne(this::mapToDigitalSpecimen);
  }

  private DigitalSpecimenWrapper mapToDigitalSpecimen(Record dbRecord) {
    try {
      var ds = mapper.readValue(dbRecord.get(DIGITAL_SPECIMEN.DATA).data(), DigitalSpecimen.class)
          .withOdsId("https://doi.org/" + dbRecord.get(DIGITAL_SPECIMEN.ID))
          .withOdsType(dbRecord.get(DIGITAL_SPECIMEN.TYPE))
          .withOdsMidsLevel(dbRecord.get(DIGITAL_SPECIMEN.MIDSLEVEL).intValue())
          .withOdsCreated(dbRecord.get(DIGITAL_SPECIMEN.CREATED).toString())
          .withOdsVersion(dbRecord.get(DIGITAL_SPECIMEN.VERSION));
      return new DigitalSpecimenWrapper(
          ds,
          mapper.readTree(dbRecord.get(DIGITAL_SPECIMEN.ORIGINAL_DATA).data()));
    } catch (JsonProcessingException e) {
      throw new DisscoJsonBMappingException(
          "Failed to parse jsonb field to json: " + dbRecord.get(DIGITAL_SPECIMEN.DATA).data(), e);
    }
  }

}
