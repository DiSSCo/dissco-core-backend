package eu.dissco.backend.web;

import com.fasterxml.jackson.databind.JsonNode;
import eu.dissco.backend.exceptions.PidCreationException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Component
@RequiredArgsConstructor
@Slf4j
public class HandleComponent {

  @Qualifier("handleClient")
  private final WebClient handleClient;
  private final TokenAuthenticator tokenAuthenticator;

  private static final String UNEXPECTED_MSG = "Unexpected response from handle API.";

  public String postHandle(List<JsonNode> request)
      throws PidCreationException {
    var requestBody = BodyInserters.fromValue(request);
    var response = sendRequest(HttpMethod.POST, requestBody, "batch");
    var responseJson = validateResponse(response);
    return getHandleName(responseJson);
  }

  public void updateHandle(List<JsonNode> request)
      throws PidCreationException {
    var requestBody = BodyInserters.fromValue(request);
    var response = sendRequest(HttpMethod.PATCH, requestBody, "");
    validateResponse(response);
  }

  public void rollbackHandleCreation(JsonNode request)
      throws PidCreationException {
    var requestBody = BodyInserters.fromValue(request);
    var response = sendRequest(HttpMethod.DELETE, requestBody, "rollback");
    validateResponse(response);
  }

  public void rollbackHandleUpdate(List<JsonNode> request)
      throws PidCreationException {
    var requestBody = BodyInserters.fromValue(request);
    var response = sendRequest(HttpMethod.DELETE, requestBody, "rollback/update");
    validateResponse(response);
  }

  public void archiveHandle(JsonNode request, String handle) throws PidCreationException {
    var requestBody = BodyInserters.fromValue(request);
    var response = sendRequest(HttpMethod.PUT, requestBody, handle);
    validateResponse(response);
  }

  private <T> Mono<JsonNode> sendRequest(HttpMethod httpMethod,
      BodyInserter<T, ReactiveHttpOutputMessage> requestBody, String endpoint)
      throws PidCreationException {
    var token = "Bearer " + tokenAuthenticator.getToken();
    return handleClient
        .method(httpMethod)
        .uri(uriBuilder -> uriBuilder.path(endpoint).build())
        .body(requestBody)
        .header("Authorization", token)
        .acceptCharset(StandardCharsets.UTF_8)
        .retrieve()
        .onStatus(HttpStatus.UNAUTHORIZED::equals,
            r -> Mono.error(
                new PidCreationException("Unable to authenticate with Handle Service.")))
        .onStatus(HttpStatusCode::is4xxClientError, r -> Mono.error(new PidCreationException(
            "Unable to create PID. Response from Handle API: " + r.statusCode())))
        .bodyToMono(JsonNode.class).retryWhen(
            Retry.fixedDelay(3, Duration.ofSeconds(2)).filter(WebClientUtils::is5xxServerError)
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> new PidCreationException(
                    "External Service failed to process after max retries")));
  }

  private JsonNode validateResponse(Mono<JsonNode> response) throws PidCreationException {
    try {
      return response.toFuture().get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error("Interrupted exception has occurred.");
      throw new PidCreationException(
          "Interrupted execution: A connection error has occurred in creating a handle.");
    } catch (ExecutionException e) {
      log.error("PID creation failed.", e.getCause());
      throw new PidCreationException(e.getCause().getMessage());
    }
  }

  private String getHandleName(JsonNode jsonResponse) throws PidCreationException {
    try {
      return jsonResponse.get("data").get(0).get("id").asText();
    } catch (NullPointerException e) {
      log.error(UNEXPECTED_MSG + " Response: {}", jsonResponse.toPrettyString());
      throw new PidCreationException(UNEXPECTED_MSG);
    }
  }

}
