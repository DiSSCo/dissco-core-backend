package eu.dissco.backend.service;

import static eu.dissco.backend.TestUtils.CREATED;
import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.ORCID;
import static eu.dissco.backend.TestUtils.PREFIX;
import static eu.dissco.backend.TestUtils.SANDBOX_URI;
import static eu.dissco.backend.TestUtils.SUFFIX;
import static eu.dissco.backend.TestUtils.USER_ID_TOKEN;
import static eu.dissco.backend.TestUtils.givenUser;
import static eu.dissco.backend.controller.BaseController.DATE_STRING;
import static eu.dissco.backend.utils.AnnotationUtils.ANNOTATION_PATH;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationKafkaRequest;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationRequest;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationJsonResponse;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationJsonResponseNoPagination;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationResponse;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationResponseList;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationResponseSingleDataNode;
import static eu.dissco.backend.utils.AnnotationUtils.givenCreator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import static org.mockito.Mockito.mockStatic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import eu.dissco.backend.client.AnnotationClient;
import eu.dissco.backend.domain.User;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.domain.jsonapi.JsonApiLinks;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiMeta;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import eu.dissco.backend.exceptions.ForbiddenException;
import eu.dissco.backend.exceptions.NoAnnotationFoundException;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.repository.AnnotationRepository;
import eu.dissco.backend.repository.ElasticSearchRepository;
import eu.dissco.backend.repository.MongoRepository;
import eu.dissco.backend.utils.AnnotationUtils;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnnotationServiceTest {

  public DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_STRING)
      .withZone(ZoneOffset.UTC);
  @Mock
  private AnnotationRepository repository;
  @Mock
  private AnnotationClient annotationClient;
  @Mock
  private ElasticSearchRepository elasticRepository;
  @Mock
  private MongoRepository mongoRepository;
  @Mock
  private UserService userService;
  private AnnotationService service;


  @BeforeEach
  void setup() {
    service = new AnnotationService(repository, annotationClient, elasticRepository,
        mongoRepository, userService, MAPPER, formatter);
  }

  @Test
  void testGetAnnotationsForUser() throws Exception {
    // Given
    String userId = USER_ID_TOKEN;
    String annotationId = "123";
    int pageNumber = 1;
    int pageSize = 15;
    var totalCount = 30L;
    String path = SANDBOX_URI + "api/v1/annotations/creator/json";
    var tmp = givenAnnotationJsonResponse(path, pageNumber, pageSize,
        userId, annotationId, true);
    var expected = new JsonApiListResponseWrapper(tmp.getData(), tmp.getLinks(),
        new JsonApiMeta(totalCount));

    given(elasticRepository.getAnnotationsForCreator(userId, pageNumber, pageSize))
        .willReturn(Pair.of(totalCount, givenAnnotationResponseList(annotationId, pageSize + 1)));

    // When
    var receivedResponse = service.getAnnotationsForUser(userId, pageNumber, pageSize, path);

    // Then
    assertThat(receivedResponse).isEqualTo(expected);
  }

  @Test
  void testGetAnnotationsForUserLastPage() throws Exception {
    // Given
    String userId = USER_ID_TOKEN;
    String annotationId = "123";
    int pageNumber = 2;
    int pageSize = 15;
    var totalCount = 30L;
    String path = SANDBOX_URI + "api/v1/annotations/creator/json";
    var tmp = givenAnnotationJsonResponse(path, pageNumber, pageSize,
        userId, annotationId, false);
    var expected = new JsonApiListResponseWrapper(tmp.getData(), tmp.getLinks(),
        new JsonApiMeta(totalCount));

    given(elasticRepository.getAnnotationsForCreator(userId, pageNumber, pageSize))
        .willReturn(Pair.of(totalCount, givenAnnotationResponseList(annotationId, pageSize)));

    // When
    var receivedResponse = service.getAnnotationsForUser(userId, pageNumber, pageSize, path);

    // Then
    assertThat(receivedResponse).isEqualTo(expected);
  }

  @Test
  void testGetAnnotationForTargetObject() {
    // Given
    var expected = List.of(givenAnnotationRequest());
    given(repository.getForTarget("https://hdl.handle.net/" + ID)).willReturn(expected);

    // When
    var result = service.getAnnotationForTargetObject(ID);

    // Then
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void testGetAnnotationForTarget() {
    var repositoryResponse = givenAnnotationResponseList(ID, 1);
    var expected = givenAnnotationJsonResponseNoPagination(ANNOTATION_PATH, List.of(ID));
    given(repository.getForTarget("https://hdl.handle.net/" + ID)).willReturn(repositoryResponse);

    // When
    var result = service.getAnnotationForTarget(ID, ANNOTATION_PATH);

    assertThat(result).isEqualTo(expected);
  }

  @Test
  void testGetAnnotation() {
    // Given
    var expected = givenAnnotationResponseSingleDataNode(ANNOTATION_PATH);
    given(repository.getAnnotation(ID)).willReturn(givenAnnotationResponse(ID));

    // When
    var result = service.getAnnotation(ID, ANNOTATION_PATH);

    // Then
    assertThat(expected).isEqualTo(result);
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2})
  void testGetAnnotations(int pageNumber) {
    int pageSize = 15;
    String userId = USER_ID_TOKEN;
    String annotationId = "123";
    String path = SANDBOX_URI + "api/v1/annotations/all/json";
    var expected = givenAnnotationJsonResponse(path, pageNumber, pageSize,
        userId, annotationId, true);
    given(repository.getAnnotations(pageNumber, pageSize)).willReturn(
        givenAnnotationResponseList(annotationId, pageSize + 1));

    // When
    var receivedResponse = service.getAnnotations(pageNumber, pageSize, path);

    // Then
    assertThat(receivedResponse).isEqualTo(expected);
  }

  @Test
  void testGetAnnotationsJsonResponseLastPage() {
    int pageNumber = 1;
    int pageSize = 15;
    String userId = USER_ID_TOKEN;
    String annotationId = "123";
    String path = SANDBOX_URI + "api/v1/annotations/all/json";
    var expected = givenAnnotationJsonResponse(path, pageNumber, pageSize,
        userId, annotationId, false);
    given(repository.getAnnotations(pageNumber, pageSize)).willReturn(
        givenAnnotationResponseList(annotationId, pageSize));

    // When
    var receivedResponse = service.getAnnotations(pageNumber, pageSize, path);

    // Then
    assertThat(receivedResponse).isEqualTo(expected);
  }

  @Test
  void testGetLatestAnnotations() throws IOException {
    int pageSize = 15;
    int pageNumber = 1;
    String path = SANDBOX_URI + "api/v1/annotations/latest/json";
    var expected = givenAnnotationJsonResponse(path, pageNumber, pageSize,
        USER_ID_TOKEN, ID, true);
    var elasticResponse = Collections.nCopies(pageSize + 1, givenAnnotationResponse(ID));
    given(elasticRepository.getLatestAnnotations(pageNumber, pageSize)).willReturn(elasticResponse);

    // When
    var receivedResponse = service.getLatestAnnotations(pageNumber, pageSize, path);

    // Then
    assertThat(receivedResponse).isEqualTo(expected);
  }

  @Test
  void testGetLatestAnnotationsLastPage() throws IOException {
    int pageSize = 15;
    int pageNumber = 1;
    String path = SANDBOX_URI + "api/v1/annotations/latest/json";
    var expected = givenAnnotationJsonResponse(path, pageNumber, pageSize,
        USER_ID_TOKEN, ID, false);
    var elasticResponse = Collections.nCopies(pageSize, givenAnnotationResponse(ID));
    given(elasticRepository.getLatestAnnotations(pageNumber, pageSize)).willReturn(elasticResponse);

    // When
    var receivedResponse = service.getLatestAnnotations(pageNumber, pageSize, path);

    // Then
    assertThat(receivedResponse).isEqualTo(expected);
  }

  @Test
  void testPersistAnnotation() throws Exception {
    // Given
    var annotationRequest = givenAnnotationRequest();
    var annotationToKafkaRequest = givenAnnotationKafkaRequest(false);
    var kafkaResponse = MAPPER.valueToTree(givenAnnotationResponse().withOaCreator(givenCreator(ORCID)));

    var expected = givenAnnotationResponseSingleDataNode(ANNOTATION_PATH, ORCID);

    try (var mockedStatic = mockStatic(Instant.class)) {
      mockTime(mockedStatic);

      given(annotationClient.postAnnotation(annotationToKafkaRequest)).willReturn(kafkaResponse);
      given(userService.getUser(USER_ID_TOKEN)).willReturn(givenUser());

      //When
      var responseReceived = service.persistAnnotation(annotationRequest, USER_ID_TOKEN,
          ANNOTATION_PATH);

      // Then
      assertThat(responseReceived).isEqualTo(expected);
    }
  }

  @Test
  void testPersistAnnotationIsNull() throws Exception {
    // Given
    var annotationRequest = givenAnnotationRequest();
    var annotationToKafka = givenAnnotationKafkaRequest(false);
    given(annotationClient.postAnnotation(annotationToKafka))
        .willReturn(null);
    given(userService.getUser(USER_ID_TOKEN)).willReturn(givenUser());

    try (var mockedStatic = mockStatic(Instant.class)) {
      mockTime(mockedStatic);

      // When
      var result = service.persistAnnotation(annotationRequest, USER_ID_TOKEN, ANNOTATION_PATH);

      // Then
      assertThat(result).isNull();
    }
  }

  @Test
  void testUserHasNoOrcid() {
    // Given
    given(userService.getUser(USER_ID_TOKEN)).willReturn(new User(null, null, null, null, null));

    // Then
    assertThrowsExactly(ForbiddenException.class,
        () -> service.persistAnnotation(givenAnnotationRequest(), USER_ID_TOKEN, ANNOTATION_PATH));
  }

  @Test
  void testUpdateAnnotation() throws Exception {
    // Given
    var expected = givenAnnotationResponseSingleDataNode(ANNOTATION_PATH, ORCID);
    given(repository.getAnnotationForUser(ID, USER_ID_TOKEN)).willReturn(1);

    var annotationRequest = givenAnnotationRequest().withOdsId(ID);
    var annotationToKafkaRequest = givenAnnotationKafkaRequest(true).withDcTermsCreated(null)
        .withOdsId(ID);
    var kafkaResponse = MAPPER.valueToTree(givenAnnotationResponse().withOaCreator(givenCreator(ORCID)));

    try (var mockedStatic = mockStatic(Instant.class)) {
      mockTime(mockedStatic);

      given(annotationClient.updateAnnotation(any(), any(), eq(annotationToKafkaRequest)))
          .willReturn(kafkaResponse);
      given(userService.getUser(USER_ID_TOKEN)).willReturn(givenUser());

      // When
      var result = service.updateAnnotation(ID, annotationRequest, USER_ID_TOKEN, ANNOTATION_PATH, PREFIX, SUFFIX);

      // Then
      assertThat(result).isEqualTo(expected);
    }
  }

  @Test
  void testUpdateAnnotationDoesNotExist() {
    // Given
    given(repository.getAnnotationForUser(ID, USER_ID_TOKEN)).willReturn(0);

    // Then
    assertThrowsExactly(NoAnnotationFoundException.class,
        () -> service.updateAnnotation(ID, givenAnnotationRequest(), USER_ID_TOKEN,
            ANNOTATION_PATH, PREFIX, SUFFIX));
  }

  @Test
  void testGetAnnotationsByVersion() throws Exception {
    // Given
    int version = 1;
    var annotationNode = MAPPER.createObjectNode();
    annotationNode.set("annotation",
        MAPPER.valueToTree(givenAnnotationResponse(ID)));
    given(mongoRepository.getByVersion(ID, version, "annotation_provenance")).willReturn(
        annotationNode);
    var expected = new JsonApiWrapper(
        new JsonApiData(ID, "Annotation", annotationNode.get("annotation")),
        new JsonApiLinks(ANNOTATION_PATH));

    // When
    var result = service.getAnnotationByVersion(ID, version, ANNOTATION_PATH);

    // Then
    assertThat(expected).isEqualTo(result);
  }

  @Test
  void testGetDigitalMediaVersions() throws NotFoundException {
    // Given
    List<Integer> versionsList = List.of(1, 2);
    var versionsNode = MAPPER.createObjectNode();
    var arrayNode = versionsNode.putArray("versions");
    arrayNode.add(1).add(2);
    var dataNode = new JsonApiData(ID, "annotationVersions", versionsNode);
    var responseExpected = new JsonApiWrapper(dataNode, new JsonApiLinks(ANNOTATION_PATH));

    given(mongoRepository.getVersions(ID, "annotation_provenance")).willReturn(versionsList);
    try (var mockedStatic = mockStatic(ServiceUtils.class)) {
      mockedStatic.when(() -> ServiceUtils.createVersionNode(versionsList, MAPPER))
          .thenReturn(versionsNode);
      // When
      var responseReceived = service.getAnnotationVersions(ID, ANNOTATION_PATH);

      // Then
      assertThat(responseReceived).isEqualTo(responseExpected);
    }
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

  @Test
  void testDeleteAnnotation() throws Exception {
    // Given
    given(repository.getAnnotationForUser(ID, USER_ID_TOKEN)).willReturn(1);

    // When
    var result = service.deleteAnnotation(PREFIX, SUFFIX, USER_ID_TOKEN);

    // Then
    assertThat(result).isTrue();
  }

  @Test
  void testDeleteAnnotationDoesNotExist() throws Exception {
    // Given
    given(repository.getAnnotationForUser(ID, USER_ID_TOKEN)).willReturn(0);

    // Then
    assertThrowsExactly(NoAnnotationFoundException.class,
        () -> service.deleteAnnotation(PREFIX, SUFFIX, USER_ID_TOKEN));
  }


  private void mockTime(MockedStatic<Instant> mockedStatic) {
    Clock clock = Clock.fixed(CREATED, ZoneOffset.UTC);
    Instant instant = Instant.now(clock);
    mockedStatic.when(Instant::now).thenReturn(instant);
    mockedStatic.when(() -> Instant.ofEpochSecond(CREATED.getLong(ChronoField.INSTANT_SECONDS)))
        .thenReturn(instant);
    mockedStatic.when(() -> Instant.from(any())).thenReturn(instant);
  }

}
