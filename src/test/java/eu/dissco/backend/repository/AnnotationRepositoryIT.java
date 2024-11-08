package eu.dissco.backend.repository;

import static eu.dissco.backend.TestUtils.BATCH_ID;
import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.ID_ALT;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.ORCID;
import static eu.dissco.backend.TestUtils.PREFIX;
import static eu.dissco.backend.TestUtils.USER_ID_TOKEN;
import static eu.dissco.backend.database.jooq.Tables.ANNOTATION;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import eu.dissco.backend.exceptions.DisscoJsonBMappingException;
import eu.dissco.backend.schema.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    repository = new AnnotationRepository(context, MAPPER);
  }

  @AfterEach
  void destroy() {
    context.truncate(ANNOTATION).execute();
  }

  @Test
  void testGetAnnotation() throws JsonProcessingException {
    // Given
    var expectedAnnotation = givenAnnotationResponse().withOdsBatchID(BATCH_ID);
    postAnnotations(List.of(expectedAnnotation));

    // When
    var result = repository.getAnnotation(ID);

    // Then
    assertThat(result).isEqualTo(expectedAnnotation);
  }

  @Test
  void testGetAnnotations() throws JsonProcessingException {
    // Given
    int pageNumber = 1;
    int pageSize = 10;

    List<Annotation> annotationsAll = new ArrayList<>();
    List<String> annotationIds = IntStream.rangeClosed(0, (pageSize - 1)).boxed()
        .map(Object::toString).toList();
    for (String annotationId : annotationIds) {
      annotationsAll.add(givenAnnotationResponse(annotationId));

    }
    postAnnotations(annotationsAll);

    // When
    var receivedResponse = repository.getAnnotations(pageNumber, pageSize);

    // Then
    assertThat(receivedResponse).hasSameElementsAs(annotationsAll);
  }

  @Test
  void testGetActiveAnnotationForUser() throws JsonProcessingException {
    // Given
    var annotations = List.of(givenAnnotationResponse(ID),
        givenAnnotationResponse(USER_ID_TOKEN, "AnotherUser", PREFIX + "/TAR-GET-002"),
        givenAnnotationResponse(USER_ID_TOKEN, "JamesBond", PREFIX + "/TAR-GET-007"));
    postAnnotations(annotations);

    // When
    var receivedResponse = repository.getActiveAnnotationForUser(ID, ORCID);

    // Then
    assertThat(receivedResponse).isEqualTo(Optional.of(annotations.get(0)));
  }

  @Test
  void testGetForTarget() throws JsonProcessingException {
    // Given
    MAPPER.setSerializationInclusion(Include.ALWAYS);
    var expectedResponse = givenAnnotationResponse(ID, USER_ID_TOKEN, ID_ALT);
    List<Annotation> annotations = List.of(expectedResponse,
        givenAnnotationResponse(PREFIX + "/XXX-XXX-XXX", PREFIX + "/TAR-GET-002"),
        givenAnnotationResponse(PREFIX + "/YYY-YYY-YYY", PREFIX + "/TAR-GET-007"));
    postAnnotations(annotations);

    // When
    var receivedResponse = repository.getForTarget(ID_ALT);

    // Then
    assertThat(receivedResponse).isEqualTo(List.of(expectedResponse));
  }

  @Test
  void testNullResponse() throws Exception {
    // Given
    var annotation = givenAnnotationResponse(ID);
    var badTarget = MAPPER.readTree("""
        {
          "field":"value"
        }
        """);
    context.insertInto(ANNOTATION)
        .set(ANNOTATION.ID, annotation.getId())
        .set(ANNOTATION.VERSION, annotation.getOdsVersion())
        .set(ANNOTATION.TYPE, annotation.getOdsFdoType())
        .set(ANNOTATION.MOTIVATION, annotation.getOaMotivation().value())
        .set(ANNOTATION.MJR_JOB_ID, annotation.getOdsJobID())
        .set(ANNOTATION.BATCH_ID, annotation.getOdsBatchID())
        .set(ANNOTATION.CREATOR, annotation.getDctermsCreator().getId())
        .set(ANNOTATION.CREATED, annotation.getDctermsCreated().toInstant())
        .set(ANNOTATION.MODIFIED, annotation.getDctermsModified().toInstant())
        .set(ANNOTATION.LAST_CHECKED, annotation.getDctermsCreated().toInstant())
        .set(ANNOTATION.TARGET_ID, annotation.getOaHasTarget().getId())
        .set(ANNOTATION.DATA, JSONB.jsonb(MAPPER.writeValueAsString(badTarget)))
        .execute();

    // When / Then
    assertThrows(DisscoJsonBMappingException.class, () -> repository.getAnnotation(ID));
  }

  private void postAnnotations(List<Annotation> annotations) throws JsonProcessingException {
    List<Query> queryList = new ArrayList<>();
    for (var annotation : annotations) {
      var query = context.insertInto(ANNOTATION)
          .set(ANNOTATION.ID, annotation.getId())
          .set(ANNOTATION.VERSION, annotation.getOdsVersion())
          .set(ANNOTATION.TYPE, annotation.getOdsFdoType())
          .set(ANNOTATION.MOTIVATION, annotation.getOaMotivation().value())
          .set(ANNOTATION.MJR_JOB_ID, annotation.getOdsJobID())
          .set(ANNOTATION.BATCH_ID, annotation.getOdsBatchID())
          .set(ANNOTATION.CREATOR, annotation.getDctermsCreator().getId())
          .set(ANNOTATION.CREATED, annotation.getDctermsCreated().toInstant())
          .set(ANNOTATION.MODIFIED, annotation.getDctermsModified().toInstant())
          .set(ANNOTATION.LAST_CHECKED, annotation.getDctermsCreated().toInstant())
          .set(ANNOTATION.TARGET_ID, annotation.getOaHasTarget().getId())
          .set(ANNOTATION.DATA, JSONB.jsonb(MAPPER.writeValueAsString(annotation)))
          .onConflict(ANNOTATION.ID).doUpdate()
          .set(ANNOTATION.VERSION, annotation.getOdsVersion())
          .set(ANNOTATION.TYPE, annotation.getOdsFdoType())
          .set(ANNOTATION.MOTIVATION, annotation.getOaMotivation().value())
          .set(ANNOTATION.MJR_JOB_ID, annotation.getOdsJobID())
          .set(ANNOTATION.BATCH_ID, annotation.getOdsBatchID())
          .set(ANNOTATION.CREATOR, annotation.getDctermsCreator().getId())
          .set(ANNOTATION.CREATED, annotation.getDctermsCreated().toInstant())
          .set(ANNOTATION.MODIFIED, annotation.getDctermsModified().toInstant())
          .set(ANNOTATION.LAST_CHECKED, annotation.getDctermsCreated().toInstant())
          .set(ANNOTATION.TARGET_ID, annotation.getOaHasTarget().getId())
          .set(ANNOTATION.DATA, JSONB.jsonb(MAPPER.writeValueAsString(annotation)));
      queryList.add(query);
    }
    context.batch(queryList).execute();
  }

}
