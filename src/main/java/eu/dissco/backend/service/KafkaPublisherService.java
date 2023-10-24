package eu.dissco.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.domain.CreateUpdateDeleteEvent;
import eu.dissco.backend.domain.annotation.Annotation;
import java.time.Instant;
import java.util.UUID;
import com.github.fge.jsonpatch.diff.JsonDiff;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaPublisherService {
  private static final String SUBJECT_TYPE = "Annotation";
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper mapper;

  public void sendObjectToQueue(String topicName, Object object) throws JsonProcessingException {
    log.info("Sending to topic: {} with object: {}", topicName, object);
    kafkaTemplate.send(topicName, mapper.writeValueAsString(object));
  }

  public void publishCreateEvent(Annotation annotation) throws JsonProcessingException {
    var event = new CreateUpdateDeleteEvent(UUID.randomUUID(),
        "create",
        "annotation-processing-service",
        annotation.getOdsId(),
        SUBJECT_TYPE,
        Instant.now(),
        mapper.valueToTree(annotation),
        null,
        "Annotation newly created");
    kafkaTemplate.send("createUpdateDeleteTopic", mapper.writeValueAsString(event));
  }

  public void publishUpdateEvent(Annotation currentAnnotation, Annotation annotation)
      throws JsonProcessingException {
    var jsonPatch = createJsonPatch(currentAnnotation, annotation);
    var event = new CreateUpdateDeleteEvent(UUID.randomUUID(),
        "update",
        "annotation-processing-service",
        annotation.getOdsId(),
        SUBJECT_TYPE,
        Instant.now(),
        mapper.valueToTree(annotation), jsonPatch, "Annotation has been updated");
    kafkaTemplate.send("createUpdateDeleteTopic", mapper.writeValueAsString(event));
  }

  private JsonNode createJsonPatch(Annotation currentAnnotation, Annotation annotation) {
    return JsonDiff.asJson(mapper.valueToTree(currentAnnotation), mapper.valueToTree(annotation));
  }


}
