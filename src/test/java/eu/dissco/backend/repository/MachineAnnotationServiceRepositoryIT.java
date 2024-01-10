package eu.dissco.backend.repository;

import static eu.dissco.backend.TestUtils.CREATED;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.PREFIX;
import static eu.dissco.backend.database.jooq.Tables.MACHINE_ANNOTATION_SERVICES;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.dissco.backend.domain.MachineAnnotationServiceRecord;
import eu.dissco.backend.utils.MachineAnnotationServiceUtils;
import java.util.ArrayList;
import java.util.List;
import org.jooq.JSONB;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.trilead.ssh2.crypto.digest.MAC;

class MachineAnnotationServiceRepositoryIT extends BaseRepositoryIT {

  private MachineAnnotationServiceRepository repository;

  @BeforeEach
  void setup() {
    repository = new MachineAnnotationServiceRepository(context, MAPPER);
  }

  @AfterEach
  void destroy() {
    context.truncate(MACHINE_ANNOTATION_SERVICES).execute();
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
    var result = repository.getMasRecords(
        List.of(PREFIX + "ABC-123-XY14", PREFIX + "ABC-123-XY18"));

    // Then
    assertThat(result).hasSize(2);
  }

  private List<MachineAnnotationServiceRecord> populateDatabase() throws JsonProcessingException {
    var expectedRecords = new ArrayList<MachineAnnotationServiceRecord>();
    for (int i = 0; i < 26; i++) {
      MachineAnnotationServiceRecord masRecord;
      if (i % 2 == 0) {
        masRecord = MachineAnnotationServiceUtils.givenMasRecord(PREFIX + "ABC-123-XY" + i, null);
        expectedRecords.add(masRecord);
      } else {
        masRecord = MachineAnnotationServiceUtils.givenMasRecord(PREFIX + "ABC-123-XY" + i,
            CREATED);
      }
      createRecord(masRecord);
    }
    return expectedRecords;
  }

  private void createRecord(MachineAnnotationServiceRecord masRecord)
      throws JsonProcessingException {
    context.insertInto(MACHINE_ANNOTATION_SERVICES)
        .set(MACHINE_ANNOTATION_SERVICES.ID, masRecord.id())
        .set(MACHINE_ANNOTATION_SERVICES.VERSION, masRecord.version())
        .set(MACHINE_ANNOTATION_SERVICES.NAME, masRecord.mas().name())
        .set(MACHINE_ANNOTATION_SERVICES.CREATED, masRecord.created())
        .set(MACHINE_ANNOTATION_SERVICES.ADMINISTRATOR, masRecord.administrator())
        .set(MACHINE_ANNOTATION_SERVICES.CONTAINER_IMAGE, masRecord.mas().containerImage())
        .set(MACHINE_ANNOTATION_SERVICES.CONTAINER_IMAGE_TAG, masRecord.mas().containerTag())
        .set(MACHINE_ANNOTATION_SERVICES.TARGET_DIGITAL_OBJECT_FILTERS,
            masRecord.mas().targetDigitalObjectFilters() != null ? JSONB.jsonb(
                masRecord.mas().targetDigitalObjectFilters().toString()) : null
        )
        .set(MACHINE_ANNOTATION_SERVICES.SERVICE_DESCRIPTION,
            masRecord.mas().serviceDescription())
        .set(MACHINE_ANNOTATION_SERVICES.SERVICE_STATE, masRecord.mas().serviceState())
        .set(MACHINE_ANNOTATION_SERVICES.SERVICE_AVAILABILITY,
            masRecord.mas().serviceAvailability())
        .set(MACHINE_ANNOTATION_SERVICES.SOURCE_CODE_REPOSITORY,
            masRecord.mas().sourceCodeRepository())
        .set(MACHINE_ANNOTATION_SERVICES.CODE_MAINTAINER, masRecord.mas().codeMaintainer())
        .set(MACHINE_ANNOTATION_SERVICES.CODE_LICENSE, masRecord.mas().codeLicense())
        .set(MACHINE_ANNOTATION_SERVICES.DEPENDENCIES,
            masRecord.mas().dependencies() != null ? masRecord.mas().dependencies()
                .toArray(new String[0]) : null
        )
        .set(MACHINE_ANNOTATION_SERVICES.SUPPORT_CONTACT, masRecord.mas().supportContact())
        .set(MACHINE_ANNOTATION_SERVICES.SLA_DOCUMENTATION, masRecord.mas().slaDocumentation())
        .set(MACHINE_ANNOTATION_SERVICES.TOPICNAME, masRecord.mas().topicName())
        .set(MACHINE_ANNOTATION_SERVICES.MAXREPLICAS, masRecord.mas().maxReplicas())
        .set(MACHINE_ANNOTATION_SERVICES.DELETED_ON, masRecord.deleted())
        .set(MACHINE_ANNOTATION_SERVICES.MAS_INPUT, JSONB.jsonb(MAPPER.writeValueAsString(masRecord.mas().masInput())))
        .execute();
  }
}
