package eu.dissco.backend.service;

import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.SANDBOX_URI;
import static eu.dissco.backend.TestUtils.givenAnnotationResponse;
import static eu.dissco.backend.TestUtils.givenDigitalMediaJsonApiData;
import static eu.dissco.backend.TestUtils.givenDigitalMediaObject;
import static eu.dissco.backend.TestUtils.givenJsonApiLinksFull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import eu.dissco.backend.domain.DigitalMediaObject;
import eu.dissco.backend.domain.DigitalMediaObjectFull;
import eu.dissco.backend.domain.JsonApiLinks;
import eu.dissco.backend.domain.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.JsonApiWrapper;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.repository.DigitalMediaObjectRepository;
import eu.dissco.backend.repository.MongoRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DigitalMediaObjectServiceTest {

  @Mock
  private DigitalMediaObjectRepository repository;
  @Mock
  private AnnotationService annotationService;
  @Mock
  private MongoRepository mongoRepository;

  private DigitalMediaObjectService service;

  @BeforeEach
  void setup() {
    service = new DigitalMediaObjectService(repository, annotationService, mongoRepository);
  }

  @Test
  void testGetDigitalMediaById() {
    // Given
    var responseExpected = givenDigitalMediaObject(ID);
    given(repository.getLatestDigitalMediaById(ID)).willReturn(responseExpected);

    // When
    var responseReceived = service.getDigitalMediaById(ID);

    // Then
    assertThat(responseReceived).isEqualTo(responseExpected);
  }

  @Test
  void testGetDigitalMediaByIJsonResponse() {
    // Given
    String path = SANDBOX_URI + "/json/" + ID;
    var dataNode = givenDigitalMediaJsonApiData(ID);
    var responseExpected = new JsonApiWrapper(dataNode, new JsonApiLinks(path));
    given(repository.getLatestDigitalMediaObjectByIdJsonResponse(ID)).willReturn(dataNode);

    // When
    var responseReceived = service.getDigitalMediaByIdJsonResponse(ID, path);

    // Then
    assertThat(responseReceived).isEqualTo(responseExpected);
  }

  @Test
  void testGetDigitalMediaByIdJsonResponse() {
    // Given
    List<String> mediaIds = List.of("1", "2", "3");
    List<DigitalMediaObject> mediaObjects = new ArrayList<>();
    List<DigitalMediaObjectFull> responseExpected = new ArrayList<>();

    for (String id : mediaIds) {
      var mediaObject = givenDigitalMediaObject(id);
      mediaObjects.add(mediaObject);
      var annotation = givenAnnotationResponse();
      responseExpected.add(new DigitalMediaObjectFull(mediaObject, List.of(annotation)));
      given(annotationService.getAnnotationForTarget(String.valueOf(id))).willReturn(
          List.of(annotation));
    }
    given(repository.getDigitalMediaForSpecimen(ID)).willReturn(mediaObjects);

    // When
    var responseReceived = service.getDigitalMediaObjectFull(ID);

    // Then
    assertThat(responseReceived).isEqualTo(responseExpected);
  }

  @Test
  void testGetDigitalMediaVersions() throws NotFoundException {
    // Given
    List<Integer> responseExpected = List.of(1, 2);
    given(mongoRepository.getVersions(ID, "digital_media_provenance")).willReturn(responseExpected);

    // When
    var responseReceived = service.getDigitalMediaVersions(ID);

    // Then
    assertThat(responseReceived).isEqualTo(responseExpected);
  }

  @Test
  void testGetDigitalMediaVersion() throws NotFoundException, JsonProcessingException {
    // Given
    int version = 1;
    var responseExpected = givenMongoDBMediaResponse();
    given(mongoRepository.getVersion(ID, version, "digital_media_provenance")).willReturn(
        responseExpected);

    // When
    var responseReceived = service.getDigitalMediaVersion(ID, version);

    // Then
    assertThat(responseReceived).isEqualTo(givenDigitalMediaObject(ID));
  }

  private JsonNode givenMongoDBMediaResponse() throws JsonProcessingException {
    return MAPPER.readValue(
        """
            {
              "_id": "20.5000.1025/ABC-123-XYZ/1",
              "id": "20.5000.1025/ABC-123-XYZ",
              "version": 1,
              "type": "2DImageObject",
              "digital_specimen_id": "20.5000.1025/460-A7R-QMJ",
              "media_url": "https://dissco.com",
              "format": "image/jpeg",
              "source_system_id": "20.5000.1025/GW0-TYL-YRU",
              "created": {
                "$date": "2022-11-01T09:59:24.00Z"
              },
              "last_checked": {
                "$date": "2022-11-01T09:59:24.00Z"
              },
              "deleted": null,
              "data": {
                "dcterms:title": "19942272",
                "dcterms:publisher": "Royal Botanic Garden Edinburgh"
              },
              "original_data": {
                "dcterms:title": "19942272",
                "dcterms:type": "StillImage"
              }
            }
            """, JsonNode.class
    );
  }

  @Test
  void testGetDigitalMediaForSpecimen() {
    // Given
    var responseExpected = List.of(givenDigitalMediaObject(ID));
    given(repository.getDigitalMediaForSpecimen(ID)).willReturn(responseExpected);

    // When
    var responseReceived = service.getDigitalMediaForSpecimen(ID);

    // Then
    assertThat(responseReceived).isEqualTo(responseExpected);
  }

  @Test
  void testGetAnnotationsOnMediaObject() {
    // Given
    var expectedAnnotation = List.of(givenAnnotationResponse());

    given(annotationService.getAnnotationForTarget(ID)).willReturn(expectedAnnotation);

    // When
    var responseReceived = service.getAnnotationsOnDigitalMediaObject(ID);

    // Then
    assertThat(responseReceived).isEqualTo(expectedAnnotation);
  }

  @Test
  void testGetDigitalMediaObjects() {
    // Given
    int pageNumber = 1;
    int pageSize = 10;
    var responseExpected = Collections.nCopies(pageSize, givenDigitalMediaObject(ID));
    given(repository.getDigitalMediaObject(pageNumber, pageSize)).willReturn(responseExpected);

    // When
    var responseReceived = service.getDigitalMediaObjects(pageNumber, pageSize);

    // Then
    assertThat(responseReceived).isEqualTo(responseExpected);
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2})
  void testGetDigitalMediaObjectsJsonResponse(int pageNumber) {
    // Given
    int pageSize = 10;
    String path = SANDBOX_URI + "json";

    var dataNodePlusOne = Collections.nCopies(pageSize + 1, givenDigitalMediaJsonApiData(ID));
    var linksNode = givenJsonApiLinksFull(path, pageNumber, pageSize, true);

    given(repository.getDigitalMediaObjectJsonResponse(pageNumber, pageSize + 1)).willReturn(
        dataNodePlusOne);

    var dataNode = dataNodePlusOne.subList(0, pageSize);
    var responseExpected = new JsonApiListResponseWrapper(dataNode, linksNode);

    // When
    var responseReceived = service.getDigitalMediaObjectsJsonResponse(pageNumber, pageSize, path);

    // Then
    assertThat(responseReceived).isEqualTo(responseExpected);
  }

  @Test
  void testGetDigitalMediaObjectsJsonResponseLastPage() {
    // Given
    int pageSize = 10;
    int pageNumber = 1;
    String path = SANDBOX_URI + "json";

    var dataNode = Collections.nCopies(pageSize, givenDigitalMediaJsonApiData(ID));
    var linksNode = givenJsonApiLinksFull(path, pageNumber, pageSize, false);

    given(repository.getDigitalMediaObjectJsonResponse(pageNumber, pageSize + 1)).willReturn(
        dataNode);

    var responseExpected = new JsonApiListResponseWrapper(dataNode, linksNode);

    // When
    var responseReceived = service.getDigitalMediaObjectsJsonResponse(pageNumber, pageSize, path);

    // Then
    assertThat(responseReceived).isEqualTo(responseExpected);
  }

  @Test
  void testGetDigitalMediaIdsForSpecimen() {
    // Given
    List<String> responseExpected = List.of("1, 2");
    given(repository.getDigitalMediaIdsForSpecimen(ID)).willReturn(responseExpected);

    // When
    var responseReceived = service.getDigitalMediaIdsForSpecimen(ID);

    // Then
    assertThat(responseReceived).isEqualTo(responseExpected);
  }
}
