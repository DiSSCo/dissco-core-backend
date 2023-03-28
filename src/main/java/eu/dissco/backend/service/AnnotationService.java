package eu.dissco.backend.service;

import static eu.dissco.backend.service.ServiceUtils.createVersionNode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.client.AnnotationClient;
import eu.dissco.backend.domain.AnnotationEvent;
import eu.dissco.backend.domain.AnnotationRequest;
import eu.dissco.backend.domain.AnnotationResponse;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.domain.jsonapi.JsonApiLinks;
import eu.dissco.backend.domain.jsonapi.JsonApiLinksFull;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import eu.dissco.backend.exceptions.NoAnnotationFoundException;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.repository.AnnotationRepository;
import eu.dissco.backend.repository.ElasticSearchRepository;
import eu.dissco.backend.repository.MongoRepository;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
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
  private final ObjectMapper mapper;

  // Used by Controller

  public JsonApiWrapper getAnnotation(String id, String path) {
    var annotation = repository.getAnnotation(id);
    var dataNode = new JsonApiData(id, annotation.type(), annotation, mapper);
    return new JsonApiWrapper(dataNode, new JsonApiLinks(path));
  }

  public JsonApiListResponseWrapper getLatestAnnotations(int pageNumber, int pageSize,
      String path) throws IOException {
    var annotationsPlusOne = elasticRepository.getLatestAnnotations(pageNumber,
        pageSize + 1);
    return wrapListResponse(annotationsPlusOne, pageNumber, pageSize, path);
  }

  public JsonApiWrapper getAnnotationByVersion(String id, int version, String path)
      throws NotFoundException, JsonProcessingException {
    var annotationNode = mongoRepository.getByVersion(id, version, "annotation_provenance").get("annotation");
    validateAnnotationNode(annotationNode);
    var type = annotationNode.get("type").asText();
    var dataNode = new JsonApiData(id, type, annotationNode);
    return new JsonApiWrapper(dataNode, new JsonApiLinks(path));
  }

  public JsonApiListResponseWrapper getAnnotations(int pageNumber, int pageSize,
      String path) {
    var annotationsPlusOne = repository.getAnnotations(pageNumber, pageSize + 1);
    return wrapListResponse(annotationsPlusOne, pageNumber, pageSize, path);
  }

  public JsonApiWrapper persistAnnotation(AnnotationRequest annotationRequest, String userId,
      String path) throws JsonProcessingException {
    var event = mapAnnotationRequestToEvent(annotationRequest, userId);
    var response = annotationClient.postAnnotation(event);
    if (response != null) {
      var annotationResponse = mapper.treeToValue(response.get("annotation"), AnnotationResponse.class);
      var dataNode = new JsonApiData(annotationResponse.id(), annotationResponse.type(), annotationResponse, mapper);
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

  public JsonApiWrapper updateAnnotation(String id, AnnotationRequest annotation, String userId,
      String path)
      throws NoAnnotationFoundException, JsonProcessingException {
    var result = repository.getAnnotationForUser(id, userId);
    if (result > 0) {
      return persistAnnotation(annotation, userId, path);
    } else {
      log.info("No active annotation with id: {} found for user: {}", id, userId);
      throw new NoAnnotationFoundException(
          "No active annotation with id: " + id + " was found for user");
    }
  }

  public JsonApiListResponseWrapper getAnnotationsForUser(String userId, int pageNumber,
      int pageSize, String path) {
    var annotationsPlusOne = repository.getAnnotationsForUser(userId, pageNumber,
        pageSize + 1);

    return wrapListResponse(annotationsPlusOne, pageNumber, pageSize, path);
  }
  public JsonApiWrapper getAnnotationVersions(String id, String path) throws NotFoundException {
    var versions = mongoRepository.getVersions(id, "annotation_provenance");
    var versionsNode = createVersionNode(versions, mapper);
    var dataNode = new JsonApiData(id, "annotationVersions", versionsNode);
    return new JsonApiWrapper(dataNode, new JsonApiLinks(path));
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

  // Used by other services

  public List<AnnotationResponse> getAnnotationForTargetObject(String id) {
    var fullId = "https://hdl.handle.net/" + id;
    return repository.getForTarget(fullId);
  }

  public JsonApiListResponseWrapper getAnnotationForTarget(String id, String path) {
    var fullId = "https://hdl.handle.net/" + id;
    var annotations = repository.getForTarget(fullId);
    return wrapListResponse(annotations, path);
  }

  // Response Constructors
  private JsonApiListResponseWrapper wrapListResponse(List<AnnotationResponse> annotationsPlusOne, int pageNumber, int pageSize, String path){
    List<JsonApiData> dataNodePlusOne = new ArrayList<>();
    annotationsPlusOne.forEach(annotation -> dataNodePlusOne.add(
        new JsonApiData(annotation.id(), annotation.type(), annotation, mapper)));
    return new JsonApiListResponseWrapper(dataNodePlusOne, pageNumber, pageSize, path);
  }

  private void validateAnnotationNode(JsonNode annotationNode) throws JsonProcessingException {
    mapper.treeToValue(annotationNode, AnnotationResponse.class);
  }

  private JsonApiListResponseWrapper wrapListResponse(List<AnnotationResponse> annotations, String path){
    List<JsonApiData> dataNode = new ArrayList<>();
    annotations.forEach(annotation -> dataNode.add(
        new JsonApiData(annotation.id(), annotation.type(), annotation, mapper)));
    return new JsonApiListResponseWrapper(dataNode, new JsonApiLinksFull(path));
  }
}
