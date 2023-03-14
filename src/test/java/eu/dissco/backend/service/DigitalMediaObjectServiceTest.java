package eu.dissco.backend.service;

import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.SANDBOX_URI;
import static eu.dissco.backend.TestUtils.givenDigitalMediaJsonApiData;
import static eu.dissco.backend.TestUtils.givenDigitalMediaObject;
import static eu.dissco.backend.TestUtils.givenJsonApiLinksFull;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationResponse;
import static eu.dissco.backend.utils.DigitalMediaObjectUtils.DIGITAL_MEDIA_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import eu.dissco.backend.domain.DigitalMediaObject;
import eu.dissco.backend.domain.DigitalMediaObjectFull;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.domain.jsonapi.JsonApiLinks;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
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
    String path = SANDBOX_URI + "/json/" + ID;
    var dataNode = givenDigitalMediaJsonApiData(ID);
    var responseExpected = new JsonApiWrapper(dataNode, new JsonApiLinks(path));
    given(repository.getLatestDigitalMediaObjectByIdJsonResponse(ID)).willReturn(dataNode);

    // When
    var responseReceived = service.getDigitalMediaById(ID, path);

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
      given(annotationService.getAnnotationForTargetObject(String.valueOf(id))).willReturn(
          List.of(annotation));
    }
    given(repository.getDigitalMediaForSpecimenObject(ID)).willReturn(mediaObjects);

    // When
    var responseReceived = service.getDigitalMediaObjectFull(ID);

    // Then
    assertThat(responseReceived).isEqualTo(responseExpected);
  }

  @Test
  void testGetDigitalMediaVersions() throws NotFoundException {
    // Given
    List<Integer> versionsList = List.of(1, 2);
    given(mongoRepository.getVersions(ID, "digital_media_provenance")).willReturn(versionsList);
    var versionsNode = MAPPER.createObjectNode();
    var arrayNode = versionsNode.putArray("versions");
    arrayNode.add(1).add(2);
    var dataNode = new JsonApiData(ID, "digitalMediaVersions", versionsNode);
    var responseExpected = new JsonApiWrapper(dataNode, new JsonApiLinks(DIGITAL_MEDIA_PATH));

    try (var mockedStatic = mockStatic(ServiceUtils.class)) {
      mockedStatic.when(() -> ServiceUtils.createVersionNode(versionsList))
          .thenReturn(versionsNode);
      // When
      var responseReceived = service.getDigitalMediaVersions(ID, DIGITAL_MEDIA_PATH);

      // Then
      assertThat(responseReceived).isEqualTo(responseExpected);
    }
  }

  @Test
  void testGetDigitalMediaVersion() throws NotFoundException, JsonProcessingException {
    // Given
    int version = 1;
    var mongoResponse = givenMongoDBMediaResponse();
    given(mongoRepository.getByVersion(ID, version, "digital_media_provenance")).willReturn(
        mongoResponse);

    var type = mongoResponse.get("digitalMediaObject").get("type").asText();

    var expectedResponse = new JsonApiWrapper(new JsonApiData(ID, type, mongoResponse),
        new JsonApiLinks(DIGITAL_MEDIA_PATH));

    // When
    var responseReceived = service.getDigitalMediaObjectByVersion(ID, version, DIGITAL_MEDIA_PATH);

    // Then
    assertThat(responseReceived).isEqualTo(expectedResponse);
  }

  private JsonNode givenMongoDBMediaResponse() throws JsonProcessingException {
    return MAPPER.readValue(
        """
            {
              "id": "20.5000.1025/ABC-123-XYZ",
              "version": 1,
              "created": 1667296764,
              "digitalMediaObject": {
                "type": "2DImageObject",
                "digitalSpecimenId": "20.5000.1025/460-A7R-QMJ",
                "mediaUrl": "https://dissco.com",
                "format": "image/jpeg",
                "sourceSystemId": "20.5000.1025/GW0-TYL-YRU",
                "data": {
                "dcterms:title": "19942272",
                "dcterms:publisher": "Royal Botanic Garden Edinburgh"
                },
                "originalData": {
                "dcterms:title": "19942272",
                "dcterms:type": "StillImage"
                }
              }
            }
                        """, JsonNode.class
    );
  }

  @Test
  void testGetDigitalMediaForSpecimen() {
    // Given
    var mediaObject = givenDigitalMediaObject(ID);
    var responseExpected = List.of(new JsonApiData(
        mediaObject.id(),
        mediaObject.type(),
        MAPPER.valueToTree(mediaObject)));
    given(repository.getDigitalMediaForSpecimen(ID)).willReturn(responseExpected);

    // When
    var responseReceived = service.getDigitalMediaForSpecimen(ID);

    // Then
    assertThat(responseReceived).isEqualTo(responseExpected);
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2})
  void testGetDigitalMediaObjects(int pageNumber) {
    // Given
    int pageSize = 10;
    String path = SANDBOX_URI + "json";

    var dataNodePlusOne = Collections.nCopies(pageSize + 1, givenDigitalMediaJsonApiData(ID));
    var linksNode = givenJsonApiLinksFull(path, pageNumber, pageSize, true);

    given(repository.getDigitalMediaObjects(pageNumber, pageSize + 1)).willReturn(
        dataNodePlusOne);

    var dataNode = dataNodePlusOne.subList(0, pageSize);
    var responseExpected = new JsonApiListResponseWrapper(dataNode, linksNode);

    // When
    var responseReceived = service.getDigitalMediaObjects(pageNumber, pageSize, path);

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

    given(repository.getDigitalMediaObjects(pageNumber, pageSize + 1)).willReturn(
        dataNode);

    var responseExpected = new JsonApiListResponseWrapper(dataNode, linksNode);

    // When
    var responseReceived = service.getDigitalMediaObjects(pageNumber, pageSize, path);

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
