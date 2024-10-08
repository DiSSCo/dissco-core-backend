package eu.dissco.backend.client;


import com.fasterxml.jackson.databind.JsonNode;
import eu.dissco.backend.domain.annotation.AnnotationTombstoneWrapper;
import eu.dissco.backend.domain.annotation.batch.AnnotationEvent;
import eu.dissco.backend.schema.Annotation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "annotations", url = "${feign.annotations}")
public interface AnnotationClient {

  @PostMapping(value = "")
  JsonNode postAnnotation(@RequestBody Annotation annotation);

  @PostMapping(value = "/batch")
  JsonNode postAnnotationBatch(@RequestBody AnnotationEvent event);

  @PutMapping(value = "/{prefix}/{suffix}")
  JsonNode updateAnnotation(@PathVariable("prefix") String prefix,
      @PathVariable("suffix") String suffix, @RequestBody Annotation annotation);

  @DeleteMapping(value = "/{prefix}/{suffix}")
  void tombstoneAnnotation(@PathVariable("prefix") String prefix,
      @PathVariable("suffix") String suffix, @RequestBody AnnotationTombstoneWrapper annotationTombstoneWrapper);
}
