package eu.dissco.backend.repository;

import static eu.dissco.backend.TestUtils.USER_ID_TOKEN;
import static eu.dissco.backend.TestUtils.givenAnnotationJsonApiData;
import static eu.dissco.backend.TestUtils.givenAnnotationJsonApiDataList;
import static eu.dissco.backend.TestUtils.givenAnnotationResponse;
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
    int pageNumber = 0;
    int pageSize = 11;
    List<AnnotationResponse> annotationsNonTarget = List.of(
        givenAnnotationResponse("test", "test"));
    postAnnotations(annotationsNonTarget);

    List<String> annotationIds = IntStream.rangeClosed(0, pageSize - 1).boxed().map(
        Object::toString).toList();
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
  void testGetAnnotationsForUserJsonResponse() {
    // Given
    String userId = USER_ID_TOKEN;
    int pageNumber = 0;
    int pageSize = 11;
    List<String> annotationIds = IntStream.rangeClosed(0, pageSize - 1).boxed().map(
        Object::toString).toList();

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
  void testgetAnnotationsCountForUser() {
    // Given
    String userId = USER_ID_TOKEN;
    int userAnnotationsCount = 10;
    int pageSize = 5;
    int expectedCount = userAnnotationsCount / pageSize;
    List<AnnotationResponse> annotationsNonTarget = List.of(
        givenAnnotationResponse("test", "test"));
    postAnnotations(annotationsNonTarget);
    List<AnnotationResponse> userAnnotations = new ArrayList<>();
    List<String> annotationIds = IntStream.rangeClosed(0, userAnnotationsCount - 1).boxed().map(
        Object::toString).toList();
    for (String annotationId : annotationIds) {
      userAnnotations.add(givenAnnotationResponse(userId, annotationId));
    }
    postAnnotations(userAnnotations);

    // When
    int receivedCount = repository.getAnnotationsCountForUser(userId, pageSize);

    // Then
    assertThat(receivedCount).isEqualTo(expectedCount);
  }

  @Test
  void testGetAnnotation() {
    // Given
    String userId = USER_ID_TOKEN;
    String targetId = "2";
    List<AnnotationResponse> annotations = new ArrayList<>();
    List<String> annotationIds = IntStream.rangeClosed(0, 10).boxed().map(
        Object::toString).toList();
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
    String userId = USER_ID_TOKEN;
    var annotationsReceivedAll = new ArrayList<>();

    List<AnnotationResponse> annotationsAll = new ArrayList<>();
    List<String> annotationIds = IntStream.rangeClosed(0, (pageSize * 2 - 1)).boxed().map(
        Object::toString).toList();
    for (String annotationId : annotationIds) {
      annotationsAll.add(givenAnnotationResponse(userId, annotationId));
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
  }

  @Test
  void testGetAnnotationsJsonResponse() {
    // Given
    int pageNumber = 1;
    int pageSize = 10;
    String userId = USER_ID_TOKEN;

    List<AnnotationResponse> annotationsAll = new ArrayList<>();
    List<String> annotationIds = IntStream.rangeClosed(0, (pageSize - 1)).boxed().map(
        Object::toString).toList();
    for (String annotationId : annotationIds) {
      annotationsAll.add(givenAnnotationResponse(userId, annotationId));
    }
    postAnnotations(annotationsAll);
    var expectedResponse = givenAnnotationJsonApiDataList(userId, annotationIds);

    // When
    var receivedResponse = repository.getAnnotationsJsonResponse(pageNumber, pageSize);

    // Then
    assertThat(receivedResponse).hasSameElementsAs(expectedResponse);
  }

  @Test
  void testGetAnnotationsCountGlobal() {
    // Given
    int pageSize = 5;
    int expectedPageCount = 2;
    String userId = USER_ID_TOKEN;
    List<AnnotationResponse> annotationsAll = new ArrayList<>();
    List<String> annotationIds = IntStream.rangeClosed(0, (pageSize * expectedPageCount - 1))
        .boxed().map(
            Object::toString).toList();
    for (String annotationId : annotationIds) {
      annotationsAll.add(givenAnnotationResponse(userId, annotationId));
    }
    postAnnotations(annotationsAll);

    // When
    int receivedPageCount = repository.getAnnotationsCountGlobal(pageSize);

    // Then
    assertThat(receivedPageCount).isEqualTo(expectedPageCount);
  }

  @Test
  void testGetAnnotationVersion() {
    // Given
    String annotationId = "123";
    String userId = USER_ID_TOKEN;
    int targetVersion = 2;
    List<AnnotationResponse> annotationsAll = new ArrayList<>();
    AnnotationResponse version1 = givenAnnotationResponse(userId, annotationId);
    AnnotationResponse version2 = new AnnotationResponse(
        version1.id(),
        targetVersion,
        version1.type(),
        version1.motivation(),
        version1.target(),
        version1.body(),
        version1.preferenceScore(),
        version1.creator(),
        version1.created(),
        version1.generator(),
        version1.generated(),
        version1.deleted_on()
    );
    annotationsAll.add(version1);
    annotationsAll.add(version2);
    postAnnotations(annotationsAll);

    // When
    var receivedResponse = repository.getAnnotationVersion(annotationId, targetVersion);

    // Then
    assertThat(version2).isEqualTo(receivedResponse);
  }

  private void postAnnotations(List<AnnotationResponse> annotations) {
    List<Query> queryList = new ArrayList<>();
    for (var annotation : annotations) {
      var query = context.insertInto(NEW_ANNOTATION)
          .set(NEW_ANNOTATION.ID, annotation.id())
          .set(NEW_ANNOTATION.VERSION, annotation.version())
          .set(NEW_ANNOTATION.TYPE, annotation.type())
          .set(NEW_ANNOTATION.MOTIVATION, annotation.motivation())
          .set(NEW_ANNOTATION.TARGET_ID, annotation.target().get("id").toString())
          .set(NEW_ANNOTATION.TARGET_BODY, JSONB.jsonb(annotation.target().toString()))
          .set(NEW_ANNOTATION.BODY, JSONB.jsonb(annotation.body().toString()))
          .set(NEW_ANNOTATION.PREFERENCE_SCORE, annotation.preferenceScore())
          .set(NEW_ANNOTATION.CREATOR, annotation.creator())
          .set(NEW_ANNOTATION.CREATED, annotation.created())
          .set(NEW_ANNOTATION.GENERATOR_ID, annotation.generator().get("id").toString())
          .set(NEW_ANNOTATION.GENERATOR_BODY, JSONB.jsonb(annotation.generator().toString()))
          .set(NEW_ANNOTATION.GENERATED, annotation.generated())
          .set(NEW_ANNOTATION.LAST_CHECKED, annotation.created());
      queryList.add(query);
    }
    context.batch(queryList).execute();
  }

}
