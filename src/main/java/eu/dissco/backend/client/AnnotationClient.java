package eu.dissco.backend.client;


import eu.dissco.backend.domain.annotation.AnnotationTombstoneWrapper;
import eu.dissco.backend.domain.annotation.batch.AnnotationEvent;
import eu.dissco.backend.schema.Annotation;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;
import tools.jackson.databind.JsonNode;

public interface AnnotationClient {

  @PostExchange(value = "")
  JsonNode postAnnotation(@RequestBody Annotation annotation);

  @PostExchange(value = "/batch")
  JsonNode postAnnotationBatch(@RequestBody AnnotationEvent event);

  @PutExchange(value = "/{prefix}/{suffix}")
  JsonNode updateAnnotation(@PathVariable("prefix") String prefix,
      @PathVariable("suffix") String suffix, @RequestBody Annotation annotation);

  @DeleteExchange(value = "/{prefix}/{suffix}")
  void tombstoneAnnotation(@PathVariable("prefix") String prefix,
      @PathVariable("suffix") String suffix, @RequestBody AnnotationTombstoneWrapper annotationTombstoneWrapper);
}
