package eu.dissco.backend.repository;

import static eu.dissco.backend.TestUtils.HANDLE;
import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.ID_ALT;
import static eu.dissco.backend.TestUtils.SOURCE_SYSTEM_ID_1;
import static eu.dissco.backend.TestUtils.givenDigitalSpecimen;
import static eu.dissco.backend.database.jooq.Tables.NEW_DIGITAL_MEDIA_OBJECT;
import static eu.dissco.backend.database.jooq.Tables.NEW_DIGITAL_SPECIMEN;
import static eu.dissco.backend.utils.DigitalMediaObjectUtils.givenDigitalMediaObject;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.domain.DigitalMediaObject;
import eu.dissco.backend.domain.DigitalSpecimen;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import java.util.ArrayList;
import java.util.List;
import org.jooq.JSONB;
import org.jooq.Query;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DigitalMediaObjectRepositoryIT extends BaseRepositoryIT {

  private DigitalMediaObjectRepository repository;

  @BeforeEach
  void setup() {
    ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
    mapper.setSerializationInclusion(Include.NON_NULL);
    repository = new DigitalMediaObjectRepository(mapper, context);
  }

  @AfterEach
  void destroy() {
    context.truncate(NEW_DIGITAL_SPECIMEN).execute();
    context.truncate(NEW_DIGITAL_MEDIA_OBJECT).execute();
  }

  @Test
  void testGetDigitalMediaObjects() throws JsonProcessingException {
    // Given
    int pageNum1 = 1;
    int pageNum2 = 2;
    int pageSize = 10;
    String specimenId = ID_ALT;
    var specimen = givenDigitalSpecimen(specimenId);
    postDigitalSpecimen(specimen);
    List<DigitalMediaObject> mediaObjectsAll = new ArrayList<>();
    for (int i = 0; i < pageSize * 2; i++) {
      mediaObjectsAll.add(givenDigitalMediaObject(String.valueOf(i), specimenId));
    }
    postMediaObjects(mediaObjectsAll);
    List<DigitalMediaObject> mediaObjectsReceived = new ArrayList<>();

    // When
    var pageOne = repository.getDigitalMediaObjects(pageNum1, pageSize);
    var pageTwo = repository.getDigitalMediaObjects(pageNum2, pageSize);
    mediaObjectsReceived.addAll(pageOne);
    mediaObjectsReceived.addAll(pageTwo);

    // Then
    assertThat(pageOne).hasSize(pageSize + 1);
    assertThat(pageTwo).hasSize(pageSize);
    assertThat(mediaObjectsReceived).hasSameElementsAs(mediaObjectsAll.stream().map(
        media -> givenDigitalMediaObject(HANDLE + media.id(), HANDLE + media.digitalSpecimenId(),
            HANDLE + SOURCE_SYSTEM_ID_1)).toList());
  }

  @Test
  void testGetLatestDigitalMediaObjectById() throws JsonProcessingException {
    var firstMediaObject = givenDigitalMediaObject(ID, ID_ALT);
    var secondMediaObject = givenDigitalMediaObject(ID, ID_ALT, 2);

    postMediaObjects(List.of(firstMediaObject, secondMediaObject));
    var specimen = givenDigitalSpecimen(ID_ALT);
    postDigitalSpecimen(specimen);

    // When
    var receivedResponse = repository.getLatestDigitalMediaObjectById(ID);

    // Then
    assertThat(receivedResponse).isEqualTo(
        givenDigitalMediaObject(HANDLE + ID, HANDLE + ID_ALT, HANDLE + SOURCE_SYSTEM_ID_1, 2));
  }

  @Test
  void testGetDigitalMediaForSpecimen() throws JsonProcessingException {
    // Given
    String specimenId = ID_ALT;
    List<DigitalMediaObject> postedMediaObjects = List.of(
        givenDigitalMediaObject(ID, specimenId),
        givenDigitalMediaObject("aa", specimenId));
    postMediaObjects(postedMediaObjects);

    var specimen = givenDigitalSpecimen(specimenId);
    postDigitalSpecimen(specimen);

    // When
    var receivedResponse = repository.getDigitalMediaForSpecimen(specimenId);

    // Then
    assertThat(receivedResponse).hasSameElementsAs(
        List.of(
            givenDigitalMediaObject(HANDLE + ID, HANDLE + specimenId, HANDLE + SOURCE_SYSTEM_ID_1),
            givenDigitalMediaObject(HANDLE + "aa", HANDLE + specimenId,
                HANDLE + SOURCE_SYSTEM_ID_1)));
  }

  @Test
  void testGetDigitalMediaIdsForSpecimen() throws JsonProcessingException {
    // Given
    List<String> expectedResponse = List.of(ID, ID_ALT);
    String specimenId = "specimenId";
    var specimen = givenDigitalSpecimen(specimenId);
    postDigitalSpecimen(specimen);
    List<DigitalMediaObject> mediaObjects = List.of(
        givenDigitalMediaObject(ID, specimenId, HANDLE + SOURCE_SYSTEM_ID_1),
        givenDigitalMediaObject(ID_ALT, specimenId, HANDLE + SOURCE_SYSTEM_ID_1));
    postMediaObjects(mediaObjects);

    // When
    var receivedResponse = repository.getDigitalMediaIdsForSpecimen(specimenId);

    // Then
    assertThat(receivedResponse).hasSameElementsAs(expectedResponse);
  }

  private void postMediaObjects(List<DigitalMediaObject> mediaObjects) {
    List<Query> queryList = new ArrayList<>();
    for (DigitalMediaObject mediaObject : mediaObjects) {
      var query = context.insertInto(NEW_DIGITAL_MEDIA_OBJECT)
          .set(NEW_DIGITAL_MEDIA_OBJECT.ID, mediaObject.id())
          .set(NEW_DIGITAL_MEDIA_OBJECT.VERSION, mediaObject.version())
          .set(NEW_DIGITAL_MEDIA_OBJECT.TYPE, mediaObject.type())
          .set(NEW_DIGITAL_MEDIA_OBJECT.CREATED, mediaObject.created())
          .set(NEW_DIGITAL_MEDIA_OBJECT.DIGITAL_SPECIMEN_ID, mediaObject.digitalSpecimenId())
          .set(NEW_DIGITAL_MEDIA_OBJECT.MEDIA_URL, mediaObject.mediaUrl())
          .set(NEW_DIGITAL_MEDIA_OBJECT.FORMAT, mediaObject.format())
          .set(NEW_DIGITAL_MEDIA_OBJECT.SOURCE_SYSTEM_ID, mediaObject.sourceSystemId())
          .set(NEW_DIGITAL_MEDIA_OBJECT.DATA, JSONB.jsonb(mediaObject.data().toString()))
          .set(NEW_DIGITAL_MEDIA_OBJECT.ORIGINAL_DATA,
              JSONB.jsonb(mediaObject.originalData().toString()))
          .set(NEW_DIGITAL_MEDIA_OBJECT.LAST_CHECKED, mediaObject.created())
          .onConflict(NEW_DIGITAL_SPECIMEN.ID).doUpdate()
          .set(NEW_DIGITAL_MEDIA_OBJECT.VERSION, mediaObject.version())
          .set(NEW_DIGITAL_MEDIA_OBJECT.TYPE, mediaObject.type())
          .set(NEW_DIGITAL_MEDIA_OBJECT.CREATED, mediaObject.created())
          .set(NEW_DIGITAL_MEDIA_OBJECT.DIGITAL_SPECIMEN_ID, mediaObject.digitalSpecimenId())
          .set(NEW_DIGITAL_MEDIA_OBJECT.MEDIA_URL, mediaObject.mediaUrl())
          .set(NEW_DIGITAL_MEDIA_OBJECT.FORMAT, mediaObject.format())
          .set(NEW_DIGITAL_MEDIA_OBJECT.SOURCE_SYSTEM_ID, mediaObject.sourceSystemId())
          .set(NEW_DIGITAL_MEDIA_OBJECT.DATA, JSONB.jsonb(mediaObject.data().toString()))
          .set(NEW_DIGITAL_MEDIA_OBJECT.ORIGINAL_DATA,
              JSONB.jsonb(mediaObject.originalData().toString()))
          .set(NEW_DIGITAL_MEDIA_OBJECT.LAST_CHECKED, mediaObject.created());
      queryList.add(query);
    }
    context.batch(queryList).execute();
  }

  private void postDigitalSpecimen(DigitalSpecimen specimen) {
    context.insertInto(NEW_DIGITAL_SPECIMEN).set(NEW_DIGITAL_SPECIMEN.ID, specimen.id())
        .set(NEW_DIGITAL_SPECIMEN.VERSION, specimen.version())
        .set(NEW_DIGITAL_SPECIMEN.TYPE, specimen.type())
        .set(NEW_DIGITAL_SPECIMEN.MIDSLEVEL, (short) specimen.midsLevel())
        .set(NEW_DIGITAL_SPECIMEN.PHYSICAL_SPECIMEN_ID, specimen.physicalSpecimenId())
        .set(NEW_DIGITAL_SPECIMEN.PHYSICAL_SPECIMEN_TYPE, specimen.physicalSpecimenIdType())
        .set(NEW_DIGITAL_SPECIMEN.SPECIMEN_NAME, specimen.specimenName())
        .set(NEW_DIGITAL_SPECIMEN.ORGANIZATION_ID, specimen.organisationId())
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
