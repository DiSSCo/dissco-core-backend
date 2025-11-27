package eu.dissco.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.domain.VirtualCollectionEvent;
import eu.dissco.backend.exceptions.ProcessingFailedException;
import eu.dissco.backend.schema.Agent;
import eu.dissco.backend.schema.VirtualCollection;
import java.util.Objects;
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
  @Value("${rabbitmq.provenance.exchange-name:provenance-exchange}")
  private String provenanceExchange;
  @Value("${rabbitmq.provenance.routing-key:provenance}")
  private String provenanceRoutingKey;
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

  public void publishCreateEvent(Object object, Agent agent)
      throws JsonProcessingException {
    var event = provenanceService.generateCreateEvent(mapper.valueToTree(object), agent);
    log.info("Publishing new create message to queue: {}", event);
    rabbitTemplate.convertAndSend(provenanceExchange, assembleRoutingKey(object), mapper.writeValueAsString(event));
  }

  public void publishUpdateEvent(Object object, Object currentObject, Agent agent)
      throws JsonProcessingException {
    var event = provenanceService.generateUpdateEvent(mapper.valueToTree(object), mapper.valueToTree(currentObject), agent);
    log.info("Publishing new update message to queue: {}", event);
    rabbitTemplate.convertAndSend(provenanceExchange, assembleRoutingKey(object), mapper.writeValueAsString(event));
  }

  public void publishTombstoneEvent(Object tombstoneObject, Object currentObject, Agent agent)
      throws JsonProcessingException {
    var event = provenanceService.generateTombstoneEvent(mapper.valueToTree(tombstoneObject), mapper.valueToTree(currentObject), agent);
    log.info("Publishing new tombstone message to queue: {}", event);
    rabbitTemplate.convertAndSend(provenanceExchange, assembleRoutingKey(tombstoneObject), mapper.writeValueAsString(event));
  }

  public void publishVirtualCollectionEvent(VirtualCollectionEvent event)
      throws JsonProcessingException {
    log.info("Publishing {} virtual-collection to queue", event.action());
    rabbitTemplate.convertAndSend(virtualCollectionExchange, virtualCollectionRoutingKey,
        mapper.writeValueAsString(event));
  }

  private String assembleRoutingKey(Object object) throws ProcessingFailedException {
    if (Objects.requireNonNull(object) instanceof VirtualCollection) {
      return provenanceRoutingKey + ".virtual-collection";
    } else {
      throw new ProcessingFailedException("Unsupported object type for routing key assembly");
    }
  }

}
