package eu.dissco.backend.service;

import static eu.dissco.backend.util.TestUtils.ANNOTATION_CREATOR;
import static eu.dissco.backend.util.TestUtils.ANNOTATION_ID;
import static eu.dissco.backend.util.TestUtils.givenAnnotation;
import static eu.dissco.backend.util.TestUtils.givenAnnotationRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.dissco.backend.repository.AnnotationRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnnotationServiceTest {

  @Mock
  private AnnotationRepository repository;

  private AnnotationService service;

  @BeforeEach
  void setup() {
    this.service = new AnnotationService(repository);
  }

  @Test
  void testPersistAnnotation() throws JsonProcessingException {
    // Given

    // When
    service.persistAnnotation(givenAnnotationRequest(), ANNOTATION_CREATOR);

    // Then
    then(repository).should().saveAnnotation(eq(givenAnnotationRequest()), eq(ANNOTATION_CREATOR));
  }

  @Test
  void testUpdateAnnotation() throws JsonProcessingException {
    // Given

    // When
    service.updateAnnotation(givenAnnotationRequest(), ANNOTATION_CREATOR);

    // Then
    then(repository).should().updateAnnotation(eq(givenAnnotationRequest()), eq(ANNOTATION_CREATOR));
  }

  @Test
  void testDeleteAnnotation() {
    // Given

    // When
    service.deleteAnnotation(ANNOTATION_ID);

    // Then
    then(repository).should().deleteAnnotation(ANNOTATION_ID);
  }

  @Test
  void testGetAnnotationsForUser() throws JsonProcessingException {
    // Given
    given(repository.getAnnotationsForUser(anyString())).willReturn(List.of(givenAnnotation()));

    // When
    var annotation = service.getAnnotationsForUser(ANNOTATION_CREATOR);

    // Then
    assertThat(annotation).isEqualTo(List.of(givenAnnotation()));
  }

  @Test
  void testGetAnnotation() throws JsonProcessingException {
    // Given
    given(repository.getAnnotation(anyString())).willReturn(givenAnnotation());

    // When
    var annotation = service.getAnnotation(ANNOTATION_ID);

    // Then
    assertThat(annotation).isEqualTo(givenAnnotation());
  }

}
