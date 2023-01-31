package eu.dissco.backend.repository;

import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.ID_ALT;
import static eu.dissco.backend.TestUtils.givenDigitalMediaObject;
import static eu.dissco.backend.TestUtils.givenDigitalSpecimen;
import static eu.dissco.backend.TestUtils.givenMediaObjectJsonApiDataWithSpeciesName;
import static eu.dissco.backend.database.jooq.Tables.NEW_DIGITAL_MEDIA_OBJECT;
import static eu.dissco.backend.database.jooq.Tables.NEW_DIGITAL_SPECIMEN;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.domain.DigitalMediaObject;
import eu.dissco.backend.domain.DigitalSpecimen;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.jooq.JSONB;
import org.jooq.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
class DigitalMediaObjectRepositoryIT extends BaseRepositoryIT {

  private DigitalMediaObjectRepository repository;

  @BeforeEach
  void setup() {
    ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
    mapper.setSerializationInclusion(Include.NON_NULL);
    repository = new DigitalMediaObjectRepository(mapper, context);
  }

  @Test
  void testGetLatestDigitalMediaById(){
    // Given
    var firstMediaObject = givenDigitalMediaObject(ID);
    var secondMediaObject = new DigitalMediaObject(
        firstMediaObject.id(),
        firstMediaObject.version() + 1,
        firstMediaObject.created(),
        firstMediaObject.type(),
        firstMediaObject.digitalSpecimenId(),
        firstMediaObject.mediaUrl(),
        firstMediaObject.format(),
        firstMediaObject.sourceSystemId(),
        firstMediaObject.data(),
        firstMediaObject.originalData()
    );
    postMediaObjects(List.of(firstMediaObject, secondMediaObject));

    // When
    var receivedResponse = repository.getLatestDigitalMediaById(ID);

    // Then
    assertThat(receivedResponse).isEqualTo(secondMediaObject);
  }

  @Test
  void testGetLatestDigitalMediaObjectByIdJsonResponse(){
    var firstMediaObject = givenDigitalMediaObject(ID, ID_ALT);
    var secondMediaObject = new DigitalMediaObject(
        firstMediaObject.id(),
        firstMediaObject.version() + 1,
        firstMediaObject.created(),
        firstMediaObject.type(),
        firstMediaObject.digitalSpecimenId(),
        firstMediaObject.mediaUrl(),
        firstMediaObject.format(),
        firstMediaObject.sourceSystemId(),
        firstMediaObject.data(),
        firstMediaObject.originalData()
    );

    postMediaObjects(List.of(firstMediaObject, secondMediaObject));
    var specimen = givenDigitalSpecimen(ID_ALT);
    postDigitalSpecimen(List.of(specimen));

    var expectedResponse = givenMediaObjectJsonApiDataWithSpeciesName(secondMediaObject, specimen);

    // When
    var receivedResponse = repository.getLatestDigitalMediaObjectByIdJsonResponse(ID);

    // Then
    assertThat(receivedResponse).isEqualTo(expectedResponse);
  }

  @Test
  void testGetForDigitalSpecimen(){
    // Given
    String specimenId = ID_ALT;
    var firstMediaObject = givenDigitalMediaObject(ID, specimenId);
    var secondMediaObject = givenDigitalMediaObject("aa", specimenId);
    List<DigitalMediaObject> expectedResponse = List.of(firstMediaObject, secondMediaObject);
    postMediaObjects(expectedResponse);
    var specimen = givenDigitalSpecimen(specimenId);
    postDigitalSpecimen(List.of(specimen));

    // When
    var receivedResponse = repository.getForDigitalSpecimen(specimenId);

    // Then
    assertThat(receivedResponse).hasSameElementsAs(expectedResponse);
  }

  @Test
  void testGetDigitalMediaVersions(){
    // Given
    var firstMediaObject = givenDigitalMediaObject(ID, ID_ALT);
    var secondMediaObject = new DigitalMediaObject(
        firstMediaObject.id(),
        firstMediaObject.version() + 1,
        firstMediaObject.created(),
        firstMediaObject.type(),
        firstMediaObject.digitalSpecimenId(),
        firstMediaObject.mediaUrl(),
        firstMediaObject.format(),
        firstMediaObject.sourceSystemId(),
        firstMediaObject.data(),
        firstMediaObject.originalData()
    );
    List<Integer> expectedResponse = List.of(1, 2);
    postMediaObjects(List.of(firstMediaObject, secondMediaObject));

    // When
    var receivedResponse = repository.getDigitalMediaVersions(ID);

    // Then
    assertThat(receivedResponse).hasSameElementsAs(expectedResponse);
  }

  @Test
  void testGetDigitalMediaByVersion(){
    int targetVersion = 2;
    var firstMediaObject = givenDigitalMediaObject(ID, ID_ALT);
    var expectedResponse = new DigitalMediaObject(
        firstMediaObject.id(),
        targetVersion,
        firstMediaObject.created(),
        firstMediaObject.type(),
        firstMediaObject.digitalSpecimenId(),
        firstMediaObject.mediaUrl(),
        firstMediaObject.format(),
        firstMediaObject.sourceSystemId(),
        firstMediaObject.data(),
        firstMediaObject.originalData()
    );
    postMediaObjects(List.of(firstMediaObject, expectedResponse));

    // When
    var receivedResponse = repository.getDigitalMediaByVersion(ID, targetVersion);

    // Then
    assertThat(receivedResponse).isEqualTo(expectedResponse);
  }

  @Test
  void testGetDigitalMediaByVersionJsonResponse(){
    int targetVersion = 2;
    var firstMediaObject = givenDigitalMediaObject(ID, ID_ALT);
    var secondMediaObject = new DigitalMediaObject(
        firstMediaObject.id(),
        targetVersion,
        firstMediaObject.created(),
        firstMediaObject.type(),
        firstMediaObject.digitalSpecimenId(),
        firstMediaObject.mediaUrl(),
        firstMediaObject.format(),
        firstMediaObject.sourceSystemId(),
        firstMediaObject.data(),
        firstMediaObject.originalData()
    );
    postMediaObjects(List.of(firstMediaObject, secondMediaObject));

    var specimen = givenDigitalSpecimen(ID_ALT);
    postDigitalSpecimen(List.of(specimen));

    var expectedResponse = givenMediaObjectJsonApiDataWithSpeciesName(secondMediaObject, specimen);

    // When
    var receivedResponse = repository.getDigitalMediaByVersionJsonResponse(ID, targetVersion);

    // Then
    assertThat(receivedResponse).isEqualTo(expectedResponse);
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
          .set(NEW_DIGITAL_MEDIA_OBJECT.LAST_CHECKED, mediaObject.created());
      queryList.add(query);
    }
    context.batch(queryList).execute();
  }

  private void postDigitalSpecimen(List<DigitalSpecimen> specimens){
    List<Query> queryList = new ArrayList<>();
    for (DigitalSpecimen specimen: specimens){
      var query = context.insertInto(NEW_DIGITAL_SPECIMEN)
          .set(NEW_DIGITAL_SPECIMEN.ID, specimen.id())
          .set(NEW_DIGITAL_SPECIMEN.VERSION, specimen.version())
          .set(NEW_DIGITAL_SPECIMEN.TYPE, specimen.type())
          .set(NEW_DIGITAL_SPECIMEN.MIDSLEVEL, (short) specimen.midsLevel())
          .set(NEW_DIGITAL_SPECIMEN.PHYSICAL_SPECIMEN_ID, specimen.physicalSpecimenId())
          .set(NEW_DIGITAL_SPECIMEN.PHYSICAL_SPECIMEN_TYPE, specimen.physicalSpecimenIdType())
          .set(NEW_DIGITAL_SPECIMEN.SPECIMEN_NAME, specimen.specimenName())
          .set(NEW_DIGITAL_SPECIMEN.ORGANIZATION_ID, specimen.organizationId())
          .set(NEW_DIGITAL_SPECIMEN.PHYSICAL_SPECIMEN_COLLECTION, specimen.physicalSpecimenCollection())
          .set(NEW_DIGITAL_SPECIMEN.DATASET, specimen.datasetId())
          .set(NEW_DIGITAL_SPECIMEN.SOURCE_SYSTEM_ID, specimen.sourceSystemId())
          .set(NEW_DIGITAL_SPECIMEN.CREATED, specimen.created())
          .set(NEW_DIGITAL_SPECIMEN.LAST_CHECKED, specimen.created())
          .set(NEW_DIGITAL_SPECIMEN.DATA, JSONB.jsonb(specimen.data().toString()))
          .set(NEW_DIGITAL_SPECIMEN.ORIGINAL_DATA, JSONB.jsonb(specimen.originalData().toString()))
          .set(NEW_DIGITAL_SPECIMEN.DWCA_ID, specimen.dwcaId());
      queryList.add(query);
    }
    context.batch(queryList).execute();
  }
}