package eu.dissco.backend.web;

import static org.springframework.http.HttpMethod.POST;

import com.fasterxml.jackson.databind.JsonNode;
import eu.dissco.backend.component.FdoRecordComponent;
import eu.dissco.backend.exceptions.PidException;
import eu.dissco.backend.schema.VirtualCollection;
import eu.dissco.backend.schema.VirtualCollectionRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
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
  private static final String ERROR_MESSAGE = "Error occurred while creating PID: {}";

  @Qualifier("handleClient")
  private final WebClient handleClient;
  private final TokenAuthenticator tokenAuthenticator;
  private final FdoRecordComponent fdoRecordComponent;

  public String postHandleVirtualCollection(VirtualCollectionRequest virtualCollection)
      throws PidException {
    var request = fdoRecordComponent.getPostRequest(virtualCollection);
    var requestBody = BodyInserters.fromValue(request);
    var response = sendRequest(POST, requestBody, "");
    var result = validateResponse(response);
    try {
      return result.get("data").get(0).get("id").asText();
    } catch (NullPointerException e) {
      log.error(ERROR_MESSAGE, result);
      throw new PidException("Unexpected response from Handle API");
    }
  }

  public List<String> postHandle(int n) throws PidException {
    var request = Collections.nCopies(n, fdoRecordComponent.getPostRequest());
    var requestBody = BodyInserters.fromValue(request);
    var response = sendRequest(POST, requestBody, "batch");
    var result = validateResponse(response);
    try {
      var dataNode = result.get("data");
      if (!dataNode.isArray()) {
        log.error(ERROR_MESSAGE, result);
        throw new PidException("Unexpected response from Handle API");
      }
      var handles = new ArrayList<String>();
      for (var node : dataNode) {
        handles.add(node.get("id").asText());
      }
      return handles;
    } catch (NullPointerException e) {
      log.error(ERROR_MESSAGE, result);
      throw new PidException("Unexpected response from Handle API");
    }
  }

  private <T> Mono<JsonNode> sendRequest(HttpMethod httpMethod,
      BodyInserter<T, ReactiveHttpOutputMessage> requestBody, String endpoint) throws PidException {
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
                new PidException("Unable to authenticate with Handle Service.")))
        .onStatus(HttpStatusCode::is4xxClientError, r -> Mono.error(new PidException(
            "Unable to create PID. Response from Handle API: " + r.statusCode())))
        .bodyToMono(JsonNode.class).retryWhen(
            Retry.fixedDelay(3, Duration.ofSeconds(2)).filter(WebClientUtils::is5xxServerError)
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> new PidException(
                    "External Service failed to process after max retries")));
  }

  private JsonNode validateResponse(Mono<JsonNode> response) throws PidException {
    try {
      return response.toFuture().get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error("Interrupted exception has occurred.");
      throw new PidException(
          "Interrupted execution: A connection error has occurred in creating a jobId.");
    } catch (ExecutionException e) {
      log.error("PID creation failed.", e.getCause());
      throw new PidException(e.getCause().getMessage());
    }
  }

  public void rollbackVirtualCollection(String id) throws PidException {
    var request = fdoRecordComponent.getRollbackCreateRequest(id);
    var requestBody = BodyInserters.fromValue(request);
    var response = sendRequest(HttpMethod.DELETE, requestBody, "rollback/create");
    validateResponse(response);
  }

  public void tombstoneHandle(String handle) throws PidException {
    var request = fdoRecordComponent.getTombstoneRequest(handle);
    var requestBody = BodyInserters.fromValue(request);
    var response = sendRequest(HttpMethod.PUT, requestBody, handle);
    validateResponse(response);
  }

  public void updateHandle(VirtualCollection virtualCollection) throws PidException {
    var request = fdoRecordComponent.getPatchHandleRequest(virtualCollection);
    var requestBody = BodyInserters.fromValue(request);
    var response = sendRequest(HttpMethod.PATCH, requestBody, virtualCollection.getId());
    validateResponse(response);
  }
}
