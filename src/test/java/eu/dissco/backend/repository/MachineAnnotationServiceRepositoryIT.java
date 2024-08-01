package eu.dissco.backend.repository;

import static eu.dissco.backend.TestUtils.CREATED;
import static eu.dissco.backend.TestUtils.HANDLE;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.PREFIX;
import static eu.dissco.backend.database.jooq.Tables.MACHINE_ANNOTATION_SERVICE;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.dissco.backend.schema.MachineAnnotationService;
import eu.dissco.backend.utils.MachineAnnotationServiceUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.jooq.JSONB;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MachineAnnotationServiceRepositoryIT extends BaseRepositoryIT {

  private MachineAnnotationServiceRepository repository;

  @BeforeEach
  void setup() {
    repository = new MachineAnnotationServiceRepository(context, MAPPER);
  }

  @AfterEach
  void destroy() {
    context.truncate(MACHINE_ANNOTATION_SERVICE).execute();
  }

  @Test
  void testGetAllMas() throws JsonProcessingException {
    // Given
    var expected = populateDatabase();

    // When
    var result = repository.getAllMas();

    // Then
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void testGetMasRecords() throws JsonProcessingException {
    // Given
    populateDatabase();

    // When
    var result = repository.getMasRecords(Set.of(PREFIX + "ABC-123-XY14", PREFIX + "ABC-123-XY18"));

    // Then
    assertThat(result).hasSize(2);
  }

  private List<MachineAnnotationService> populateDatabase() throws JsonProcessingException {
    var expectedRecords = new ArrayList<MachineAnnotationService>();
    for (int i = 0; i < 26; i++) {
      MachineAnnotationService masRecord;
      if (i % 2 == 0) {
        masRecord = MachineAnnotationServiceUtils.givenMas(PREFIX + "ABC-123-XY" + i, null);
        expectedRecords.add(masRecord);
      } else {
        masRecord = MachineAnnotationServiceUtils.givenMas(PREFIX + "ABC-123-XY" + i,
            CREATED);
      }
      createRecord(masRecord);
    }
    return expectedRecords;
  }

  private void createRecord(MachineAnnotationService mas) throws JsonProcessingException {
    context.insertInto(MACHINE_ANNOTATION_SERVICE)
        .set(MACHINE_ANNOTATION_SERVICE.ID, mas.getId().replace(HANDLE, ""))
        .set(MACHINE_ANNOTATION_SERVICE.VERSION, mas.getSchemaVersion())
        .set(MACHINE_ANNOTATION_SERVICE.NAME, mas.getSchemaName())
        .set(MACHINE_ANNOTATION_SERVICE.DATE_CREATED, mas.getSchemaDateCreated().toInstant())
        .set(MACHINE_ANNOTATION_SERVICE.DATE_MODIFIED, mas.getSchemaDateModified().toInstant())
        .set(MACHINE_ANNOTATION_SERVICE.CREATOR, mas.getSchemaCreator().getId())
        .set(MACHINE_ANNOTATION_SERVICE.CONTAINER_IMAGE, mas.getOdsContainerImage())
        .set(MACHINE_ANNOTATION_SERVICE.CONTAINER_IMAGE_TAG, mas.getOdsContainerTag())
        .set(MACHINE_ANNOTATION_SERVICE.CREATIVE_WORK_STATE,
            mas.getSchemaCreativeWorkStatus())
        .set(MACHINE_ANNOTATION_SERVICE.SERVICE_AVAILABILITY,
            mas.getOdsServiceAvailability())
        .set(MACHINE_ANNOTATION_SERVICE.SOURCE_CODE_REPOSITORY,
            mas.getSchemaCodeRepository())
        .set(MACHINE_ANNOTATION_SERVICE.CODE_MAINTAINER, mas.getSchemaMaintainer().getId())
        .set(MACHINE_ANNOTATION_SERVICE.CODE_LICENSE, mas.getSchemaLicense())
        .set(MACHINE_ANNOTATION_SERVICE.BATCHING_PERMITTED, mas.getOdsBatchingPermitted())
        .set(MACHINE_ANNOTATION_SERVICE.TIME_TO_LIVE, mas.getOdsTimeToLive())
        .set(MACHINE_ANNOTATION_SERVICE.DATE_TOMBSTONED,
            mas.getOdsTombstoneMetadata() != null ? mas.getOdsTombstoneMetadata()
                .getOdsTombstonedDate().toInstant() : null)
        .set(MACHINE_ANNOTATION_SERVICE.DATA, mapToJSONB(mas))
        .execute();
  }

  private JSONB mapToJSONB(MachineAnnotationService mas) throws JsonProcessingException {
    return JSONB.valueOf(MAPPER.writeValueAsString(mas));
  }

}
