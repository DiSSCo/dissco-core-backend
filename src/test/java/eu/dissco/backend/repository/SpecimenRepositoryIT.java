package eu.dissco.backend.repository;

import static eu.dissco.backend.TestUtils.DOI;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.SOURCE_SYSTEM_ID_1;
import static eu.dissco.backend.TestUtils.givenDigitalSpecimenSourceSystem;
import static eu.dissco.backend.TestUtils.givenDigitalSpecimenWrapper;
import static eu.dissco.backend.database.jooq.Tables.DIGITAL_SPECIMEN;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.dissco.backend.domain.DigitalSpecimenWrapper;
import java.time.Instant;
import java.util.ArrayList;
import org.jooq.JSONB;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SpecimenRepositoryIT extends BaseRepositoryIT {

  private SpecimenRepository repository;

  @BeforeEach
  void setup() {
    repository = new SpecimenRepository(context, MAPPER);
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
    var specimens = new ArrayList<DigitalSpecimenWrapper>();
    for (int i = 0; i < 22; i++) {
      specimens.add(givenDigitalSpecimenWrapper("20.5000.1025/ABC-123-XY" + i));
    }
    insertIntoDatabase(specimens);
  }

  private void insertIntoDatabase(ArrayList<DigitalSpecimenWrapper> specimens)
      throws JsonProcessingException {
    for (var specimenWrapper : specimens) {
      context.insertInto(DIGITAL_SPECIMEN)
          .set(DIGITAL_SPECIMEN.ID, specimenWrapper.digitalSpecimen().getOdsId())
          .set(DIGITAL_SPECIMEN.VERSION, specimenWrapper.digitalSpecimen().getOdsVersion())
          .set(DIGITAL_SPECIMEN.TYPE, specimenWrapper.digitalSpecimen().getOdsType())
          .set(DIGITAL_SPECIMEN.MIDSLEVEL,
              specimenWrapper.digitalSpecimen().getOdsMidsLevel().shortValue())
          .set(DIGITAL_SPECIMEN.PHYSICAL_SPECIMEN_ID,
              specimenWrapper.digitalSpecimen().getOdsPhysicalSpecimenId())
          .set(DIGITAL_SPECIMEN.PHYSICAL_SPECIMEN_TYPE,
              specimenWrapper.digitalSpecimen().getOdsPhysicalSpecimenIdType().value())
          .set(DIGITAL_SPECIMEN.SPECIMEN_NAME,
              specimenWrapper.digitalSpecimen().getOdsSpecimenName())
          .set(DIGITAL_SPECIMEN.ORGANIZATION_ID,
              specimenWrapper.digitalSpecimen().getDwcInstitutionId())
          .set(DIGITAL_SPECIMEN.SOURCE_SYSTEM_ID,
              specimenWrapper.digitalSpecimen().getOdsSourceSystem())
          .set(DIGITAL_SPECIMEN.CREATED,
              Instant.parse(specimenWrapper.digitalSpecimen().getOdsCreated()))
          .set(DIGITAL_SPECIMEN.LAST_CHECKED,
              Instant.parse(specimenWrapper.digitalSpecimen().getOdsCreated()))
          .set(DIGITAL_SPECIMEN.DATA, JSONB.jsonb(
              MAPPER.writeValueAsString(specimenWrapper.digitalSpecimen())))
          .set(DIGITAL_SPECIMEN.ORIGINAL_DATA,
              JSONB.jsonb(specimenWrapper.originalData().toString()))
          .execute();
    }
  }

}
