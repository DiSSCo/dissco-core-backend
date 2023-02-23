package eu.dissco.backend.repository;

import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.PREFIX;
import static eu.dissco.backend.TestUtils.TARGET_ID;
import static eu.dissco.backend.TestUtils.USER_ID_TOKEN;
import static eu.dissco.backend.TestUtils.givenAnnotationJsonApiData;
import static eu.dissco.backend.TestUtils.givenAnnotationJsonApiDataList;
import static eu.dissco.backend.TestUtils.givenAnnotationResponse;
import static eu.dissco.backend.TestUtils.givenAnnotationResponseTarget;
import static eu.dissco.backend.database.jooq.tables.NewAnnotation.NEW_ANNOTATION;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.domain.AnnotationResponse;
import eu.dissco.backend.domain.JsonApiData;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import org.jooq.JSONB;
import org.jooq.Query;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AnnotationRepositoryIT extends BaseRepositoryIT {

  private AnnotationRepository repository;

  @BeforeEach
  void setup() {
    ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
    mapper.setSerializationInclusion(Include.NON_NULL);
    repository = new AnnotationRepository(context, mapper);
  }

  @AfterEach
  void destroy() {
    context.truncate(NEW_ANNOTATION).execute();
  }

  @Test
  void testGetAnnotationsForUser() {
    // Given
    String userId = USER_ID_TOKEN;
    int pageNumber = 1;
    int pageSize = 11;
    List<AnnotationResponse> annotationsNonTarget = List.of(
        givenAnnotationResponse("test", "test"));
    postAnnotations(annotationsNonTarget);

    List<String> annotationIds = IntStream.rangeClosed(0, pageSize - 1).boxed()
        .map(Object::toString).toList();
    List<AnnotationResponse> expectedResponse = new ArrayList<>();

    for (String id : annotationIds) {
      expectedResponse.add(givenAnnotationResponse(userId, id));
    }
    postAnnotations(expectedResponse);

    // When
    var receivedResponse = repository.getAnnotationsForUser(userId, pageNumber, pageSize);

    // Then
    assertThat(receivedResponse).hasSameElementsAs(expectedResponse);
  }

  @Test
  void testGetAnnotationsForUserSecondPage() {
    // Given
    String userId = USER_ID_TOKEN;
    int pageNumber = 2;
    int pageSize = 11;
    List<AnnotationResponse> annotationsNonTarget = List.of(
        givenAnnotationResponse("test", "test"));
    postAnnotations(annotationsNonTarget);
    List<String> annotationIds = IntStream.rangeClosed(0, (pageSize * 2) - 1).boxed()
        .map(Object::toString).toList();
    List<AnnotationResponse> annotations = new ArrayList<>();
    for (String id : annotationIds) {
      annotations.add(givenAnnotationResponse(userId, id));
    }
    postAnnotations(annotations);

    // When
    var receivedResponse = repository.getAnnotationsForUser(userId, pageNumber, pageSize);

    // Then
    assertThat(receivedResponse).hasSize(pageSize);
  }

  @Test
  void testGetAnnotationsForUserJsonResponse() {
    // Given
    String userId = USER_ID_TOKEN;
    int pageNumber = 1;
    int pageSize = 11;
    List<String> annotationIds = IntStream.rangeClosed(0, pageSize - 1).boxed()
        .map(Object::toString).toList();
    List<AnnotationResponse> userAnnotations = new ArrayList<>();
    List<JsonApiData> expectedResponse = new ArrayList<>();
    for (String annotationId : annotationIds) {
      userAnnotations.add(givenAnnotationResponse(userId, annotationId));
      expectedResponse.add(givenAnnotationJsonApiData(userId, annotationId));
    }
    postAnnotations(userAnnotations);

    // When
    var receivedResponse = repository.getAnnotationsForUserJsonResponse(userId, pageNumber,
        pageSize);

    // Then
    assertThat(receivedResponse).hasSameElementsAs(expectedResponse);
  }

  @Test
  void testGetAnnotationsForUserJsonResponseSecondPage() {
    // Given
    String userId = USER_ID_TOKEN;
    int pageNumber = 2;
    int pageSize = 11;
    List<String> annotationIds = IntStream.rangeClosed(0, (pageSize * 2) - 1).boxed()
        .map(Object::toString).toList();

    List<AnnotationResponse> userAnnotations = new ArrayList<>();

    for (String annotationId : annotationIds) {
      userAnnotations.add(givenAnnotationResponse(userId, annotationId));
    }
    postAnnotations(userAnnotations);

    // When
    var receivedResponse = repository.getAnnotationsForUserJsonResponse(userId, pageNumber,
        pageSize);

    // Then
    assertThat(receivedResponse).hasSize(pageSize);
  }

  @Test
  void testGetAnnotation() {
    // Given
    String userId = USER_ID_TOKEN;
    String targetId = "2";
    List<AnnotationResponse> annotations = new ArrayList<>();
    List<String> annotationIds = IntStream.rangeClosed(0, 10).boxed().map(Object::toString)
        .toList();
    for (String annotationId : annotationIds) {
      annotations.add(givenAnnotationResponse(userId, annotationId));
    }
    postAnnotations(annotations);
    var expectedResponse = givenAnnotationResponse(userId, targetId);

    // When
    var receivedResponse = repository.getAnnotation(targetId);

    // Then
    assertThat(receivedResponse).isEqualTo(expectedResponse);
  }

  @Test
  void testGetAnnotations() {
    // Given
    int pageNumber = 1;
    int pageSize = 10;
    var annotationsReceivedAll = new ArrayList<>();

    List<AnnotationResponse> annotationsAll = new ArrayList<>();
    List<String> annotationIds = IntStream.rangeClosed(0, (pageSize * 2 - 1)).boxed()
        .map(Object::toString).toList();
    for (String annotationId : annotationIds) {
      annotationsAll.add(givenAnnotationResponse(USER_ID_TOKEN, annotationId));
    }
    postAnnotations(annotationsAll);

    // When
    var annotationsP1 = repository.getAnnotations(pageNumber, pageSize);
    var annotationsP2 = repository.getAnnotations(pageNumber + 1, pageSize);
    annotationsReceivedAll.addAll(annotationsP1);
    annotationsReceivedAll.addAll(annotationsP2);

    // Then
    assertThat(annotationsP1).hasSize(pageSize);
    assertThat(annotationsP2).hasSize(pageSize);
    assertThat(annotationsReceivedAll).hasSameElementsAs(annotationsAll);
  }

  @Test
  void testGetAnnotationsJsonResponse() {
    // Given
    int pageNumber = 1;
    int pageSize = 10;

    List<AnnotationResponse> annotationsAll = new ArrayList<>();
    List<String> annotationIds = IntStream.rangeClosed(0, (pageSize - 1)).boxed()
        .map(Object::toString).toList();
    for (String annotationId : annotationIds) {
      annotationsAll.add(givenAnnotationResponse(USER_ID_TOKEN, annotationId));
    }
    postAnnotations(annotationsAll);
    var expectedResponse = givenAnnotationJsonApiDataList(USER_ID_TOKEN, annotationIds);

    // When
    var receivedResponse = repository.getAnnotationsJsonResponse(pageNumber, pageSize);

    // Then
    assertThat(receivedResponse).hasSameElementsAs(expectedResponse);
  }

  @Test
  void testGetForTarget() {
    // Given
    var expectedResponse = givenAnnotationResponseTarget(ID, TARGET_ID);
    var annotations = List.of(
        expectedResponse,
        givenAnnotationResponseTarget(PREFIX + "/XXX-XXX-XXX", PREFIX + "/TAR-GET-002"),
        givenAnnotationResponseTarget(PREFIX + "/YYY-YYY-YYY", PREFIX + "/TAR-GET-007")
    );
    postAnnotations(annotations);

    // When
    var receivedResponse = repository.getForTarget(TARGET_ID);

    // Then
    assertThat(receivedResponse).isEqualTo(List.of(expectedResponse));
  }

  @Test
  void testGetAnnotationForUser() {
    // Given
    var annotations = List.of(
        givenAnnotationResponse(USER_ID_TOKEN, ID),
        givenAnnotationResponseTarget("AnotherUser", PREFIX + "/TAR-GET-002"),
        givenAnnotationResponseTarget("JamesBond", PREFIX + "/TAR-GET-007")
    );
    postAnnotations(annotations);

    // When
    var receivedResponse = repository.getAnnotationForUser(ID, USER_ID_TOKEN);

    // Then
    assertThat(receivedResponse).isEqualTo(1);
  }


  private void postAnnotations(List<AnnotationResponse> annotations) {
    List<Query> queryList = new ArrayList<>();
    for (var annotation : annotations) {
      var query = context.insertInto(NEW_ANNOTATION)
          .set(NEW_ANNOTATION.ID, annotation.id())
          .set(NEW_ANNOTATION.VERSION, annotation.version())
          .set(NEW_ANNOTATION.TYPE, annotation.type())
          .set(NEW_ANNOTATION.MOTIVATION, annotation.motivation())
          .set(NEW_ANNOTATION.TARGET_ID, annotation.target().get("id").asText())
          .set(NEW_ANNOTATION.TARGET_BODY, JSONB.jsonb(annotation.target().toString()))
          .set(NEW_ANNOTATION.BODY, JSONB.jsonb(annotation.body().toString()))
          .set(NEW_ANNOTATION.PREFERENCE_SCORE, annotation.preferenceScore())
          .set(NEW_ANNOTATION.CREATOR, annotation.creator())
          .set(NEW_ANNOTATION.CREATED, annotation.created())
          .set(NEW_ANNOTATION.GENERATOR_ID, annotation.generator().get("id").asText())
          .set(NEW_ANNOTATION.GENERATOR_BODY, JSONB.jsonb(annotation.generator().toString()))
          .set(NEW_ANNOTATION.GENERATED, annotation.generated())
          .set(NEW_ANNOTATION.LAST_CHECKED, annotation.created())
          .onConflict(NEW_ANNOTATION.ID).doUpdate()
          .set(NEW_ANNOTATION.VERSION, annotation.version())
          .set(NEW_ANNOTATION.TYPE, annotation.type())
          .set(NEW_ANNOTATION.MOTIVATION, annotation.motivation())
          .set(NEW_ANNOTATION.TARGET_ID, annotation.target().get("id").asText())
          .set(NEW_ANNOTATION.TARGET_BODY, JSONB.jsonb(annotation.target().toString()))
          .set(NEW_ANNOTATION.BODY, JSONB.jsonb(annotation.body().toString()))
          .set(NEW_ANNOTATION.PREFERENCE_SCORE, annotation.preferenceScore())
          .set(NEW_ANNOTATION.CREATOR, annotation.creator())
          .set(NEW_ANNOTATION.CREATED, annotation.created())
          .set(NEW_ANNOTATION.GENERATOR_ID, annotation.generator().get("id").asText())
          .set(NEW_ANNOTATION.GENERATOR_BODY, JSONB.jsonb(annotation.generator().toString()))
          .set(NEW_ANNOTATION.GENERATED, annotation.generated())
          .set(NEW_ANNOTATION.LAST_CHECKED, annotation.created());
      queryList.add(query);
    }
    context.batch(queryList).execute();
  }

}
