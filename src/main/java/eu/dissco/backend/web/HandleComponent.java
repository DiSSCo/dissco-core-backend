package eu.dissco.backend.web;

import com.fasterxml.jackson.databind.JsonNode;
import eu.dissco.backend.component.FdoComponent;
import eu.dissco.backend.exceptions.PidCreationException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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
  private final FdoComponent fdoComponent;

  public List<String> postHandle(int n)
      throws PidCreationException {
    var request = Collections.nCopies(n, fdoComponent.getPostRequest());
    var requestBody = BodyInserters.fromValue(request);
    var response = sendRequest(requestBody);
    var result = validateResponse(response);
    try {
      var dataNode = result.get("data");
      if (!dataNode.isArray()) {
        throw new PidCreationException("Unexpected response from Handle API");
      }
      var handles = new ArrayList<String>();
      for (var node : dataNode) {
        handles.add(node.get("id").asText());
      }
      return handles;
    } catch (NullPointerException e) {
      log.error("Unexpected response from handle API: {}", result);
      throw new PidCreationException("Unexpected response from Handle API");
    }
  }

  private <T> Mono<JsonNode> sendRequest(
      BodyInserter<T, ReactiveHttpOutputMessage> requestBody) {
    var token = "Bearer " + tokenAuthenticator.getToken();
    return handleClient
        .post()
        .uri(uriBuilder -> uriBuilder.path("batch").build())
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

  private JsonNode validateResponse(Mono<JsonNode> response) {
    try {
      return response.toFuture().get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error("Interrupted exception has occurred.");
      throw new PidCreationException(
          "Interrupted execution: A connection error has occurred in creating a jobId.");
    } catch (ExecutionException e) {
      log.error("PID creation failed.", e.getCause());
      throw new PidCreationException(e.getCause().getMessage());
    }
  }

}
