package eu.dissco.backend.repository;

import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.givenDigitalSpecimen;
import static eu.dissco.backend.database.jooq.Tables.NEW_DIGITAL_SPECIMEN;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.dissco.backend.domain.DigitalSpecimen;
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
    context.truncate(NEW_DIGITAL_SPECIMEN).execute();
  }

  @Test
  void testGetSpecimenLatest() throws JsonProcessingException {
    // Given
    populateSpecimenTable();

    // When
    var resultPage1 = repository.getSpecimensLatest(1, 15);
    var resultPage2 = repository.getSpecimensLatest(2, 15);

    // Then
    assertThat(resultPage1).hasSize(15);
    assertThat(resultPage2).hasSize(7);
  }

  @Test
  void testGetSpecimenById() throws JsonProcessingException {
    // Given
    populateSpecimenTable();

    // When
    var result = repository.getSpecimenById("20.5000.1025/ABC-123-XY3");

    // Then
    assertThat(result).isEqualTo(givenDigitalSpecimen("20.5000.1025/ABC-123-XY3"));
  }

  private void populateSpecimenTable() throws JsonProcessingException {
    var specimens = new ArrayList<DigitalSpecimen>();
    for (int i = 0; i < 22; i++) {
      specimens.add(givenDigitalSpecimen("20.5000.1025/ABC-123-XY" + i));
    }
    insertIntoDatabase(specimens);
  }

  private void insertIntoDatabase(ArrayList<DigitalSpecimen> specimens) {
    for (var specimen : specimens) {
      context.insertInto(NEW_DIGITAL_SPECIMEN)
          .set(NEW_DIGITAL_SPECIMEN.ID, specimen.id())
          .set(NEW_DIGITAL_SPECIMEN.VERSION, specimen.version())
          .set(NEW_DIGITAL_SPECIMEN.TYPE, specimen.type())
          .set(NEW_DIGITAL_SPECIMEN.MIDSLEVEL, (short) specimen.midsLevel())
          .set(NEW_DIGITAL_SPECIMEN.PHYSICAL_SPECIMEN_ID, specimen.physicalSpecimenId())
          .set(NEW_DIGITAL_SPECIMEN.PHYSICAL_SPECIMEN_TYPE, specimen.physicalSpecimenIdType())
          .set(NEW_DIGITAL_SPECIMEN.SPECIMEN_NAME, specimen.specimenName())
          .set(NEW_DIGITAL_SPECIMEN.ORGANIZATION_ID, specimen.organizationId())
          .set(NEW_DIGITAL_SPECIMEN.PHYSICAL_SPECIMEN_COLLECTION,
              specimen.physicalSpecimenCollection())
          .set(NEW_DIGITAL_SPECIMEN.DATASET, specimen.datasetId())
          .set(NEW_DIGITAL_SPECIMEN.SOURCE_SYSTEM_ID, specimen.sourceSystemId())
          .set(NEW_DIGITAL_SPECIMEN.CREATED, specimen.created())
          .set(NEW_DIGITAL_SPECIMEN.LAST_CHECKED, specimen.created())
          .set(NEW_DIGITAL_SPECIMEN.DATA, JSONB.jsonb(specimen.data().toString()))
          .set(NEW_DIGITAL_SPECIMEN.ORIGINAL_DATA, JSONB.jsonb(specimen.originalData().toString()))
          .set(NEW_DIGITAL_SPECIMEN.DWCA_ID, specimen.dwcaId())
          .execute();
    }
  }

}
