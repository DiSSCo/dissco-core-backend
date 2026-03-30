package eu.dissco.backend.client;

import eu.dissco.backend.exceptions.WebProcessingFailedException;
import eu.dissco.backend.schema.Annotation;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

public interface ProcessorClient {

  @PostExchange("/annotation")
  void acceptAnnotation(@RequestBody Annotation annotation) throws WebProcessingFailedException;
}
