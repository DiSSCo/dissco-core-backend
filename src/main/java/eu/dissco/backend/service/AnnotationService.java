package eu.dissco.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import eu.dissco.backend.client.AnnotationClient;
import eu.dissco.backend.domain.AnnotationEvent;
import eu.dissco.backend.domain.AnnotationRequest;
import eu.dissco.backend.domain.AnnotationResponse;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.domain.jsonapi.JsonApiLinks;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import eu.dissco.backend.exceptions.NoAnnotationFoundException;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.repository.AnnotationRepository;
import eu.dissco.backend.repository.ElasticSearchRepository;
import eu.dissco.backend.repository.MongoRepository;
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

  private static final String MONGO_DATE_FIELD = "$date";

  private final AnnotationRepository repository;
  private final AnnotationClient annotationClient;
  private final ElasticSearchRepository elasticRepository;
  private final MongoRepository mongoRepository;

  public JsonApiWrapper getAnnotation(String id, String path){
    var dataNode = repository.getAnnotation(id);
    return new JsonApiWrapper(dataNode, new JsonApiLinks(path));
  }

  public JsonApiListResponseWrapper getLatestAnnotations(int pageNumber, int pageSize,
      String path) throws IOException {
    var annotationsPlusOne = elasticRepository.getLatestAnnotations(pageNumber,
        pageSize + 1);
    return new JsonApiListResponseWrapper(annotationsPlusOne, pageNumber, pageSize, path);
  }

  public JsonApiListResponseWrapper getAnnotationsForUser(String userId, int pageNumber,
      int pageSize, String path) {
    var annotationsPlusOne = repository.getAnnotationsForUser(userId, pageNumber,
        pageSize + 1);
    return new JsonApiListResponseWrapper(annotationsPlusOne, pageNumber, pageSize, path);
  }

  public JsonApiListResponseWrapper getAnnotations(int pageNumber, int pageSize,
      String path) {
    var annotationsPlusOne = repository.getAnnotations(pageNumber, pageSize + 1);
   return new JsonApiListResponseWrapper(annotationsPlusOne, pageNumber, pageSize, path);
  }

  public JsonApiWrapper persistAnnotation(AnnotationRequest annotation, String userId, String path) {
    var event = mapAnnotationRequestToEvent(annotation, userId);
    var response = annotationClient.postAnnotation(event);
    if (response != null) {
      response = response.get("annotation");
      var type = response.get("type").asText();
      var id = response.get("id").asText();
      var dataNode = new JsonApiData(id, type, response);
      return new JsonApiWrapper(dataNode, new JsonApiLinks(path));
    }
    return null;
  }

  @NotNull
  private static AnnotationEvent mapAnnotationRequestToEvent(AnnotationRequest annotation,
      String userId) {
    return new AnnotationEvent(annotation.type(), annotation.motivation(), userId, Instant.now(),
        annotation.target(), annotation.body());
  }

  public JsonApiWrapper updateAnnotation(String id, AnnotationRequest annotation, String userId, String path)
      throws NoAnnotationFoundException {
    var result = repository.getAnnotationForUser(id, userId);
    if (result > 0) {
      return persistAnnotation(annotation, userId, path);
    } else {
      log.info("No active annotation with id: {} found for user: {}", id, userId);
      throw new NoAnnotationFoundException(
          "No active annotation with id: " + id + " was found for user");
    }
  }

  public JsonApiWrapper getAnnotationByVersion(String id, int version, String path)
      throws NotFoundException, JsonProcessingException {
    var annotation = mongoRepository.getByVersion(id, version, "annotation_provenance");
    var type = annotation.get("annotation").get("type").asText();
    var dataNode = new JsonApiData(id, type, annotation);
    return new JsonApiWrapper(dataNode, new JsonApiLinks(path));
  }

  public List<AnnotationResponse> getAnnotationForTarget(String id) {
    var fullId = "https://hdl.handle.net/" + id;
    return repository.getForTarget(fullId);
  }

  public List<Integer> getAnnotationVersions(String id) throws NotFoundException {
    return mongoRepository.getVersions(id, "annotation_provenance");
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

  public List<AnnotationResponse> getLatestAnnotation() throws IOException {
    return elasticRepository.getLatestAnnotation();
  }

}
