package eu.dissco.backend.web;

import static eu.dissco.backend.TestUtils.MAPPER;
import static org.mockito.BDDMockito.given;

import eu.dissco.backend.configuration.WebClientErrorHandlingConfiguration;
import eu.dissco.backend.exceptions.PidAuthorizationException;
import eu.dissco.backend.exceptions.PidException;
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
class WebClientErrorHandlingConfigurationTest {

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
    var result = WebClientErrorHandlingConfiguration.exchangeFilterResponseProcessor(
        clientResponse);

    // Then
    StepVerifier.create(result)
        .expectError(PidAuthorizationException.class)
        .verify();
  }

  @Test
  void testServerErrorResponse() {
    // Given
    given(clientResponse.statusCode()).willReturn(HttpStatus.UNPROCESSABLE_CONTENT);
    given(clientResponse.bodyToMono(JsonNode.class)).willReturn(
        Mono.just(MAPPER.createObjectNode()));

    // When
    var result = WebClientErrorHandlingConfiguration.exchangeFilterResponseProcessor(
        clientResponse);

    // Then
    StepVerifier.create(result)
        .expectError(PidException.class)
        .verify();
  }

  @Test
  void testOkResponse() {
    // Given
    given(clientResponse.statusCode()).willReturn(HttpStatus.OK);

    // When
    var result = WebClientErrorHandlingConfiguration.exchangeFilterResponseProcessor(
        clientResponse);

    // Then
    StepVerifier.create(result)
        .expectNext(clientResponse)
        .verifyComplete();
  }

}
