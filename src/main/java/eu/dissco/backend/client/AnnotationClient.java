package eu.dissco.backend.client;


import com.fasterxml.jackson.databind.JsonNode;
import eu.dissco.backend.domain.annotation.Annotation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "annotations", url = "${feign.annotations}")
public interface AnnotationClient {

  @PostMapping(value = "")
  JsonNode postAnnotation(@RequestBody Annotation annotation);

  @PatchMapping(value = "{prefix}/{suffix}")
  JsonNode updateAnnotation(@PathVariable("prefix") String prefix,
      @PathVariable("suffix") String suffix, @RequestBody Annotation annotation);

  @DeleteMapping(value = "/{prefix}/{suffix}")
  void deleteAnnotation(@PathVariable("prefix") String prefix,
      @PathVariable("suffix") String suffix);
}
