package eu.dissco.backend.client;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;

public interface ProcessorClient {

	@PostExchange("/annotation")
	Mono<Void> acceptAnnotation(@RequestBody JsonNode annotation);

}
