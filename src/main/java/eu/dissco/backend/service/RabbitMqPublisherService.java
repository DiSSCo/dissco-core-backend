package eu.dissco.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.domain.VirtualCollectionEvent;
import eu.dissco.backend.exceptions.ProcessingFailedException;
import eu.dissco.backend.properties.RabbitMqProperties;
import eu.dissco.backend.schema.Agent;
import eu.dissco.backend.schema.VirtualCollection;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMqPublisherService {

  private static final String SERIALISATION_ERROR = "Failed to serialize tombstone event: {}";

  private final ObjectMapper mapper;
  private final RabbitTemplate rabbitTemplate;
  private final ProvenanceService provenanceService;
  private final RabbitMqProperties rabbitMqProperties;


  public void publishMasRequestEvent(String routingKey, Object object)
      throws JsonProcessingException {
    log.debug("Publishing new mas request with routing key: {} and with object: {}", routingKey,
        object);
    rabbitTemplate.convertAndSend(rabbitMqProperties.getMasExchangeName(), routingKey,
        mapper.writeValueAsString(object));
  }

  public void publishCreateEvent(Object object, Agent agent)
      throws ProcessingFailedException {
    var event = provenanceService.generateCreateEvent(mapper.valueToTree(object), agent);
    log.info("Publishing new create message to queue: {}", event);
    try {
      rabbitTemplate.convertAndSend(rabbitMqProperties.getProvenanceExchange(),
          assembleRoutingKey(object), mapper.writeValueAsString(event));
    } catch (JsonProcessingException e) {
      log.error(SERIALISATION_ERROR, event, e);
      throw new ProcessingFailedException("Failed to serialize tombstone event", e);
    }
  }

  public void publishUpdateEvent(Object object, Object currentObject, Agent agent)
      throws ProcessingFailedException {
    var event = provenanceService.generateUpdateEvent(mapper.valueToTree(object),
        mapper.valueToTree(currentObject), agent);
    log.info("Publishing new update message to queue: {}", event);
    try {
      rabbitTemplate.convertAndSend(rabbitMqProperties.getProvenanceExchange(),
          assembleRoutingKey(object), mapper.writeValueAsString(event));
    } catch (JsonProcessingException e) {
      log.error(SERIALISATION_ERROR, event, e);
      throw new ProcessingFailedException("Failed to serialize tombstone event", e);
    }
  }

  public void publishTombstoneEvent(Object tombstoneObject, Object currentObject, Agent agent)
      throws ProcessingFailedException {
    var event = provenanceService.generateTombstoneEvent(mapper.valueToTree(tombstoneObject),
        mapper.valueToTree(currentObject), agent);
    log.info("Publishing new tombstone message to queue: {}", event);
    try {
      rabbitTemplate.convertAndSend(rabbitMqProperties.getProvenanceExchange(),
          assembleRoutingKey(tombstoneObject), mapper.writeValueAsString(event));
    } catch (JsonProcessingException e) {
      log.error(SERIALISATION_ERROR, event, e);
      throw new ProcessingFailedException("Failed to serialize tombstone event", e);
    }
  }

  public void publishVirtualCollectionEvent(VirtualCollectionEvent event)
      throws JsonProcessingException {
    log.info("Publishing {} virtual-collection to queue", event.action());
    rabbitTemplate.convertAndSend(rabbitMqProperties.getVirtualCollectionExchange(),
        rabbitMqProperties.getVirtualCollectionRoutingKey(),
        mapper.writeValueAsString(event));
  }

  private String assembleRoutingKey(Object object) throws ProcessingFailedException {
    if (Objects.requireNonNull(object) instanceof VirtualCollection) {
      return rabbitMqProperties.getProvenanceRoutingPrefix() + ".virtual-collection";
    } else {
      throw new ProcessingFailedException("Unsupported object type for routing key assembly");
    }
  }

}
