package eu.dissco.backend.repository;

import static eu.dissco.backend.TestUtils.DOI;
import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.ID_ALT;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.givenDigitalSpecimenWrapper;
import static eu.dissco.backend.database.jooq.Tables.DIGITAL_MEDIA_OBJECT;
import static eu.dissco.backend.database.jooq.Tables.DIGITAL_SPECIMEN;
import static eu.dissco.backend.utils.DigitalMediaObjectUtils.givenDigitalMediaObject;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.schema.DigitalMedia;
import eu.dissco.backend.schema.DigitalSpecimen;
import java.util.ArrayList;
import java.util.List;
import org.jooq.JSONB;
import org.jooq.Query;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DigitalMediaRepositoryIT extends BaseRepositoryIT {

  private DigitalMediaRepository repository;

  @BeforeEach
  void setup() {
    ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
    mapper.setSerializationInclusion(Include.NON_NULL);
    repository = new DigitalMediaRepository(mapper, context);
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
    List<DigitalMedia> mediaObjectsAll = new ArrayList<>();
    for (int i = 0; i < pageSize * 2; i++) {
      mediaObjectsAll.add(givenDigitalMediaObject(String.valueOf(i), specimenId));
    }
    postMediaObjects(mediaObjectsAll);
    List<DigitalMedia> mediaObjectsReceived = new ArrayList<>();

    // When
    var pageOne = repository.getDigitalMediaObjects(pageNum1, pageSize);
    var pageTwo = repository.getDigitalMediaObjects(pageNum2, pageSize);
    mediaObjectsReceived.addAll(pageOne);
    mediaObjectsReceived.addAll(pageTwo);

    // Then
    assertThat(pageOne).hasSize(pageSize + 1);
    assertThat(pageTwo).hasSize(pageSize);
    assertThat(mediaObjectsReceived).hasSameElementsAs(mediaObjectsAll.stream().map(
        media -> givenDigitalMediaObject(DOI + media.getDctermsIdentifier(), specimenId)).toList());
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
        givenDigitalMediaObject(DOI + ID, ID_ALT, 2));
  }

  @Test
  void testGetOriginalMediaData() throws JsonProcessingException{
    // Given
    var expected = MAPPER.createObjectNode()
        .put("originalData", "yep");
    postMediaObjects(List.of(givenDigitalMediaObject(ID)));
    context.update(DIGITAL_MEDIA_OBJECT)
        .set(DIGITAL_MEDIA_OBJECT.ORIGINAL_DATA, JSONB.jsonb(MAPPER.writeValueAsString(expected)))
        .where(DIGITAL_MEDIA_OBJECT.ID.eq(ID))
        .execute();

    // When
    var result = repository.getMediaOriginalData(ID);

    // Then
    assertThat(result).isEqualTo(expected);
  }

  private void postMediaObjects(List<DigitalMedia> mediaObjects)
      throws JsonProcessingException {
    List<Query> queryList = new ArrayList<>();
    for (DigitalMedia mediaObject : mediaObjects) {
      var specimenId = mediaObject.getOdsHasEntityRelationships().get(0)
          .getDwcRelatedResourceID();
      var query = context.insertInto(DIGITAL_MEDIA_OBJECT)
          .set(DIGITAL_MEDIA_OBJECT.ID, mediaObject.getDctermsIdentifier())
          .set(DIGITAL_MEDIA_OBJECT.VERSION, mediaObject.getOdsVersion())
          .set(DIGITAL_MEDIA_OBJECT.TYPE, mediaObject.getOdsFdoType())
          .set(DIGITAL_MEDIA_OBJECT.CREATED, mediaObject.getDctermsCreated().toInstant())
          .set(DIGITAL_MEDIA_OBJECT.DIGITAL_SPECIMEN_ID, specimenId)
          .set(DIGITAL_MEDIA_OBJECT.MEDIA_URL, mediaObject.getAcAccessURI())
          .set(DIGITAL_MEDIA_OBJECT.DATA,
              JSONB.jsonb(MAPPER.writeValueAsString(mediaObject)))
          .set(DIGITAL_MEDIA_OBJECT.ORIGINAL_DATA, JSONB.valueOf("{}"))
          .set(DIGITAL_MEDIA_OBJECT.LAST_CHECKED,
              mediaObject.getDctermsCreated().toInstant())
          .onConflict(DIGITAL_SPECIMEN.ID).doUpdate()
          .set(DIGITAL_MEDIA_OBJECT.ID, mediaObject.getDctermsIdentifier())
          .set(DIGITAL_MEDIA_OBJECT.VERSION, mediaObject.getOdsVersion())
          .set(DIGITAL_MEDIA_OBJECT.TYPE, mediaObject.getOdsFdoType())
          .set(DIGITAL_MEDIA_OBJECT.CREATED,
              mediaObject.getDctermsCreated().toInstant())
          .set(DIGITAL_MEDIA_OBJECT.DIGITAL_SPECIMEN_ID, specimenId)
          .set(DIGITAL_MEDIA_OBJECT.MEDIA_URL, mediaObject.getAcAccessURI())
          .set(DIGITAL_MEDIA_OBJECT.DATA,
              JSONB.jsonb(MAPPER.writeValueAsString(mediaObject)))
          .set(DIGITAL_MEDIA_OBJECT.ORIGINAL_DATA, JSONB.valueOf("{}"))
          .set(DIGITAL_MEDIA_OBJECT.LAST_CHECKED,
              mediaObject.getDctermsCreated().toInstant());
      queryList.add(query);
    }
    context.batch(queryList).execute();
  }

  private void postDigitalSpecimen(DigitalSpecimen digitalSpecimen)
      throws JsonProcessingException {
    context.insertInto(DIGITAL_SPECIMEN)
        .set(DIGITAL_SPECIMEN.ID, digitalSpecimen.getDctermsIdentifier())
        .set(DIGITAL_SPECIMEN.VERSION, digitalSpecimen.getOdsVersion())
        .set(DIGITAL_SPECIMEN.TYPE, digitalSpecimen.getOdsFdoType())
        .set(DIGITAL_SPECIMEN.MIDSLEVEL,
            digitalSpecimen.getOdsMidsLevel().shortValue())
        .set(DIGITAL_SPECIMEN.PHYSICAL_SPECIMEN_ID,
            digitalSpecimen.getOdsPhysicalSpecimenID())
        .set(DIGITAL_SPECIMEN.PHYSICAL_SPECIMEN_TYPE,
            digitalSpecimen.getOdsPhysicalSpecimenIDType().value())
        .set(DIGITAL_SPECIMEN.SPECIMEN_NAME, digitalSpecimen.getOdsSpecimenName())
        .set(DIGITAL_SPECIMEN.ORGANIZATION_ID,
            digitalSpecimen.getOdsOrganisationID())
        .set(DIGITAL_SPECIMEN.SOURCE_SYSTEM_ID,
            digitalSpecimen.getOdsSourceSystemID())
        .set(DIGITAL_SPECIMEN.CREATED,
            digitalSpecimen.getDctermsCreated().toInstant())
        .set(DIGITAL_SPECIMEN.LAST_CHECKED, digitalSpecimen.getDctermsCreated().toInstant())
        .set(DIGITAL_SPECIMEN.DATA, JSONB.jsonb(
            MAPPER.writeValueAsString(digitalSpecimen)))
        .execute();
  }

}
