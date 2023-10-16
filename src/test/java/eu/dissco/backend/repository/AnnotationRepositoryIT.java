package eu.dissco.backend.repository;

import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.PREFIX;
import static eu.dissco.backend.TestUtils.TARGET_ID;
import static eu.dissco.backend.TestUtils.USER_ID_TOKEN;
import static eu.dissco.backend.database.jooq.Tables.ANNOTATION;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationResponse;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import eu.dissco.backend.domain.annotation.Annotation;
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
    repository = new AnnotationRepository(context, MAPPER);
  }

  @AfterEach
  void destroy() {
    context.truncate(ANNOTATION).execute();
  }

  @Test
  void testGetAnnotation() {
    // Given
    var expectedAnnotation = givenAnnotationResponse();
    postAnnotations(List.of(expectedAnnotation));

    // When
    var result = repository.getAnnotation(ID);

    // Then
    assertThat(result).isEqualTo(expectedAnnotation);
  }

  @Test
  void testGetAnnotations() {
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
  void testGetAnnotationForUser() {
    // Given
    var annotations = List.of(givenAnnotationResponse(ID),
        givenAnnotationResponse(USER_ID_TOKEN, "AnotherUser", PREFIX + "/TAR-GET-002"),
        givenAnnotationResponse(USER_ID_TOKEN, "JamesBond", PREFIX + "/TAR-GET-007"));
    postAnnotations(annotations);

    // When
    var receivedResponse = repository.getAnnotationForUser(ID, USER_ID_TOKEN);

    // Then
    assertThat(receivedResponse).isEqualTo(1);
  }

  @Test
  void testGetForTarget() {
    // Given
    MAPPER.setSerializationInclusion(Include.ALWAYS);
    var expectedResponse = givenAnnotationResponse(USER_ID_TOKEN, ID, TARGET_ID);
    List<Annotation> annotations = List.of(expectedResponse,
        givenAnnotationResponse(USER_ID_TOKEN, PREFIX + "/XXX-XXX-XXX", PREFIX + "/TAR-GET-002"),
        givenAnnotationResponse(USER_ID_TOKEN, PREFIX + "/YYY-YYY-YYY", PREFIX + "/TAR-GET-007"));
    postAnnotations(annotations);

    // When
    var receivedResponse = repository.getForTarget(TARGET_ID);

    // Then
    assertThat(receivedResponse).isEqualTo(List.of(expectedResponse));
  }

  private void postAnnotations(List<Annotation> annotations) {
    List<Query> queryList = new ArrayList<>();
    for (var annotation : annotations) {
      var query = context.insertInto(ANNOTATION).set(ANNOTATION.ID, annotation.getOdsId())
          .set(ANNOTATION.VERSION, annotation.getOdsVersion())
          .set(ANNOTATION.TYPE, annotation.getRdfType())
          .set(ANNOTATION.MOTIVATION, annotation.getOaMotivation().toString())
          .set(ANNOTATION.MOTIVATED_BY, annotation.getOaMotivatedBy())
          .set(ANNOTATION.TARGET_ID, annotation.getOaTarget().getOdsId())
          .set(ANNOTATION.TARGET, JSONB.jsonb(annotation.getOaTarget().toString()))
          .set(ANNOTATION.BODY, JSONB.jsonb(annotation.getOaBody().toString()))
          .set(ANNOTATION.AGGREGATE_RATING,
              JSONB.jsonb(annotation.getOdsAggregateRating().toString()))
          .set(ANNOTATION.CREATOR, JSONB.jsonb(annotation.getOaCreator().toString()))
          .set(ANNOTATION.CREATOR_ID, annotation.getOaCreator().getOdsId())
          .set(ANNOTATION.CREATED, annotation.getDcTermsCreated())
          .set(ANNOTATION.GENERATOR, JSONB.jsonb(annotation.getAsGenerator().toString()))
          .set(ANNOTATION.GENERATED, annotation.getOaGenerated())
          .set(ANNOTATION.LAST_CHECKED, annotation.getDcTermsCreated()).onConflict(ANNOTATION.ID)
          .doUpdate().set(ANNOTATION.VERSION, annotation.getOdsVersion())
          .set(ANNOTATION.TYPE, annotation.getRdfType())
          .set(ANNOTATION.MOTIVATION, annotation.getOaMotivation().toString())
          .set(ANNOTATION.MOTIVATED_BY, annotation.getOaMotivatedBy())
          .set(ANNOTATION.TARGET_ID, annotation.getOaTarget().getOdsId())
          .set(ANNOTATION.TARGET, JSONB.jsonb(annotation.getOaTarget().toString()))
          .set(ANNOTATION.BODY, JSONB.jsonb(annotation.getOaBody().toString()))
          .set(ANNOTATION.AGGREGATE_RATING,
              JSONB.jsonb(annotation.getOdsAggregateRating().toString()))
          .set(ANNOTATION.CREATOR, JSONB.jsonb(annotation.getOaCreator().toString()))
          .set(ANNOTATION.CREATOR_ID, annotation.getOaCreator().getOdsId())
          .set(ANNOTATION.CREATED, annotation.getDcTermsCreated())
          .set(ANNOTATION.GENERATOR, JSONB.jsonb(annotation.getAsGenerator().toString()))
          .set(ANNOTATION.GENERATED, annotation.getOaGenerated())
          .set(ANNOTATION.LAST_CHECKED, annotation.getDcTermsCreated());
      queryList.add(query);
    }
    context.batch(queryList).execute();
  }

}
