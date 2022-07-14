package eu.dissco.backend.repository;

import static eu.dissco.backend.database.jooq.Tables.ANNOTATION;
import static eu.dissco.backend.util.TestUtils.ANNOTATION_BODY;
import static eu.dissco.backend.util.TestUtils.ANNOTATION_CREATOR;
import static eu.dissco.backend.util.TestUtils.ANNOTATION_TARGET;
import static eu.dissco.backend.util.TestUtils.ANNOTATION_TYPE;
import static eu.dissco.backend.util.TestUtils.givenAnnotationRequest;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AnnotationRepositoryIT extends BaseRepositoryIT {

  private ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
  private AnnotationRepository repository;

  @BeforeEach
  void setup() {
    repository = new AnnotationRepository(context, mapper);
  }

  @AfterEach
  void cleanupTests() {
    context.truncate(ANNOTATION).cascade().execute();
  }

  @Test
  void testSaveAnnotation() throws JsonProcessingException {
    // Given

    // When
    repository.saveAnnotation(givenAnnotationRequest(), ANNOTATION_CREATOR);

    // Then
    var result = context.select(ANNOTATION.asterisk()).from(ANNOTATION).fetchOne();
    assertThat(result.get(ANNOTATION.CREATOR)).isEqualTo(ANNOTATION_CREATOR);
    assertThat(result.get(ANNOTATION.TYPE)).isEqualTo(ANNOTATION_TYPE);
    assertThat(result.get(ANNOTATION.TARGET)).isEqualTo(ANNOTATION_TARGET);
    assertThat(result.get(ANNOTATION.BODY).data()).isEqualTo(ANNOTATION_BODY);
  }

  @Test
  void testGetAnnotationForUser() throws JsonProcessingException {
    // Given
    fillDatabase();

    // When
    var result = repository.getAnnotationsForUser("user1");

    // Then
    assertThat(result).hasSize(3);
  }

  private void fillDatabase() throws JsonProcessingException {
    for (int i = 0; i < 3; i++) {
      repository.saveAnnotation(givenAnnotationRequest(), "user1");
    }
    for (int i = 0; i < 2; i++) {
      repository.saveAnnotation(givenAnnotationRequest(), "user2");
    }
  }

}
