package eu.dissco.backend.repository;

import static eu.dissco.backend.TestUtils.DOI;
import static eu.dissco.backend.TestUtils.HANDLE;
import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.ID_ALT;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.SOURCE_SYSTEM_ID_1;
import static eu.dissco.backend.TestUtils.givenDigitalSpecimenWrapper;
import static eu.dissco.backend.database.jooq.Tables.DIGITAL_MEDIA_OBJECT;
import static eu.dissco.backend.database.jooq.Tables.DIGITAL_SPECIMEN;
import static eu.dissco.backend.utils.DigitalMediaObjectUtils.givenDigitalMediaObject;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.domain.DigitalMediaObjectWrapper;
import eu.dissco.backend.domain.DigitalSpecimenWrapper;
import java.time.Instant;
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
    context.truncate(DIGITAL_SPECIMEN).execute();
    context.truncate(DIGITAL_MEDIA_OBJECT).execute();
  }

  @Test
  void testGetDigitalMediaObjects() throws JsonProcessingException {
    // Given
    int pageNum1 = 1;
    int pageNum2 = 2;
    int pageSize = 10;
    String specimenId = ID_ALT;
    var specimen = givenDigitalSpecimenWrapper(specimenId);
    postDigitalSpecimen(specimen);
    List<DigitalMediaObjectWrapper> mediaObjectsAll = new ArrayList<>();
    for (int i = 0; i < pageSize * 2; i++) {
      mediaObjectsAll.add(givenDigitalMediaObject(String.valueOf(i), specimenId));
    }
    postMediaObjects(mediaObjectsAll);
    List<DigitalMediaObjectWrapper> mediaObjectsReceived = new ArrayList<>();

    // When
    var pageOne = repository.getDigitalMediaObjects(pageNum1, pageSize);
    var pageTwo = repository.getDigitalMediaObjects(pageNum2, pageSize);
    mediaObjectsReceived.addAll(pageOne);
    mediaObjectsReceived.addAll(pageTwo);

    // Then
    assertThat(pageOne).hasSize(pageSize + 1);
    assertThat(pageTwo).hasSize(pageSize);
    assertThat(mediaObjectsReceived).hasSameElementsAs(mediaObjectsAll.stream().map(
        media -> givenDigitalMediaObject(DOI + media.digitalEntity().getOdsId(),
            specimenId, SOURCE_SYSTEM_ID_1)).toList());
  }

  @Test
  void testGetLatestDigitalMediaObjectById() throws JsonProcessingException {
    var firstMediaObject = givenDigitalMediaObject(ID, ID_ALT);
    var secondMediaObject = givenDigitalMediaObject(ID, ID_ALT, 2);

    postMediaObjects(List.of(firstMediaObject, secondMediaObject));
    var specimen = givenDigitalSpecimenWrapper(ID_ALT);
    postDigitalSpecimen(specimen);

    // When
    var receivedResponse = repository.getLatestDigitalMediaObjectById(ID);

    // Then
    assertThat(receivedResponse).isEqualTo(
        givenDigitalMediaObject(DOI + ID, ID_ALT, SOURCE_SYSTEM_ID_1, 2));
  }

  @Test
  void testGetDigitalMediaForSpecimen() throws JsonProcessingException {
    // Given
    String specimenId = ID_ALT;
    List<DigitalMediaObjectWrapper> postedMediaObjects = List.of(
        givenDigitalMediaObject(ID, specimenId),
        givenDigitalMediaObject("aa", specimenId));
    postMediaObjects(postedMediaObjects);

    var specimen = givenDigitalSpecimenWrapper(specimenId);
    postDigitalSpecimen(specimen);

    // When
    var receivedResponse = repository.getDigitalMediaForSpecimen(specimenId);

    // Then
    assertThat(receivedResponse).hasSameElementsAs(
        List.of(
            givenDigitalMediaObject(DOI + ID, specimenId, SOURCE_SYSTEM_ID_1),
            givenDigitalMediaObject(DOI + "aa", specimenId, SOURCE_SYSTEM_ID_1)));
  }

  @Test
  void testGetDigitalMediaIdsForSpecimen() throws JsonProcessingException {
    // Given
    List<String> expectedResponse = List.of(ID, ID_ALT);
    String specimenId = "specimenId";
    var specimen = givenDigitalSpecimenWrapper(specimenId);
    postDigitalSpecimen(specimen);
    List<DigitalMediaObjectWrapper> mediaObjects = List.of(
        givenDigitalMediaObject(ID, specimenId, HANDLE + SOURCE_SYSTEM_ID_1),
        givenDigitalMediaObject(ID_ALT, specimenId, HANDLE + SOURCE_SYSTEM_ID_1));
    postMediaObjects(mediaObjects);

    // When
    var receivedResponse = repository.getDigitalMediaIdsForSpecimen(specimenId);

    // Then
    assertThat(receivedResponse).hasSameElementsAs(expectedResponse);
  }

  private void postMediaObjects(List<DigitalMediaObjectWrapper> mediaObjects)
      throws JsonProcessingException {
    List<Query> queryList = new ArrayList<>();
    for (DigitalMediaObjectWrapper mediaObject : mediaObjects) {
      var specimenId = mediaObject.digitalEntity().getEntityRelationships().get(0)
          .getObjectEntityIri();
      var query = context.insertInto(DIGITAL_MEDIA_OBJECT)
          .set(DIGITAL_MEDIA_OBJECT.ID, mediaObject.digitalEntity().getOdsId())
          .set(DIGITAL_MEDIA_OBJECT.VERSION, mediaObject.digitalEntity().getOdsVersion())
          .set(DIGITAL_MEDIA_OBJECT.TYPE, mediaObject.digitalEntity().getOdsType())
          .set(DIGITAL_MEDIA_OBJECT.CREATED,
              Instant.parse(mediaObject.digitalEntity().getOdsCreated()))
          .set(DIGITAL_MEDIA_OBJECT.DIGITAL_SPECIMEN_ID, specimenId)
          .set(DIGITAL_MEDIA_OBJECT.MEDIA_URL, mediaObject.digitalEntity().getAcAccessUri())
          .set(DIGITAL_MEDIA_OBJECT.DATA,
              JSONB.jsonb(MAPPER.writeValueAsString(mediaObject.digitalEntity())))
          .set(DIGITAL_MEDIA_OBJECT.ORIGINAL_DATA,
              JSONB.jsonb(mediaObject.originalData().toString()))
          .set(DIGITAL_MEDIA_OBJECT.LAST_CHECKED,
              Instant.parse(mediaObject.digitalEntity().getOdsCreated()))
          .onConflict(DIGITAL_SPECIMEN.ID).doUpdate()
          .set(DIGITAL_MEDIA_OBJECT.ID, mediaObject.digitalEntity().getOdsId())
          .set(DIGITAL_MEDIA_OBJECT.VERSION, mediaObject.digitalEntity().getOdsVersion())
          .set(DIGITAL_MEDIA_OBJECT.TYPE, mediaObject.digitalEntity().getOdsType())
          .set(DIGITAL_MEDIA_OBJECT.CREATED,
              Instant.parse(mediaObject.digitalEntity().getOdsCreated()))
          .set(DIGITAL_MEDIA_OBJECT.DIGITAL_SPECIMEN_ID, specimenId)
          .set(DIGITAL_MEDIA_OBJECT.MEDIA_URL, mediaObject.digitalEntity().getAcAccessUri())
          .set(DIGITAL_MEDIA_OBJECT.DATA,
              JSONB.jsonb(MAPPER.writeValueAsString(mediaObject.digitalEntity())))
          .set(DIGITAL_MEDIA_OBJECT.ORIGINAL_DATA,
              JSONB.jsonb(mediaObject.originalData().toString()))
          .set(DIGITAL_MEDIA_OBJECT.LAST_CHECKED,
              Instant.parse(mediaObject.digitalEntity().getOdsCreated()));
      queryList.add(query);
    }
    context.batch(queryList).execute();
  }

  private void postDigitalSpecimen(DigitalSpecimenWrapper specimenWrapper)
      throws JsonProcessingException {
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
        .set(DIGITAL_SPECIMEN.SPECIMEN_NAME, specimenWrapper.digitalSpecimen().getOdsSpecimenName())
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
        .set(DIGITAL_SPECIMEN.ORIGINAL_DATA, JSONB.jsonb(specimenWrapper.originalData().toString()))
        .execute();
  }

}
