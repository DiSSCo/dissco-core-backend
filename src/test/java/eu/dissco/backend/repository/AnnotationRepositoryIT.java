package eu.dissco.backend.repository;

import com.fasterxml.jackson.annotation.JsonInclude.Include;

import static eu.dissco.backend.TestUtils.givenAnnotationResponse;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.database.jooq.tables.NewAnnotation;
import eu.dissco.backend.domain.AnnotationResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jooq.JSONB;
import org.jooq.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AnnotationRepositoryIT extends BaseRepositoryIT {
  private AnnotationRepository repository;
  private ObjectMapper mapper;

  @BeforeEach
  private void setup(){
    mapper = new ObjectMapper().findAndRegisterModules();
    mapper.setSerializationInclusion(Include.NON_NULL);
    repository = new AnnotationRepository(context, mapper);
  }

  @Test
  void testGetAnnotationsForUser(){
    // Given
    String userId = "userId";
    int pageNumber = 0;
    int pageSize = 11;
    List<AnnotationResponse> annotationsNonTarget = Collections.nCopies(pageSize, givenAnnotationResponse());
    //postAnnotations(annotationsNonTarget);
    List<AnnotationResponse> expectedResponse = Collections.nCopies(pageSize, givenAnnotationResponse(userId));
    //postAnnotations(expectedResponse);

    // When
    var receivedResponse = repository.getAnnotationsForUser(userId, pageNumber, pageSize);

    // Then
    //assertThat(receivedResponse).isEqualTo(expectedResponse);
  }


  private void postAnnotations(List<AnnotationResponse> annotations){
    List<Query> queryList = new ArrayList<>();
    for (var annotation : annotations){
      var query = context.insertInto(NewAnnotation.NEW_ANNOTATION)
          .set(NewAnnotation.NEW_ANNOTATION.ID, annotation.id())
          .set(NewAnnotation.NEW_ANNOTATION.VERSION, annotation.version())
          .set(NewAnnotation.NEW_ANNOTATION.TYPE, annotation.type())
          .set(NewAnnotation.NEW_ANNOTATION.MOTIVATION, annotation.motivation())
          .set(NewAnnotation.NEW_ANNOTATION.TARGET_ID, annotation.target().get("id").toString())
          .set(NewAnnotation.NEW_ANNOTATION.TARGET_BODY, JSONB.jsonb(annotation.target().asText()))
          .set(NewAnnotation.NEW_ANNOTATION.BODY, JSONB.jsonb(annotation.body().toString()))
          .set(NewAnnotation.NEW_ANNOTATION.PREFERENCE_SCORE, annotation.preferenceScore())
          .set(NewAnnotation.NEW_ANNOTATION.CREATOR, annotation.creator())
          .set(NewAnnotation.NEW_ANNOTATION.CREATOR, annotation.created().toString())
          .set(NewAnnotation.NEW_ANNOTATION.GENERATOR_ID, annotation.generator().get("id").toString())
          .set(NewAnnotation.NEW_ANNOTATION.GENERATOR_BODY, JSONB.jsonb(annotation.generator().toString()))
          .set(NewAnnotation.NEW_ANNOTATION.GENERATED, annotation.generated());
      queryList.add(query);
    }
    context.batch(queryList).execute();
  }


}
