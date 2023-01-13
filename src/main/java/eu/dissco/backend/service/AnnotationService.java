package eu.dissco.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.client.AnnotationClient;
import eu.dissco.backend.domain.AnnotationEvent;
import eu.dissco.backend.domain.AnnotationRequest;
import eu.dissco.backend.domain.AnnotationResponse;
import eu.dissco.backend.domain.JsonApiData;
import eu.dissco.backend.domain.JsonApiLinks;
import eu.dissco.backend.domain.JsonApiWrapper;
import eu.dissco.backend.exceptions.NoAnnotationFoundException;
import eu.dissco.backend.repository.AnnotationRepository;
import eu.dissco.backend.repository.ElasticSearchRepository;
import java.io.IOException;
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
  private final ElasticSearchRepository elasticRepository;
  private final ObjectMapper mapper;

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


  public List<AnnotationResponse> getAnnotationsForUser(String userId, int pageNumber, int pageSize) {
    return repository.getAnnotationsForUser(userId, pageNumber, pageSize);
  }

  public AnnotationResponse getAnnotation(String id) {
    return repository.getAnnotation(id);
  }

  public JsonApiWrapper getAnnotationWithSpeciesName(String id){
    log.info("querying repo");
    JsonApiData dataNode = repository.getAnnotationWithSpeciesName(id);
    log.info("repo queried");
    String annotationLink = "https://hdl.handle.net/" + dataNode.attributes().get("id").asText();
    JsonApiLinks linkNode = new JsonApiLinks(annotationLink);

    return new JsonApiWrapper(dataNode, linkNode);
  }

  public List<AnnotationResponse> getAnnotations(int pageNumber, int pageSize){
    return repository.getAnnotations(pageNumber, pageSize);
  }

  public List<AnnotationResponse> getLatestAnnotations(int pageNumber, int pageSize)
      throws IOException {
    return elasticRepository.getLatestAnnotations(pageNumber, pageSize);
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
        Instant.ofEpochSecond(annotation.get("generated").asLong()),
        null
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

  public boolean deleteAnnotation(String prefix, String postfix, String userId)
      throws NoAnnotationFoundException {
    var id = prefix + "/" + postfix;
    var result = repository.getAnnotationForUser(id, userId);
    if (result > 0) {
      annotationClient.deleteAnnotation(prefix, postfix);
      return true;
    } else {
      log.info("No active annotation with id: {} found for user: {}", id, userId);
      throw new NoAnnotationFoundException("No active annotation with id: " + id + " was found for user");
    }
  }

  public AnnotationResponse updateAnnotation(String id, AnnotationRequest annotation, String userId)
      throws NoAnnotationFoundException {
    var result = repository.getAnnotationForUser(id, userId);
    if (result > 0) {
      return persistAnnotation(annotation, userId);
    } else {
      log.info("No active annotation with id: {} found for user: {}", id, userId);
      throw new NoAnnotationFoundException("No active annotation with id: " + id + " was found for user");
    }
  }

  public List<AnnotationResponse> getLatestAnnotation() throws IOException {
    return elasticRepository.getLatestAnnotation();
  }

}
