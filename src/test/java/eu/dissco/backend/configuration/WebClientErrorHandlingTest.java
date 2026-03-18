package eu.dissco.backend.configuration;

import static eu.dissco.backend.TestUtils.MAPPER;
import static org.mockito.BDDMockito.given;

import eu.dissco.backend.exceptions.WebAuthenticationException;
import eu.dissco.backend.exceptions.WebProcessingFailedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tools.jackson.databind.JsonNode;

@ExtendWith(MockitoExtension.class)
class WebClientErrorHandlingTest {

  @Mock
  private ClientResponse clientResponse;

  @Mock
  WebClientResponseException webClientResponseException;

  @Test
  void testUnauthorizedResponse() {
    // Given
    given(clientResponse.statusCode()).willReturn(HttpStatus.UNAUTHORIZED);
    given(clientResponse.bodyToMono(JsonNode.class)).willReturn(
        Mono.just(MAPPER.createObjectNode()));

    // When
    var result = WebClientErrorHandling.exchangeFilterResponseProcessor(
        clientResponse, "Handle");

    // Then
    StepVerifier.create(result)
        .expectError(WebAuthenticationException.class)
        .verify();
  }

  @Test
  void testServerErrorResponse() {
    // Given
    given(clientResponse.statusCode()).willReturn(HttpStatus.UNPROCESSABLE_CONTENT);
    given(clientResponse.bodyToMono(JsonNode.class)).willReturn(
        Mono.just(MAPPER.createObjectNode()));

    // When
    var result = WebClientErrorHandling.exchangeFilterResponseProcessor(
        clientResponse, "Annotation");

    // Then
    StepVerifier.create(result)
        .expectError(WebProcessingFailedException.class)
        .verify();
  }

  @Test
  void testOkResponse() {
    // Given
    given(clientResponse.statusCode()).willReturn(HttpStatus.OK);

    // When
    var result = WebClientErrorHandling.exchangeFilterResponseProcessor(
        clientResponse, "Mas Scheduler");

    // Then
    StepVerifier.create(result)
        .expectNext(clientResponse)
        .verifyComplete();
  }

}
