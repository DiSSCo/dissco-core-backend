package eu.dissco.backend.service;

import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.givenDigitalSpecimen;
import static eu.dissco.backend.TestUtils.givenJsonApiLinksFull;
import static eu.dissco.backend.utils.AnnotationUtils.ANNOTATION_PATH;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationResponse;
import static eu.dissco.backend.utils.DigitalMediaObjectUtils.DIGITAL_MEDIA_PATH;
import static eu.dissco.backend.utils.DigitalMediaObjectUtils.givenDigitalMediaJsonApiData;
import static eu.dissco.backend.utils.DigitalMediaObjectUtils.givenDigitalMediaJsonResponse;
import static eu.dissco.backend.utils.DigitalMediaObjectUtils.givenDigitalMediaObject;
import static eu.dissco.backend.utils.MachineAnnotationServiceUtils.getFlattenedDigitalMedia;
import static eu.dissco.backend.utils.MachineAnnotationServiceUtils.getMasResponse;
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
import eu.dissco.backend.repository.SpecimenRepository;
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
  private MachineAnnotationServiceService masService;
  @Mock
  private MongoRepository mongoRepository;
  @Mock
  private SpecimenRepository specimenRepository;

  private DigitalMediaObjectService service;

  @BeforeEach
  void setup() {
    service = new DigitalMediaObjectService(repository, annotationService, specimenRepository,
        masService, mongoRepository, MAPPER);
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2})
  void testGetDigitalMediaObjects(int pageNumber) {
    // Given
    int pageSize = 10;

    var repositoryResponse = Collections.nCopies(pageSize + 1, givenDigitalMediaObject(ID));
    var dataNodePlusOne = Collections.nCopies(pageSize + 1, givenDigitalMediaJsonApiData(ID));
    var linksNode = givenJsonApiLinksFull(DIGITAL_MEDIA_PATH, pageNumber, pageSize, true);
    var dataNode = dataNodePlusOne.subList(0, pageSize);
    var responseExpected = new JsonApiListResponseWrapper(dataNode, linksNode);
    given(repository.getDigitalMediaObjects(pageNumber, pageSize)).willReturn(repositoryResponse);

    // When
    var responseReceived = service.getDigitalMediaObjects(pageNumber, pageSize, DIGITAL_MEDIA_PATH);

    // Then
    assertThat(responseReceived).isEqualTo(responseExpected);
  }

  @Test
  void testGetDigitalMediaObjectsLastPage() {
    // Given
    int pageSize = 10;
    int pageNumber = 1;

    var repositoryResponse = Collections.nCopies(pageSize, givenDigitalMediaObject(ID));
    var dataNode = Collections.nCopies(pageSize, givenDigitalMediaJsonApiData(ID));
    var linksNode = givenJsonApiLinksFull(DIGITAL_MEDIA_PATH, pageNumber, pageSize, false);

    given(repository.getDigitalMediaObjects(pageNumber, pageSize)).willReturn(repositoryResponse);

    var responseExpected = new JsonApiListResponseWrapper(dataNode, linksNode);

    // When
    var responseReceived = service.getDigitalMediaObjects(pageNumber, pageSize, DIGITAL_MEDIA_PATH);

    // Then
    assertThat(responseReceived).isEqualTo(responseExpected);
  }

  @Test
  void testGetDigitalMediaById() {
    // Given
    var mediaObject = givenDigitalMediaObject(ID);
    given(repository.getLatestDigitalMediaObjectById(ID)).willReturn(mediaObject);
    var expected = new JsonApiWrapper(new JsonApiData(mediaObject.id(), mediaObject.type(),
        MAPPER.valueToTree(mediaObject)), new JsonApiLinks(DIGITAL_MEDIA_PATH));

    // When
    var responseReceived = service.getDigitalMediaById(ID, DIGITAL_MEDIA_PATH);

    // Then
    assertThat(responseReceived).isEqualTo(expected);
  }

  @Test
  void testGetAnnotationsOnDigitalMedia() {
    // Given
    var expected = givenDigitalMediaJsonResponse(ANNOTATION_PATH, 1, 1, List.of(ID));
    given(annotationService.getAnnotationForTarget(ID, ANNOTATION_PATH)).willReturn(expected);

    //
    var result = service.getAnnotationsOnDigitalMedia(ID, ANNOTATION_PATH);

    // Then
    assertThat(result).isEqualTo(expected);
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
      mockedStatic.when(() -> ServiceUtils.createVersionNode(versionsList, MAPPER))
          .thenReturn(versionsNode);

      // When
      var responseReceived = service.getDigitalMediaVersions(ID, DIGITAL_MEDIA_PATH);

      // Then
      assertThat(responseReceived).isEqualTo(responseExpected);
    }
  }

  @Test
  void testGetDigitalMediaByVersion() throws NotFoundException, JsonProcessingException {
    // Given
    int version = 1;
    var mongoResponse = givenMongoDBMediaResponse();
    given(mongoRepository.getByVersion(ID, version, "digital_media_provenance")).willReturn(
        mongoResponse);

    var type = mongoResponse.get("digitalMediaObject").get("dcterms:type").asText();

    var expectedResponse = new JsonApiWrapper(new JsonApiData(ID, type, mongoResponse),
        new JsonApiLinks(DIGITAL_MEDIA_PATH));

    // When
    var responseReceived = service.getDigitalMediaObjectByVersion(ID, version, DIGITAL_MEDIA_PATH);

    // Then
    assertThat(responseReceived).isEqualTo(expectedResponse);
  }

  @Test
  void testGetDigitalMediaObjectFull() {
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
    given(repository.getDigitalMediaForSpecimen(ID)).willReturn(mediaObjects);

    // When
    var responseReceived = service.getDigitalMediaObjectFull(ID);

    // Then
    assertThat(responseReceived).isEqualTo(responseExpected);
  }

  @Test
  void testGetDigitalMediaForSpecimen() {
    // Given
    var mediaObject = givenDigitalMediaObject(ID);
    var responseExpected = List.of(new JsonApiData(
        mediaObject.id(),
        mediaObject.type(),
        MAPPER.valueToTree(mediaObject)));
    given(repository.getDigitalMediaForSpecimen(ID)).willReturn(List.of(mediaObject));

    // When
    var responseReceived = service.getDigitalMediaForSpecimen(ID);

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

  @Test
  void testGetMas() throws JsonProcessingException {
    // Given
    var digitalMedia = givenDigitalMediaObject(ID);
    var specimenId = "20.5000.1025/460-A7R-QMJ";
    var digitalSpecimen = givenDigitalSpecimen(specimenId);
    var response = getMasResponse(DIGITAL_MEDIA_PATH);
    given(repository.getLatestDigitalMediaObjectById(ID)).willReturn(digitalMedia);
    given(specimenRepository.getLatestSpecimenById(specimenId)).willReturn(digitalSpecimen);
    given(masService.getMassForObject(getFlattenedDigitalMedia(), DIGITAL_MEDIA_PATH)).willReturn(
        response);

    // When
    var result = service.getMas(ID, DIGITAL_MEDIA_PATH);

    // Then
    assertThat(result).isEqualTo(response);
  }

  @Test
  void testScheduleMas() throws JsonProcessingException {
    // Given
    var digitalMedia = givenDigitalMediaObject(ID);
    var specimenId = "20.5000.1025/460-A7R-QMJ";
    var digitalSpecimen = givenDigitalSpecimen(specimenId);
    var response = getMasResponse(DIGITAL_MEDIA_PATH);
    given(repository.getLatestDigitalMediaObjectById(ID)).willReturn(digitalMedia);
    given(specimenRepository.getLatestSpecimenById(specimenId)).willReturn(digitalSpecimen);
    given(masService.scheduleMass(getFlattenedDigitalMedia(), List.of(ID), DIGITAL_MEDIA_PATH,
        digitalMedia)).willReturn(
        response);

    // When
    var result = service.scheduleMass(ID, List.of(ID), DIGITAL_MEDIA_PATH);

    // Then
    assertThat(result).isEqualTo(response);
  }

  private JsonNode givenMongoDBMediaResponse() throws JsonProcessingException {
    return MAPPER.readValue(
        """
            {
              "id": "20.5000.1025/ABC-123-XYZ",
              "version": 1,
              "created": 1667296764,
              "digitalMediaObject": {
                "dcterms:type": "2DImageObject",
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
}
