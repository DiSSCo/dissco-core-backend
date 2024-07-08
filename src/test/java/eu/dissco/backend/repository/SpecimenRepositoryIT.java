package eu.dissco.backend.repository;

import static eu.dissco.backend.TestUtils.DOI;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.SOURCE_SYSTEM_ID_1;
import static eu.dissco.backend.TestUtils.givenDigitalSpecimenSourceSystem;
import static eu.dissco.backend.TestUtils.givenDigitalSpecimenWrapper;
import static eu.dissco.backend.database.jooq.Tables.DIGITAL_SPECIMEN;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.dissco.backend.schema.DigitalSpecimen;
import java.util.ArrayList;
import org.jooq.JSONB;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SpecimenRepositoryIT extends BaseRepositoryIT {

  private DigitalSpecimenRepository repository;

  @BeforeEach
  void setup() {
    repository = new DigitalSpecimenRepository(context, MAPPER);
  }

  @AfterEach
  void destroy() {
    context.truncate(DIGITAL_SPECIMEN).execute();
  }

  @Test
  void testGetSpecimenById() throws JsonProcessingException {
    // Given
    populateSpecimenTable();

    // When
    var result = repository.getLatestSpecimenById("20.5000.1025/ABC-123-XY3");

    // Then
    assertThat(result).isEqualTo(
        givenDigitalSpecimenSourceSystem(DOI + "20.5000.1025/ABC-123-XY3", SOURCE_SYSTEM_ID_1));
  }

  private void populateSpecimenTable() throws JsonProcessingException {
    var specimens = new ArrayList<DigitalSpecimen>();
    for (int i = 0; i < 22; i++) {
      specimens.add(givenDigitalSpecimenWrapper("20.5000.1025/ABC-123-XY" + i));
    }
    insertIntoDatabase(specimens);
  }

  private void insertIntoDatabase(ArrayList<DigitalSpecimen> specimens)
      throws JsonProcessingException {
    for (var specimenWrapper : specimens) {
      context.insertInto(DIGITAL_SPECIMEN)
          .set(DIGITAL_SPECIMEN.ID, specimenWrapper.getOdsID())
          .set(DIGITAL_SPECIMEN.VERSION, specimenWrapper.getOdsVersion())
          .set(DIGITAL_SPECIMEN.TYPE, specimenWrapper.getOdsType())
          .set(DIGITAL_SPECIMEN.MIDSLEVEL,
              specimenWrapper.getOdsMidsLevel().shortValue())
          .set(DIGITAL_SPECIMEN.PHYSICAL_SPECIMEN_ID,
              specimenWrapper.getOdsPhysicalSpecimenID())
          .set(DIGITAL_SPECIMEN.PHYSICAL_SPECIMEN_TYPE,
              specimenWrapper.getOdsPhysicalSpecimenIDType().value())
          .set(DIGITAL_SPECIMEN.SPECIMEN_NAME,
              specimenWrapper.getOdsSpecimenName())
          .set(DIGITAL_SPECIMEN.ORGANIZATION_ID,
              specimenWrapper.getOdsOrganisationID())
          .set(DIGITAL_SPECIMEN.SOURCE_SYSTEM_ID,
              specimenWrapper.getOdsSourceSystemID())
          .set(DIGITAL_SPECIMEN.CREATED,
              specimenWrapper.getDctermsCreated().toInstant())
          .set(DIGITAL_SPECIMEN.LAST_CHECKED,
              specimenWrapper.getDctermsCreated().toInstant())
          .set(DIGITAL_SPECIMEN.DATA, JSONB.jsonb(
              MAPPER.writeValueAsString(specimenWrapper)))
          .execute();
    }
  }

}
