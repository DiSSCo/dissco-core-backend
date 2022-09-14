package eu.dissco.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import eu.dissco.backend.client.AnnotationClient;
import eu.dissco.backend.domain.AnnotationEvent;
import eu.dissco.backend.domain.AnnotationRequest;
import eu.dissco.backend.domain.AnnotationResponse;
import eu.dissco.backend.repository.AnnotationRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnnotationService {

  private final AnnotationRepository repository;
  private final AnnotationClient annotationClient;

  @NotNull
  private static AnnotationEvent mapAnnotationRequestToEvent(AnnotationRequest annotation,
      String userId) {
    return new AnnotationEvent(
        annotation.type(),
        annotation.motivation(),
        userId,
        Instant.now(),
        annotation.target(),
        annotation.body()
    );
  }

  public List<AnnotationResponse> getAnnotationsForUser(String userId) {
    return repository.getAnnotationsForUser(userId);
  }

  public AnnotationResponse getAnnotation(String id) {
    return repository.getAnnotation(id);
  }

  public AnnotationResponse persistAnnotation(AnnotationRequest annotation, String userId) {
    var event = mapAnnotationRequestToEvent(annotation, userId);
    var response = annotationClient.postAnnotation(event);
    if (response != null) {
      return mapResponseToAnnotationResponse(response);
    }
    return null;
  }

  private AnnotationResponse mapResponseToAnnotationResponse(JsonNode response) {
    var annotation = response.get("annotation");
    return new AnnotationResponse(
        response.get("id").asText(),
        response.get("version").asInt(),
        annotation.get("type").asText(),
        annotation.get("motivation").asText(),
        annotation.get("target"),
        annotation.get("body"),
        annotation.get("preferenceScore").asInt(),
        annotation.get("creator").asText(),
        Instant.ofEpochSecond(annotation.get("created").asLong()),
        annotation.get("generator"),
        Instant.ofEpochSecond(annotation.get("generated").asLong())
    );
  }

  public AnnotationResponse getAnnotationVersion(String id, int version) {
    return repository.getAnnotationVersion(id, version);
  }

  public List<AnnotationResponse> getAnnotationForTarget(String id) {
    var fullId = "https://hdl.handle.net/" + id;
    return repository.getForTarget(fullId);
  }

  public List<Integer> getAnnotationVersions(String id) {
    return repository.getAnnotationVersions(id);
  }

  public boolean deleteAnnotation(String prefix, String postfix, String userId) {
    var id = prefix + "/" + postfix;
    var result = repository.getAnnotationForUser(id, userId);
    if (result > 0) {
      annotationClient.deleteAnnotation(prefix, postfix);
      return true;
    } else {
      log.info("No active annotation with id: {} found for user: {}", id, userId);
      return false;
    }
  }
}
