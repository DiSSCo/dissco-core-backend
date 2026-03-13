package eu.dissco.backend.configuration;

import eu.dissco.backend.exceptions.PidAuthorizationException;
import eu.dissco.backend.exceptions.PidException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;

@Slf4j
public class WebClientErrorHandlingConfiguration {

  private WebClientErrorHandlingConfiguration() {
  }

  public static Mono<ClientResponse> exchangeFilterResponseProcessor(ClientResponse response) {
    var status = response.statusCode();
    if (status.is4xxClientError() || status.is5xxServerError()) {
      if (HttpStatus.UNAUTHORIZED.equals(status)) {
        return response.bodyToMono(JsonNode.class)
            .flatMap(body -> {
              log.error("Unable to authenticate with the Handle Service: {}", body);
              return Mono.error(
                  new PidAuthorizationException("Unable to authenticate with the Handle Service"));
            });
      }
      return response.bodyToMono(JsonNode.class)
          .flatMap(body -> {
            log.error("An error has occurred with the Handle Service. Status: {}, response: {}",
                status, body);
            return Mono.error(new PidException("An error has occurred with the Handle Service"));
          });
    }
    return Mono.just(response);
  }

}
