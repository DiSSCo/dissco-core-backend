package eu.dissco.backend.client;


import eu.dissco.backend.domain.annotation.AnnotationTombstoneWrapper;
import eu.dissco.backend.domain.annotation.batch.AnnotationEvent;
import eu.dissco.backend.exceptions.WebProcessingFailedException;
import eu.dissco.backend.schema.Annotation;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;
import tools.jackson.databind.JsonNode;

public interface AnnotationClient {

  @PostExchange(value = "")
  JsonNode postAnnotation(@RequestBody Annotation annotation) throws WebProcessingFailedException;

  @PostExchange(value = "/batch")
  JsonNode postAnnotationBatch(@RequestBody AnnotationEvent event)
      throws WebProcessingFailedException;

  @PutExchange(value = "/{prefix}/{suffix}")
  JsonNode updateAnnotation(@PathVariable String prefix, @PathVariable String suffix,
      @RequestBody Annotation annotation) throws WebProcessingFailedException;

  @DeleteExchange(value = "/{prefix}/{suffix}")
  void tombstoneAnnotation(@PathVariable String prefix, @PathVariable String suffix,
      @RequestBody AnnotationTombstoneWrapper annotationTombstoneWrapper)
      throws WebProcessingFailedException;
}
