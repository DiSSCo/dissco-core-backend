package eu.dissco.backend.controller;

import static eu.dissco.backend.TestUtils.FORBIDDEN_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;

import eu.dissco.backend.exceptions.ConflictException;
import eu.dissco.backend.exceptions.ForbiddenException;
import eu.dissco.backend.exceptions.InvalidAnnotationRequestException;
import eu.dissco.backend.exceptions.MasSchedulingException;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.exceptions.PidException;
import eu.dissco.backend.exceptions.UnknownParameterException;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class RestResponseEntityExceptionHandlerTest {

  private RestResponseEntityExceptionHandler exceptionHandler;

  @BeforeEach
  void setup() {
    exceptionHandler = new RestResponseEntityExceptionHandler();
  }

  @Test
  void testNotFoundException() {
    // Given

    // When
    var result = exceptionHandler.handleException(new NotFoundException());

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void testNotFoundExceptionMessage() {
    // Given

    // When
    var result = exceptionHandler.handleException(new NotFoundException(""));

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void testIOExceptionMessage() {
    // Given

    // When
    var result = exceptionHandler.handleIOException(new IOException());

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
  }

  @Test
  void testPidCreationExceptionMessage() {
    // Given

    // When
    var result = exceptionHandler.handlePidCreationException(new PidException(""));

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
  }

  @Test
  void testForbiddenException() {
    // Given

    // When
    var result = exceptionHandler.handleException(new ForbiddenException(FORBIDDEN_MESSAGE));

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void testConflictException() {
    // Given

    // When
    var result = exceptionHandler.handleException(new ConflictException());

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  void testIllegalArgumentException() {
    // Given

    // When
    var result = exceptionHandler.handleException(new IllegalArgumentException());

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void testMasSchedulingException() {
    // Given

    // When
    var result = exceptionHandler.handleException(new MasSchedulingException(""));

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void testUnknownParameterException() {
    // Given

    // When
    var result = exceptionHandler.handleException(new UnknownParameterException(""));

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void testInvalidAnnotationRequestException() {
    // Given

    // When
    var result = exceptionHandler.handleException(new InvalidAnnotationRequestException(""));

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }
}
