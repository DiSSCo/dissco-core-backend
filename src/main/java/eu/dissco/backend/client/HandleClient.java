package eu.dissco.backend.client;

import eu.dissco.backend.exceptions.WebProcessingFailedException;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.PatchExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;
import tools.jackson.databind.JsonNode;

public interface HandleClient {

  @PostExchange("")
  JsonNode postHandle(@RequestBody JsonNode handleRequest) throws WebProcessingFailedException;

  @PostExchange("batch")
  JsonNode postHandles(@RequestBody List<JsonNode> handleRequest)
      throws WebProcessingFailedException;

  @PatchExchange("{pid}")
  void updateHandle(@RequestBody JsonNode handleRequest) throws WebProcessingFailedException;

  @PutExchange("{pid}")
  void tombstoneHandle(@PathVariable String pid, @RequestBody JsonNode handleRequest)
      throws WebProcessingFailedException;

  @DeleteExchange("rollback/create")
  void rollbackHandle(@RequestBody JsonNode handleRequest) throws WebProcessingFailedException;


}
