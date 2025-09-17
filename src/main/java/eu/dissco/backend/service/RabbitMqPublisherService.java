package eu.dissco.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.domain.VirtualCollectionEvent;
import eu.dissco.backend.schema.Agent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMqPublisherService {

  private final ObjectMapper mapper;
  private final RabbitTemplate rabbitTemplate;
  private final ProvenanceService provenanceService;
  @Value("${rabbitmq.mas-exchange-name:mas-exchange}")
  private String masExchangeName = "mas-exchange";
  @Value("${rabbitmq.create-update-tombstone.exchange-name:create-update-tombstone-exchange}")
  private String cutExchange;
  @Value("${rabbitmq.create-update-tombstone.routing-key:create-update-tombstone}")
  private String cutRoutingKey;
  @Value(value = "${rabbitmq.virtual-collection.exchange-name:virtual-collection-exchange}")
  private String virtualCollectionExchange;
  @Value("${rabbitmq.virtual-collection.routing-key:virtual-collection}")
  private String virtualCollectionRoutingKey;

  public void publishMasRequestEvent(String routingKey, Object object)
      throws JsonProcessingException {
    log.debug("Publishing new mas request with routing key: {} and with object: {}", routingKey,
        object);
    rabbitTemplate.convertAndSend(masExchangeName, routingKey, mapper.writeValueAsString(object));
  }

  public void publishCreateEvent(JsonNode object, Agent agent)
      throws JsonProcessingException {
    var event = provenanceService.generateCreateEvent(object, agent);
    log.info("Publishing new create message to queue: {}", event);
    rabbitTemplate.convertAndSend(cutExchange, cutRoutingKey, mapper.writeValueAsString(event));
  }

  public void publishUpdateEvent(JsonNode object, JsonNode currentObject, Agent agent)
      throws JsonProcessingException {
    var event = provenanceService.generateUpdateEvent(object, currentObject, agent);
    log.info("Publishing new update message to queue: {}", event);
    rabbitTemplate.convertAndSend(cutExchange, cutRoutingKey, mapper.writeValueAsString(event));
  }

  public void publishTombstoneEvent(JsonNode tombstoneObject, JsonNode currentObject, Agent agent)
      throws JsonProcessingException {
    var event = provenanceService.generateTombstoneEvent(tombstoneObject, currentObject, agent);
    log.info("Publishing new tombstone message to queue: {}", event);
    rabbitTemplate.convertAndSend(cutExchange, cutRoutingKey, mapper.writeValueAsString(event));
  }

  public void publishVirtualCollectionEvent(VirtualCollectionEvent event)
      throws JsonProcessingException {
    log.info("Publishing {} virtual-collection to queue", event.action());
    rabbitTemplate.convertAndSend(virtualCollectionExchange, virtualCollectionRoutingKey,
        mapper.writeValueAsString(event));
  }

}
