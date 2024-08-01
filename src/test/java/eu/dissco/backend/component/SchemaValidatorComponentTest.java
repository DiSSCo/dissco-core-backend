package eu.dissco.backend.component;

import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationEventRequest;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationRequest;
import static eu.dissco.backend.utils.AnnotationUtils.givenBatchMetadata;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import eu.dissco.backend.domain.annotation.batch.AnnotationEventRequest;
import eu.dissco.backend.domain.annotation.batch.BatchMetadata;
import eu.dissco.backend.exceptions.InvalidAnnotationRequestException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SchemaValidatorComponentTest {

  private SchemaValidatorComponent schemaValidator;

  @BeforeEach
  void setup() {
    schemaValidator = new SchemaValidatorComponent();
  }

  @Test
  void testValidateAnnotationEventRequest() {
    // When Then
    assertDoesNotThrow(
        () -> schemaValidator.validateAnnotationEventRequest(givenAnnotationEventRequest(), true));
  }

  @ParameterizedTest
  @MethodSource("invalidEvents")
  void testValidateAnnotationEventRequestInvalid(AnnotationEventRequest event) {
    // When Then
    assertThrows(InvalidAnnotationRequestException.class,
        () -> schemaValidator.validateAnnotationEventRequest(event, true));
  }

  private static Stream<Arguments> invalidEvents() {
    return Stream.of(
        Arguments.of(new AnnotationEventRequest(Collections.nCopies(2, givenAnnotationRequest()),
            List.of(givenBatchMetadata()))),
        Arguments.of(new AnnotationEventRequest(List.of(givenAnnotationRequest()),
            Collections.nCopies(2, givenBatchMetadata()))),
        Arguments.of(new AnnotationEventRequest(List.of(givenAnnotationRequest()),
            List.of(new BatchMetadata(Collections.emptyList())))),
        Arguments.of(new AnnotationEventRequest(List.of(givenAnnotationRequest()),
            Collections.emptyList()))
    );
  }


}
