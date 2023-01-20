package eu.dissco.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.dissco.backend.client.AnnotationClient;
import eu.dissco.backend.domain.AnnotationEvent;
import eu.dissco.backend.domain.AnnotationRequest;
import eu.dissco.backend.domain.AnnotationResponse;
import eu.dissco.backend.domain.JsonApiLinksFull;
import eu.dissco.backend.domain.JsonApiMeta;
import eu.dissco.backend.domain.JsonApiMetaWrapper;
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

  public List<AnnotationResponse> getAnnotationsForUser(String userId, int pageNumber,
      int pageSize) {
    return repository.getAnnotationsForUser(userId, pageNumber, pageSize);
  }

  public JsonApiMetaWrapper getAnnotationsForUserJsonResponse(String userId, int pageNumber,
      int pageSize, String path) {
    var annotations = repository.getAnnotationsForUserJsonResponse(userId, pageNumber, pageSize);
    int totalPageCount = repository.getAnnotationsCountForUser(userId, pageSize);
    JsonApiMeta metaNode = new JsonApiMeta(totalPageCount);
    JsonApiLinksFull linksNode = buildLinksNode(path, pageNumber, pageSize, totalPageCount);
    return new JsonApiMetaWrapper(annotations, linksNode, metaNode);
  }

  public AnnotationResponse getAnnotation(String id) {
    return repository.getAnnotation(id);
  }

  public List<AnnotationResponse> getAnnotations(int pageNumber, int pageSize) {
    return repository.getAnnotations(pageNumber, pageSize);
  }

  public JsonApiMetaWrapper getAnnotationsJsonResponse(int pageNumber, int pageSize, String path) {
    var annotations = repository.getAnnotationsJsonResponse(pageNumber, pageSize);
    int totalPageCount = repository.getAnnotationsCountGlobal(pageSize);
    JsonApiLinksFull linksNode = buildLinksNode(path, pageNumber, pageSize, totalPageCount);
    JsonApiMeta metaNode = new JsonApiMeta(totalPageCount);
    return new JsonApiMetaWrapper(annotations, linksNode, metaNode);
  }

  public List<AnnotationResponse> getLatestAnnotations(int pageNumber, int pageSize)
      throws IOException {
    return elasticRepository.getLatestAnnotations(pageNumber, pageSize);
  }

  public JsonApiMetaWrapper getLatestAnnotationsJsonResponse(int pageNumber, int pageSize, String path)
      throws IOException {
    var annotations = elasticRepository.getLatestAnnotationsJsonResponse(pageNumber, pageSize);
    int totalPageCount = repository.getAnnotationsCountGlobal(pageSize);
    JsonApiLinksFull linksNode = buildLinksNode(path, pageNumber, pageSize, totalPageCount);
    JsonApiMeta metaNode = new JsonApiMeta(totalPageCount);
    return new JsonApiMetaWrapper(annotations, linksNode, metaNode);
  }

  private JsonApiLinksFull buildLinksNode(String path, int pageNumber, int pageSize,
      int totalPageCount) {
    String pn = "?pageNumber=";
    String ps = "&pageSize=";
    String self = path + pn + pageNumber + ps + pageSize;
    String first = path + pn + "1" + ps + pageSize;
    String last = path + pn + totalPageCount + ps + pageSize;
    String prev = (pageNumber <= 1) ? null
        : path + pn + (pageNumber - 1) + ps + pageSize;
    String next = (pageNumber >= totalPageCount) ? null
        : path + pn + (pageNumber + 1) + ps + pageSize;
    return new JsonApiLinksFull(self, first, last, prev, next);
  }

  public AnnotationResponse persistAnnotation(AnnotationRequest annotation, String userId) {
    var event = mapAnnotationRequestToEvent(annotation, userId);
    var response = annotationClient.postAnnotation(event);
    log.info(response.toString());
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
      throw new NoAnnotationFoundException(
          "No active annotation with id: " + id + " was found for user");
    }
  }


  public AnnotationResponse updateAnnotation(String id, AnnotationRequest annotation, String
      userId)
      throws NoAnnotationFoundException {
    var result = repository.getAnnotationForUser(id, userId);
    if (result > 0) {
      return persistAnnotation(annotation, userId);
    } else {
      log.info("No active annotation with id: {} found for user: {}", id, userId);
      throw new NoAnnotationFoundException(
          "No active annotation with id: " + id + " was found for user");
    }
  }

  public List<AnnotationResponse> getLatestAnnotation() throws IOException {
    return elasticRepository.getLatestAnnotation();
  }

}
