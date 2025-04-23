package eu.dissco.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMqPublisherService {

  @Value("${rabbitmq.mas-exchange-name:mas-exchange}")
  private String exchangeName = "mas-exchange";

  private final ObjectMapper mapper;
  private final RabbitTemplate rabbitTemplate;

  public void sendObjectToQueue(String routingKey, Object object) throws JsonProcessingException {
    log.debug("Sending to exchange with routing key: {} and with object: {}", routingKey, object);
    rabbitTemplate.convertAndSend(exchangeName, routingKey, mapper.writeValueAsString(object));
  }

}
