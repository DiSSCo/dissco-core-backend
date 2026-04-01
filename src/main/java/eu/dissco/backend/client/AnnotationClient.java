package eu.dissco.backend.client;


import eu.dissco.backend.domain.annotation.AnnotationTombstoneWrapper;
import eu.dissco.backend.domain.annotation.batch.AnnotationEvent;
import eu.dissco.backend.exceptions.WebProcessingFailedException;
import eu.dissco.backend.schema.Agent;
import eu.dissco.backend.schema.Annotation;
import eu.dissco.backend.schema.Annotation.OdsMergingDecisionStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.PatchExchange;
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

  @PatchExchange(value = "/{prefix}/{suffix}")
  Annotation updateAnnotationMergingDecisionStatus(@PathVariable String prefix,
      @PathVariable String suffix,
      @RequestParam OdsMergingDecisionStatus mergingDecisionStatus,
      @RequestBody Agent decisionAgent) throws WebProcessingFailedException;

  @DeleteExchange(value = "/{prefix}/{suffix}")
  void tombstoneAnnotation(@PathVariable String prefix, @PathVariable String suffix,
      @RequestBody AnnotationTombstoneWrapper annotationTombstoneWrapper)
      throws WebProcessingFailedException;
}
