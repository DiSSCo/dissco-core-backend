package eu.dissco.backend.service;

import static eu.dissco.backend.TestUtils.HANDLE;
import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.givenAgent;
import static eu.dissco.backend.TestUtils.givenDigitalSpecimenWrapper;
import static eu.dissco.backend.utils.VirtualCollectionUtils.givenTombstoneVirtualCollection;
import static eu.dissco.backend.utils.VirtualCollectionUtils.givenVirtualCollection;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.dissco.backend.domain.VirtualCollectionAction;
import eu.dissco.backend.domain.VirtualCollectionEvent;
import eu.dissco.backend.schema.CreateUpdateTombstoneEvent;
import eu.dissco.backend.schema.DigitalSpecimen;
import java.io.IOException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.containers.RabbitMQContainer;

@ExtendWith(MockitoExtension.class)
class RabbitMqPublisherServiceTest {

  private static final String ROUTING_KEY = "ABC-123-XYZ";
  private static final String CREATE_UPDATE_TOMBSTONE = "create-update-tombstone";
  private static final String VIRTUAL_COLLECTION = "virtual-collection";

  private static RabbitMQContainer container;
  private static RabbitTemplate rabbitTemplate;
  private RabbitMqPublisherService rabbitMqPublisherService;
  @Mock
  private ProvenanceService provenanceService;

  @BeforeAll
  static void setupContainer() throws IOException, InterruptedException {
    container = new RabbitMQContainer("rabbitmq:4.0.8-management-alpine");
    container.start();
    // Declare the default mas exchange and the specific mas queue and binding
    declareRabbitResources("mas-exchange", ROUTING_KEY + "-queue",
        ROUTING_KEY);
    declareRabbitResources(CREATE_UPDATE_TOMBSTONE + "-exchange",
        CREATE_UPDATE_TOMBSTONE + "-queue",
        CREATE_UPDATE_TOMBSTONE);
    declareRabbitResources(VIRTUAL_COLLECTION + "-exchange",
        VIRTUAL_COLLECTION + "-queue",
        VIRTUAL_COLLECTION);

    CachingConnectionFactory factory = new CachingConnectionFactory(container.getHost());
    factory.setPort(container.getAmqpPort());
    factory.setUsername(container.getAdminUsername());
    factory.setPassword(container.getAdminPassword());
    rabbitTemplate = new RabbitTemplate(factory);
    rabbitTemplate.setReceiveTimeout(100L);
  }

  private static void declareRabbitResources(String exchangeName, String queueName,
      String routingKey)
      throws IOException, InterruptedException {
    container.execInContainer("rabbitmqadmin", "declare", "exchange", "name=" + exchangeName,
        "type=direct", "durable=true");
    container.execInContainer("rabbitmqadmin", "declare", "queue", "name=" + queueName,
        "queue_type=quorum", "durable=true");
    container.execInContainer("rabbitmqadmin", "declare", "binding", "source=" + exchangeName,
        "destination_type=queue", "destination=" + queueName, "routing_key=" + routingKey);
  }

  @AfterAll
  static void shutdownContainer() {
    container.stop();
  }

  @BeforeEach
  void setup() {
    rabbitMqPublisherService = new RabbitMqPublisherService(MAPPER, rabbitTemplate,
        provenanceService);
    ReflectionTestUtils.setField(rabbitMqPublisherService, "cutExchange",
        CREATE_UPDATE_TOMBSTONE + "-exchange");
    ReflectionTestUtils.setField(rabbitMqPublisherService, "cutRoutingKey",
        CREATE_UPDATE_TOMBSTONE);
    ReflectionTestUtils.setField(rabbitMqPublisherService, "virtualCollectionExchange",
        VIRTUAL_COLLECTION + "-exchange");
    ReflectionTestUtils.setField(rabbitMqPublisherService, "virtualCollectionRoutingKey",
        VIRTUAL_COLLECTION);
  }

  @Test
  void testSendObjectToQueue() throws JsonProcessingException {
    // Given
    var digitalSpecimen = givenDigitalSpecimenWrapper(ID);

    // When
    rabbitMqPublisherService.publishMasRequestEvent(ROUTING_KEY, digitalSpecimen);

    // Then
    var result = rabbitTemplate.receive(ROUTING_KEY + "-queue");
    assertThat(MAPPER.readValue(new String(result.getBody()), DigitalSpecimen.class)).isEqualTo(
        digitalSpecimen);
  }

  @Test
  void testPublishCreateEvent() throws JsonProcessingException {
    // Given
    var virtualCollection = givenVirtualCollection(HANDLE + ID);
    var jsonNode = MAPPER.valueToTree(virtualCollection);
    given(provenanceService.generateCreateEvent(jsonNode, givenAgent())).willReturn(
        new CreateUpdateTombstoneEvent().withId(ID));

    // When
    rabbitMqPublisherService.publishCreateEvent(jsonNode, givenAgent());

    // Then
    var result = rabbitTemplate.receive("create-update-tombstone-queue");
    assertThat(MAPPER.readValue(new String(result.getBody()),
        CreateUpdateTombstoneEvent.class).getId()).isEqualTo(ID);
  }

  @Test
  void testPublishUpdateEvent() throws JsonProcessingException {
    // Given
    var virtualCollection = MAPPER.valueToTree(givenVirtualCollection(HANDLE + ID));
    var unequalVirtualCollection = MAPPER.createObjectNode();
    given(provenanceService.generateUpdateEvent(virtualCollection, unequalVirtualCollection,
        givenAgent()))
        .willReturn(new CreateUpdateTombstoneEvent().withId(ID));

    // When
    rabbitMqPublisherService.publishUpdateEvent(virtualCollection, unequalVirtualCollection,
        givenAgent());

    // Then
    var result = rabbitTemplate.receive("create-update-tombstone-queue");
    assertThat(MAPPER.readValue(new String(result.getBody()),
        CreateUpdateTombstoneEvent.class).getId()).isEqualTo(ID);
  }

  @Test
  void testPublishTombstoneEvent() throws Exception {
    // Given
    var tombstoneVirtualCollection = MAPPER.valueToTree(givenTombstoneVirtualCollection());
    var virtualCollection = MAPPER.valueToTree(givenVirtualCollection(HANDLE + ID));
    given(provenanceService.generateTombstoneEvent(tombstoneVirtualCollection, virtualCollection,
        givenAgent())).willReturn(new
        CreateUpdateTombstoneEvent().withId(ID));

    // When
    rabbitMqPublisherService.publishTombstoneEvent(tombstoneVirtualCollection, virtualCollection,
        givenAgent());

    // Then
    var result = rabbitTemplate.receive("create-update-tombstone-queue");
    assertThat(MAPPER.readValue(new String(result.getBody()),
        CreateUpdateTombstoneEvent.class).getId()).isEqualTo(ID);
  }

  @Test
  void testPublishVirtualCollectionEvent() throws JsonProcessingException {
    // Given
    var virtualCollection = givenVirtualCollection(HANDLE + ID);
    var virtualCollectionEvent = new VirtualCollectionEvent(
        VirtualCollectionAction.CREATE, virtualCollection);

    // When
    rabbitMqPublisherService.publishVirtualCollectionEvent(virtualCollectionEvent);

    // Then
    var result = rabbitTemplate.receive(VIRTUAL_COLLECTION + "-queue");
    assertThat(MAPPER.readValue(new String(result.getBody()),
        VirtualCollectionEvent.class)).isEqualTo(virtualCollectionEvent);
  }
}
