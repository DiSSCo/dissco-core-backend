package eu.dissco.backend.configuration;

import eu.dissco.backend.exceptions.WebAuthenticationException;
import eu.dissco.backend.exceptions.WebProcessingFailedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;

@Slf4j
public class WebClientErrorHandling {

  private WebClientErrorHandling() {
  }

  public static Mono<ClientResponse> exchangeFilterResponseProcessor(ClientResponse response,
      String serviceName) {
    var status = response.statusCode();
    if (status.is4xxClientError() || status.is5xxServerError()) {
      if (HttpStatus.UNAUTHORIZED.equals(status)) {
        return response.bodyToMono(JsonNode.class)
            .flatMap(body -> {
              log.error("Unable to authenticate with the {} Service: {}", serviceName, body);
              return Mono.error(
                  new WebAuthenticationException(
                      "Unable to authenticate with " + serviceName + " service"));
            });
      }
      return response.bodyToMono(JsonNode.class)
          .flatMap(body -> {
            log.error("An error has occurred with the {} service. Status: {}, response: {}",
                serviceName, status, body);
            return Mono.error(
                new WebProcessingFailedException(
                    "An error has occurred with the " + serviceName + " service"));
          });
    }
    return Mono.just(response);
  }

}
