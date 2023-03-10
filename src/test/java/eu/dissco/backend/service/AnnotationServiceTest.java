package eu.dissco.backend.service;

import static eu.dissco.backend.TestUtils.CREATED;
import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.ID_ALT;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.SANDBOX_URI;
import static eu.dissco.backend.TestUtils.USER_ID_TOKEN;
import static eu.dissco.backend.utils.AnnotationUtils.ANNOTATION_PATH;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationBody;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationJsonApiDataList;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationJsonResponse;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationRequest;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationResponse;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationResponseSingleDataNode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.dissco.backend.client.AnnotationClient;
import eu.dissco.backend.domain.AnnotationEvent;
import eu.dissco.backend.domain.AnnotationRequest;
import eu.dissco.backend.domain.AnnotationResponse;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.domain.jsonapi.JsonApiLinks;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import eu.dissco.backend.exceptions.NoAnnotationFoundException;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.repository.AnnotationRepository;
import eu.dissco.backend.repository.ElasticSearchRepository;
import eu.dissco.backend.repository.MongoRepository;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnnotationServiceTest {

  @Mock
  private AnnotationRepository repository;
  @Mock
  private AnnotationClient annotationClient;
  @Mock
  private ElasticSearchRepository elasticRepository;
  @Mock
  private MongoRepository mongoRepository;
  private AnnotationService service;
  private ObjectMapper mapper;

  @BeforeEach
  void setup() {
    mapper = new ObjectMapper().findAndRegisterModules();
    mapper.setDefaultPropertyInclusion(Include.ALWAYS);
    service = new AnnotationService(repository, annotationClient, elasticRepository,
        mongoRepository);
  }

  @Test
  void testGetAnnotationsForUserJsonResponse() {
    // Given
    String userId = USER_ID_TOKEN;
    String annotationId = "123";
    int pageNumber = 1;
    int pageSize = 15;
    String path = SANDBOX_URI + "api/v1/annotations/creator/json";
    var expectedResponse = givenAnnotationJsonResponse(path, pageNumber, pageSize,
        userId, annotationId, true);
    given(
        repository.getAnnotationsForUser(userId, pageNumber, pageSize + 1)).willReturn(
        givenAnnotationJsonApiDataList(pageSize + 1, userId, annotationId));

    // When
    var receivedResponse = service.getAnnotationsForUser(userId, pageNumber, pageSize,
        path);

    // Then
    assertThat(receivedResponse).isEqualTo(expectedResponse);
  }

  @Test
  void testGetAnnotationsForUserJsonResponseLastPage() {
    // Given
    String userId = USER_ID_TOKEN;
    String annotationId = "123";
    int pageNumber = 1;
    int pageSize = 15;
    String path = SANDBOX_URI + "api/v1/annotations/creator/json";
    var expectedResponse = givenAnnotationJsonResponse(path, pageNumber, pageSize,
        userId, annotationId, false);
    given(
        repository.getAnnotationsForUser(userId, pageNumber, pageSize + 1)).willReturn(
        givenAnnotationJsonApiDataList(pageSize, userId, annotationId));

    // When
    var receivedResponse = service.getAnnotationsForUser(userId, pageNumber, pageSize,
        path);

    // Then
    assertThat(receivedResponse).isEqualTo(expectedResponse);
  }

  @Test
  void testGetAnnotation() {
    // Given
    var expected = givenAnnotationResponseSingleDataNode(ANNOTATION_PATH);
    given(repository.getAnnotation(ID)).willReturn(expected.data());

    // When
    var result = service.getAnnotation(ID, ANNOTATION_PATH);

    // Then
    assertThat(expected).isEqualTo(result);
  }



  @ParameterizedTest
  @ValueSource(ints = {1, 2})
  void testGetAnnotationsJsonResponse(int pageNumber) {
    int pageSize = 15;
    String userId = USER_ID_TOKEN;
    String annotationId = "123";
    String path = SANDBOX_URI + "api/v1/annotations/all/json";
    var expectedResponse = givenAnnotationJsonResponse(path, pageNumber, pageSize,
        userId, annotationId, true);
    given(repository.getAnnotations(pageNumber, pageSize + 1)).willReturn(
        givenAnnotationJsonApiDataList(pageSize + 1, userId, annotationId));

    // When
    var receivedResponse = service.getAnnotations(pageNumber, pageSize, path);

    // Then
    assertThat(receivedResponse).isEqualTo(expectedResponse);
  }

  @Test
  void testGetAnnotationsJsonResponseLastPage() {
    int pageNumber = 1;
    int pageSize = 15;
    String userId = USER_ID_TOKEN;
    String annotationId = "123";
    String path = SANDBOX_URI + "api/v1/annotations/all/json";
    var expectedResponse = givenAnnotationJsonResponse(path, pageNumber, pageSize,
        userId, annotationId, false);
    given(repository.getAnnotations(pageNumber, pageSize + 1)).willReturn(
        givenAnnotationJsonApiDataList(pageSize, userId, annotationId));

    // When
    var receivedResponse = service.getAnnotations(pageNumber, pageSize, path);

    // Then
    assertThat(receivedResponse).isEqualTo(expectedResponse);
  }

  @Test
  void testGetLatestAnnotationsJsonResponse() throws IOException {
    int pageSize = 15;
    int pageNumber = 1;
    String path = SANDBOX_URI + "api/v1/annotations/latest/json";
    var expectedResponse = givenAnnotationJsonResponse(path, pageNumber, pageSize,
        USER_ID_TOKEN, "123", true);
    given(elasticRepository.getLatestAnnotations(pageNumber, pageSize + 1)).willReturn(
        givenAnnotationJsonApiDataList(pageSize + 1, USER_ID_TOKEN, "123"));

    // When
    var receivedResponse = service.getLatestAnnotations(pageNumber, pageSize, path);

    // Then
    assertThat(receivedResponse).isEqualTo(expectedResponse);
  }

  @Test
  void testGetLatestAnnotationsJsonResponseLastPage() throws IOException {
    int pageSize = 15;
    int pageNumber = 1;
    String path = SANDBOX_URI + "api/v1/annotations/latest/json";
    var expectedResponse = givenAnnotationJsonResponse(path, pageNumber, pageSize,
        USER_ID_TOKEN, "123", false);
    given(elasticRepository.getLatestAnnotations(pageNumber, pageSize + 1)).willReturn(
        givenAnnotationJsonApiDataList(pageSize, USER_ID_TOKEN, "123"));

    // When
    var receivedResponse = service.getLatestAnnotations(pageNumber, pageSize, path);

    // Then
    assertThat(receivedResponse).isEqualTo(expectedResponse);
  }

  @Test
  void testPersistAnnotation() {
    // Given
    AnnotationRequest annotationRequest = givenAnnotationRequest();
    AnnotationResponse annotationResponse = givenAnnotationResponse(USER_ID_TOKEN, ID_ALT);
    JsonNode annotationNode = mapper.valueToTree(annotationResponse);
    ObjectNode clientResponse = mapper.createObjectNode();
    clientResponse.put("id", annotationResponse.id());
    clientResponse.put("version", annotationResponse.version());
    clientResponse.put("annotation", annotationNode);

    var dataNode = new JsonApiData(annotationResponse.id(), annotationResponse.type(),
        MAPPER.valueToTree(annotationNode));
    var expectedResponse = new JsonApiWrapper(dataNode, new JsonApiLinks(ANNOTATION_PATH));

    Clock clock = Clock.fixed(CREATED, ZoneOffset.UTC);
    Instant instant = Instant.now(clock);
    try (var mockedStatic = mockStatic(Instant.class)) {
      mockedStatic.when(Instant::now).thenReturn(instant);
      mockedStatic.when(() -> Instant.ofEpochSecond(CREATED.getLong(ChronoField.INSTANT_SECONDS)))
          .thenReturn(instant);
      mockedStatic.when(() -> Instant.from(any())).thenReturn(instant);

      given(annotationClient.postAnnotation(givenAnnotationEvent(annotationRequest, USER_ID_TOKEN)))
          .willReturn(clientResponse);

      //When
      var responseReceived = service.persistAnnotation(annotationRequest, USER_ID_TOKEN,
          ANNOTATION_PATH);

      // Then
      assertThat(responseReceived).isEqualTo(expectedResponse);
    }
  }

  @Test
  void testPersistAnnotationIsNull() {
    // Given
    AnnotationRequest annotationRequest = givenAnnotationRequest();
    given(annotationClient.postAnnotation(givenAnnotationEvent(annotationRequest, USER_ID_TOKEN)))
        .willReturn(null);
    Clock clock = Clock.fixed(CREATED, ZoneOffset.UTC);
    Instant instant = Instant.now(clock);
    try (var mockedStatic = mockStatic(Instant.class)) {
      mockedStatic.when(Instant::now).thenReturn(instant);
      mockedStatic.when(() -> Instant.ofEpochSecond(CREATED.getLong(ChronoField.INSTANT_SECONDS)))
          .thenReturn(instant);
      mockedStatic.when(() -> Instant.from(any())).thenReturn(instant);

      // When
      var result = service.persistAnnotation(annotationRequest, USER_ID_TOKEN, ANNOTATION_PATH);

      // Then
      assertThat(result).isNull();
    }
  }

  @Test
  void testUpdateAnnotation() throws Exception{
    // Given
    var expected = givenAnnotationResponseSingleDataNode(ANNOTATION_PATH);
    given(repository.getAnnotationForUser(ID, USER_ID_TOKEN)).willReturn(1);

    AnnotationRequest annotationRequest = givenAnnotationRequest();
    AnnotationResponse annotationResponse = givenAnnotationResponse(USER_ID_TOKEN, ID);
    JsonNode annotationNode = mapper.valueToTree(annotationResponse);
    ObjectNode clientResponse = mapper.createObjectNode();
    clientResponse.put("id", annotationResponse.id());
    clientResponse.put("version", annotationResponse.version());
    clientResponse.set("annotation", annotationNode);

    Clock clock = Clock.fixed(CREATED, ZoneOffset.UTC);
    Instant instant = Instant.now(clock);
    try (var mockedStatic = mockStatic(Instant.class)) {
      mockedStatic.when(Instant::now).thenReturn(instant);
      mockedStatic.when(() -> Instant.ofEpochSecond(CREATED.getLong(ChronoField.INSTANT_SECONDS)))
          .thenReturn(instant);
      mockedStatic.when(() -> Instant.from(any())).thenReturn(instant);

      given(annotationClient.postAnnotation(givenAnnotationEvent(annotationRequest, USER_ID_TOKEN)))
          .willReturn(clientResponse);

      // When
      var result = service.updateAnnotation(ID, annotationRequest, USER_ID_TOKEN, ANNOTATION_PATH);

      // Then
      assertThat(result).isEqualTo(expected);

    }
  }

  @Test
  void testGetAnnotationsByVersion() throws Exception{
    // Given
    int version = 1;
    var annotationNode = MAPPER.createObjectNode();
    annotationNode.set("annotation", MAPPER.valueToTree(givenAnnotationResponse(USER_ID_TOKEN, ID)));
    given(mongoRepository.getByVersion(ID, version, "annotation_provenance")).willReturn(annotationNode);
    var expected = new JsonApiWrapper(new JsonApiData(ID, "Annotation", annotationNode), new JsonApiLinks(ANNOTATION_PATH));

    // When
    var result = service.getAnnotationByVersion(ID, version, ANNOTATION_PATH);

    // Then
    assertThat(expected).isEqualTo(result);
  }

  @Test
  void testGetDigitalMediaVersions() throws NotFoundException {
    // Given
    List<Integer> responseExpected = List.of(1, 2);
    given(mongoRepository.getVersions(ID, "annotation_provenance")).willReturn(responseExpected);

    // When
    var responseReceived = service.getAnnotationVersions(ID);

    // Then
    assertThat(responseReceived).isEqualTo(responseExpected);
  }

  private JsonNode givenMongoDBAnnotationResponse() throws JsonProcessingException {
    return MAPPER.readValue(
        """
            {
                 "id": "20.5000.1025/ABC-123-XYZ",
                 "version": 1,
                 "created": 1667296764,
                 "annotation": {
                   "type": "Annotation",
                   "motivation": "motivation",
                   "target": {
                     "id": "20.5000.1025/TAR_GET_001",
                     "type": "digitalSpecimen"
                   },
                   "body": {
                     "source": "https://medialib.naturalis.nl/file/id/ZMA.UROCH.P.1555/format/large",
                     "values": [
                       {
                         "class": "leaf",
                         "score": 0.99
                       }
                     ]
                   },
                   "preferenceScore": 100,
                   "creator": "e2befba6-9324-4bb4-9f41-d7dfae4a44b0",
                   "created": 1667296764,
                   "generator": {
                     "id": "generatorId",
                     "name": "annotation processing service"
                   },
                   "generated": 1667296764
                 }
               }
                        """, JsonNode.class
    );
  }

  private AnnotationEvent givenAnnotationEvent(AnnotationRequest annotation, String userId) {
    return new AnnotationEvent(
        annotation.type(),
        annotation.motivation(),
        userId,
        CREATED,
        annotation.target(),
        annotation.body()
    );
  }

}
