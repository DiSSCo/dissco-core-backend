package eu.dissco.backend.service;

import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.givenDigitalSpecimen;
import static org.mockito.BDDMockito.then;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.dissco.backend.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

@ExtendWith(MockitoExtension.class)
class KafkaPublisherServiceTest {

  @Mock
  private KafkaTemplate<String, String> template;

  private KafkaPublisherService service;

  @BeforeEach
  void setup() {
    service = new KafkaPublisherService(template, TestUtils.MAPPER);
  }

  @Test
  void testSendObjectToQueue() throws JsonProcessingException {
    // Given
    var digitalSpecimen = givenDigitalSpecimen(ID);
    var topicName = "fancyTopic";

    // When
    service.sendObjectToQueue(topicName, digitalSpecimen);

    // Then
    then(template).should().send(topicName, MAPPER.writeValueAsString(digitalSpecimen));
  }

}
