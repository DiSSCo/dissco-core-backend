package eu.dissco.backend.service;

import static eu.dissco.backend.TestUtils.CREATED;
import static eu.dissco.backend.TestUtils.DOI;
import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.ORCID;
import static eu.dissco.backend.TestUtils.PREFIX;
import static eu.dissco.backend.TestUtils.SANDBOX_URI;
import static eu.dissco.backend.TestUtils.SUFFIX;
import static eu.dissco.backend.TestUtils.givenUser;
import static eu.dissco.backend.controller.BaseController.DATE_STRING;
import static eu.dissco.backend.utils.AnnotationUtils.ANNOTATION_PATH;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationCountRequest;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationEventRequest;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationJsonResponse;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationJsonResponseNoPagination;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationRequest;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationResponse;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationResponseBatch;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationResponseList;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationResponseSingleDataNode;
import static eu.dissco.backend.utils.AnnotationUtils.givenBatchMetadata;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import eu.dissco.backend.client.AnnotationClient;
import eu.dissco.backend.domain.annotation.AnnotationTargetType;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.domain.jsonapi.JsonApiLinks;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiMeta;
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
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.common.errors.InvalidRequestException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
  private MasJobRecordService masJobRecordService;


  private MockedStatic<Instant> mockedInstant;
  private MockedStatic<Clock> mockedClock;
  private AnnotationService service;

  private static Stream<Arguments> badAnnotationCountRequest() throws JsonProcessingException {
    return Stream.of(
        Arguments.of(MAPPER.readTree("""
            {
              "data": {
                "type": "annotationRequests",
                "attributes": {
                  "batchMetadata": {
                    "searchParams": [
                      {
                        "inputField": "digitalSpecimenWrapper.occurrences[*].location.dwc:country",
                        "inputValue": "Canada"
                      }
                    ]
                  }
                }
              }
            }
            """)),
        Arguments.of(MAPPER.readTree("""
            {
              "data": {
                "type": "annotationRequests",
                "attributes": {
                  "annotationTargetType": "https://doi.org/21.T11148/894b1e6cad57e921764e"
                }
              }
            }
            """)),
        Arguments.of(MAPPER.readTree("""
            {
              "data": {
                "type": "annotationRequests"
              }
            }
            """)),
        Arguments.of(MAPPER.createObjectNode())
    );
  }

  @BeforeEach
  void setup() {
    service = new AnnotationService(repository, annotationClient, elasticRepository,
        mongoRepository, MAPPER, masJobRecordService);
    Clock clock = Clock.fixed(CREATED, ZoneOffset.UTC);
    Instant instant = Instant.now(clock);
    mockedInstant = mockStatic(Instant.class);
    mockedInstant.when(Instant::now).thenReturn(instant);
    mockedInstant.when(() -> Instant.from(any())).thenReturn(instant);
    mockedInstant.when(() -> Instant.parse(any())).thenReturn(instant);
    mockedClock = mockStatic(Clock.class);
    mockedClock.when(Clock::systemUTC).thenReturn(clock);
  }

  @AfterEach
  void destroy() {
    mockedInstant.close();
    mockedClock.close();
  }

  @Test
  void testGetAnnotationsForUser() throws Exception {
    // Given
    String annotationId = "123";
    int pageNumber = 1;
    int pageSize = 15;
    var totalCount = 30L;
    String path = SANDBOX_URI + "api/v1/annotationRequests/creator/json";
    var tmp = givenAnnotationJsonResponse(path, pageNumber, pageSize,
        ORCID, annotationId, true);
    var expected = new JsonApiListResponseWrapper(tmp.getData(), tmp.getLinks(),
        new JsonApiMeta(totalCount));
    given(elasticRepository.getAnnotationsForCreator(ORCID, pageNumber, pageSize))
        .willReturn(Pair.of(totalCount, givenAnnotationResponseList(annotationId, pageSize + 1)));

    // When
    var received = service.getAnnotationsForUser(ORCID, pageNumber, pageSize, path);

    // Then
    assertThat(received).isEqualTo(expected);
  }

  @Test
  void testGetAnnotationsForUserLastPage() throws Exception {
    // Given
    String annotationId = "123";
    int pageNumber = 2;
    int pageSize = 15;
    var totalCount = 30L;
    String path = SANDBOX_URI + "api/v1/annotationRequests/creator/json";
    var tmp = givenAnnotationJsonResponse(path, pageNumber, pageSize,
        ORCID, annotationId, false);
    var expected = new JsonApiListResponseWrapper(tmp.getData(), tmp.getLinks(),
        new JsonApiMeta(totalCount));
    given(elasticRepository.getAnnotationsForCreator(ORCID, pageNumber, pageSize))
        .willReturn(Pair.of(totalCount, givenAnnotationResponseList(annotationId, pageSize)));

    // When
    var received = service.getAnnotationsForUser(ORCID, pageNumber, pageSize, path);

    // Then
    assertThat(received).isEqualTo(expected);
  }

  @Test
  void testGetAnnotationForTargetObject() {
    // Given
    var expected = List.of(givenAnnotationResponse());
    given(repository.getForTarget(DOI + ID)).willReturn(expected);

    // When
    var result = service.getAnnotationForTargetObject(ID);

    // Then
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void testGetAnnotationForTarget() {
    var repositoryResponse = givenAnnotationResponseList(ID, 1);
    var expected = givenAnnotationJsonResponseNoPagination(ANNOTATION_PATH, List.of(ID));
    given(repository.getForTarget(DOI + ID)).willReturn(repositoryResponse);

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
    String annotationId = "123";
    String path = SANDBOX_URI + "api/v1/annotationRequests/all/json";
    var expected = givenAnnotationJsonResponse(path, pageNumber, pageSize,
        ORCID, annotationId, true);
    given(repository.getAnnotations(pageNumber, pageSize)).willReturn(
        givenAnnotationResponseList(annotationId, pageSize + 1));

    // When
    var received = service.getAnnotations(pageNumber, pageSize, path);

    // Then
    assertThat(received).isEqualTo(expected);
  }

  @Test
  void testGetAnnotationsJsonResponseLastPage() {
    int pageNumber = 1;
    int pageSize = 15;
    String annotationId = "123";
    String path = SANDBOX_URI + "api/v1/annotationRequests/all/json";
    var expected = givenAnnotationJsonResponse(path, pageNumber, pageSize,
        ORCID, annotationId, false);
    given(repository.getAnnotations(pageNumber, pageSize)).willReturn(
        givenAnnotationResponseList(annotationId, pageSize));

    // When
    var received = service.getAnnotations(pageNumber, pageSize, path);

    // Then
    assertThat(received).isEqualTo(expected);
  }

  @Test
  void testGetLatestAnnotations() throws IOException {
    int pageSize = 15;
    int pageNumber = 1;
    String path = SANDBOX_URI + "api/v1/annotationRequests/latest/json";
    var expected = givenAnnotationJsonResponse(path, pageNumber, pageSize,
        ORCID, ID, true);
    var elasticResponse = Collections.nCopies(pageSize + 1, givenAnnotationResponse(ID));
    given(elasticRepository.getLatestAnnotations(pageNumber, pageSize)).willReturn(elasticResponse);

    // When
    var received = service.getLatestAnnotations(pageNumber, pageSize, path);

    // Then
    assertThat(received).isEqualTo(expected);
  }

  @Test
  void testPersistAnnotationBatch() throws Exception {
    // Given
    var annotationRequest = givenAnnotationRequest();
    var processingResponse = MAPPER.valueToTree(givenAnnotationResponse());
    var expected = givenAnnotationResponseSingleDataNode(ANNOTATION_PATH, ORCID);
    given(annotationClient.postAnnotation(any())).willReturn(
        processingResponse);

    //When
    var responseReceived = service.persistAnnotation(annotationRequest, givenUser(),
        ANNOTATION_PATH);

    // Then
    assertThat(responseReceived).isEqualTo(expected);
  }

  @Test
  void testGetLatestAnnotationsLastPage() throws IOException {
    int pageSize = 15;
    int pageNumber = 1;
    String path = SANDBOX_URI + "api/v1/annotationRequests/latest/json";
    var expected = givenAnnotationJsonResponse(path, pageNumber, pageSize,
        ORCID, ID, false);
    var elasticResponse = Collections.nCopies(pageSize, givenAnnotationResponse(ID));
    given(elasticRepository.getLatestAnnotations(pageNumber, pageSize)).willReturn(elasticResponse);

    // When
    var received = service.getLatestAnnotations(pageNumber, pageSize, path);

    // Then
    assertThat(received).isEqualTo(expected);
  }

  @Test
  void testGetAnnotationBatchCount() throws Exception {
    // Given
    given(elasticRepository.getCountForBatchAnnotations(givenBatchMetadata(),
        AnnotationTargetType.DIGITAL_SPECIMEN))
        .willReturn(10L);
    MAPPER.configure(DeserializationFeature.USE_LONG_FOR_INTS, true);
    var expected = MAPPER.readTree("""
        {
          "data": {
            "type": "batchAnnotationCount",
            "attributes": {
              "objectAffected": 10,
              "batchMetadata": {
                "searchParams": [
                  {
                    "inputField": "ods:hasEvent.ods:Location.dwc:country.keyword",
                    "inputValue": "Netherlands"
                  }
                ],
                "placeInBatch":1
              }
            }
          }
        }
        """);

    // When
    var result = service.getCountForBatchAnnotations(givenAnnotationCountRequest());

    // Then
    assertThat(result).isEqualTo(expected);
    MAPPER.configure(DeserializationFeature.USE_LONG_FOR_INTS, false);
  }

  @ParameterizedTest
  @MethodSource("badAnnotationCountRequest")
  void testGetAnnotationBatchCountBadRequest(JsonNode badRequest) {
    // When / Then
    assertThrowsExactly(InvalidRequestException.class,
        () -> service.getCountForBatchAnnotations(badRequest));
  }

  @Test
  void testPersistAnnotationBatchBatch() throws Exception {
    // Given
    var event = givenAnnotationEventRequest();
    var processingResponse = MAPPER.valueToTree(givenAnnotationResponse().withOdsPlaceInBatch(1));
    var expected = givenAnnotationResponseBatch(ANNOTATION_PATH, ORCID);
    given(annotationClient.postAnnotationBatch(any())).willReturn(
        processingResponse);

    //When
    var responseReceived = service.persistAnnotationBatch(event, givenUser(),
        ANNOTATION_PATH);

    // Then
    assertThat(responseReceived).isEqualTo(expected);

  }

  @Test
  void testPersistAnnotationBatchIsNull() throws Exception {
    // Given
    var annotationRequest = givenAnnotationRequest();
    given(annotationClient.postAnnotation(any()))
        .willReturn(null);

    // When
    var result = service.persistAnnotation(annotationRequest, givenUser(), ANNOTATION_PATH
    );

    // Then
    assertThat(result).isNull();

  }

  @Test
  void testGetAnnotationBatchCountNpe() {
    // Given
    var badRequest = MAPPER.createObjectNode()
        .set("data", MAPPER.createObjectNode());

    // When / Then
    assertThrowsExactly(InvalidRequestException.class,
        () -> service.getCountForBatchAnnotations(badRequest));
  }

  @Test
  void testUpdateAnnotation() throws Exception {
    // Given
    var expected = givenAnnotationResponseSingleDataNode(ANNOTATION_PATH, ORCID);
    given(repository.getAnnotationForUser(ID, ORCID)).willReturn(1);
    var kafkaResponse = MAPPER.valueToTree(givenAnnotationResponse());
    given(annotationClient.updateAnnotation(any(), any(), any()))
        .willReturn(kafkaResponse);

    // When
    var result = service.updateAnnotation(ID, givenAnnotationRequest(), givenUser(),
        ANNOTATION_PATH,
        PREFIX, SUFFIX);

    // Then
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void testUpdateAnnotationDoesNotExist() {
    // Given
    given(repository.getAnnotationForUser(ID, ORCID)).willReturn(0);

    // Then
    assertThrowsExactly(NoAnnotationFoundException.class,
        () -> service.updateAnnotation(ID, givenAnnotationRequest(), givenUser(),
            ANNOTATION_PATH, PREFIX, SUFFIX));
  }

  @Test
  void testGetAnnotationsByVersion() throws Exception {
    // Given
    int version = 1;
    var annotationNode = MAPPER.valueToTree(givenAnnotationResponse(ID));
    given(mongoRepository.getByVersion(ID, version, "annotation_provenance")).willReturn(
        annotationNode);
    var expected = new JsonApiWrapper(
        new JsonApiData(ID, "ods:Annotation", annotationNode),
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
    try (var mockedStatic = mockStatic(DigitalServiceUtils.class)) {
      mockedStatic.when(() -> DigitalServiceUtils.createVersionNode(versionsList, MAPPER))
          .thenReturn(versionsNode);
      // When
      var responseReceived = service.getAnnotationVersions(ID, ANNOTATION_PATH);

      // Then
      assertThat(responseReceived).isEqualTo(responseExpected);
    }
  }

  @Test
  void testDeleteAnnotation() throws Exception {
    // Given
    given(repository.getAnnotationForUser(ID, ORCID)).willReturn(1);

    // When
    var result = service.deleteAnnotation(PREFIX, SUFFIX, ORCID);

    // Then
    assertThat(result).isTrue();
  }

  @Test
  void testDeleteAnnotationDoesNotExist() {
    // Given
    given(repository.getAnnotationForUser(ID, ORCID)).willReturn(0);

    // Then
    assertThrowsExactly(NoAnnotationFoundException.class,
        () -> service.deleteAnnotation(PREFIX, SUFFIX, ORCID));
  }

}
