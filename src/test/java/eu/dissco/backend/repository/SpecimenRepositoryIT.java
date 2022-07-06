package eu.dissco.backend.repository;

import static eu.dissco.backend.database.jooq.Tables.DIGITAL_SPECIMEN;
import static eu.dissco.backend.util.TestUtils.*;
import static eu.dissco.backend.util.TestUtils.ID;
import static eu.dissco.backend.util.TestUtils.givenDigitalSpecimen;
import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.database.jooq.enums.Curatedobjectidtypes;
import eu.dissco.backend.util.TestUtils;
import java.util.List;
import java.util.UUID;
import org.jooq.JSONB;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SpecimenRepositoryIT extends BaseRepositoryIT {

  private final ObjectMapper mapper = new ObjectMapper();

  private SpecimenRepository repository;

  @BeforeEach
  void prepareTests() {
    this.repository = new SpecimenRepository(context, mapper);
  }

  @AfterEach
  void cleanupTests() {
    context.truncate(DIGITAL_SPECIMEN).cascade().execute();
  }

  @Test
  void testGetSpecimenDefault() throws JsonProcessingException {
    // Given
    fillDatabase();

    // When
    var result = repository.getSpecimen(1, 10);

    // Then
    assertThat(result).isEqualTo(List.of(givenDigitalSpecimen()));
  }

  @Test
  void testGetSpecimenPagination() throws JsonProcessingException {
    // Given
    fillDatabaseMultiple();

    // When
    var resultFirstPage = repository.getSpecimen(1, 35);
    var resultSecondPage = repository.getSpecimen(2, 35);

    // Then
    assertThat(resultFirstPage).hasSize(35);
    assertThat(resultSecondPage).hasSize(15);
  }

  @Test
  void testGetSpecimenByID() throws JsonProcessingException {
    // Given
    fillDatabase();

    // When
    var result = repository.getSpecimenById(ID);

    // Then
    assertThat(result).isEqualTo(givenDigitalSpecimen());
  }

  private void fillDatabase() throws JsonProcessingException {
    fillDatabase(ID);
  }

  private void fillDatabaseMultiple() throws JsonProcessingException {
    for (int i = 0; i < 50; i++) {
      fillDatabase("test/" + UUID.randomUUID());
    }
  }

  private void fillDatabase(String id) throws JsonProcessingException {
    context.insertInto(DIGITAL_SPECIMEN)
        .set(DIGITAL_SPECIMEN.ID, id)
        .set(DIGITAL_SPECIMEN.OBJECT_TYPE, TYPE)
        .set(DIGITAL_SPECIMEN.SPECIMEN_NAME, NAME)
        .set(DIGITAL_SPECIMEN.CURATED_OBJECT_ID, CURATED_OBJECT_ID)
        .set(DIGITAL_SPECIMEN.CURATED_OBJECT_ID_TYPE, Curatedobjectidtypes.physicalSpecimenID)
        .set(DIGITAL_SPECIMEN.MIDS_LEVEL, (short) MIDS_LEVEL)
        .set(DIGITAL_SPECIMEN.INSTITUTION_ID, INSTITUTION)
        .set(DIGITAL_SPECIMEN.INSTITUTION_NAME, INSTITUTION_CODE)
        .set(DIGITAL_SPECIMEN.DATA, JSONB.jsonb(mapper.writeValueAsString(givenDigitalSpecimen())))
        .execute();
  }

}
