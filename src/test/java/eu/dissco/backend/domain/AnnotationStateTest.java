package eu.dissco.backend.domain;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertThrows;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AnnotationStateTest {

  @ParameterizedTest
  @MethodSource("annotationStates")
  void testFromString(AnnotationState expectedState, String stateString) {
    // When
    var result = AnnotationState.fromString(stateString);

    // Then
    assertThat(result).isEqualTo(expectedState);
  }

  @Test
  void testIllegalState() {
    assertThrows(IllegalStateException.class, () -> AnnotationState.fromString("not a state"));
  }

  private static Stream<Arguments> annotationStates() {
    return Stream.of(
        Arguments.of(AnnotationState.FAILED, "failed"),
        Arguments.of(AnnotationState.COMPLETED, "completed"),
        Arguments.of(AnnotationState.SCHEDULED, "scheduled")
    );
  }

}
