package eu.dissco.backend.client;

import eu.dissco.backend.exceptions.WebProcessingFailedException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;
import tools.jackson.databind.JsonNode;

public interface ProcessorClient {

  @PostExchange("/annotation")
  void acceptAnnotation(@RequestBody JsonNode annotation) throws WebProcessingFailedException;
}
