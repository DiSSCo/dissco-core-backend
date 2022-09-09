package eu.dissco.backend.client;


import com.fasterxml.jackson.databind.JsonNode;
import eu.dissco.backend.domain.AnnotationEvent;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "annotations", url = "localhost:8082/")
public interface AnnotationClient {

  @PostMapping(value = "")
  JsonNode postAnnotation(@RequestBody AnnotationEvent event);

}
