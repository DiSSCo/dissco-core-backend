package eu.dissco.backend.service;

import static eu.dissco.backend.service.ServiceUtils.createVersionNode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.dissco.backend.client.AnnotationClient;
import eu.dissco.backend.domain.AnnotationEvent;
import eu.dissco.backend.domain.AnnotationRequest;
import eu.dissco.backend.domain.AnnotationResponse;
import eu.dissco.backend.domain.User;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.domain.jsonapi.JsonApiLinks;
import eu.dissco.backend.domain.jsonapi.JsonApiLinksFull;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiMeta;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import eu.dissco.backend.exceptions.ForbiddenException;
import eu.dissco.backend.exceptions.NoAnnotationFoundException;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.repository.AnnotationRepository;
import eu.dissco.backend.repository.ElasticSearchRepository;
import eu.dissco.backend.repository.MongoRepository;
import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnnotationService {

  private static final String ANNOTATION = "annotation";
  private static final String VERSION = "version";

  private final AnnotationRepository repository;
  private final AnnotationClient annotationClient;
  private final ElasticSearchRepository elasticRepository;
  private final MongoRepository mongoRepository;
  private final UserService userService;
  private final ObjectMapper mapper;
  private final DateTimeFormatter formatter;

  // Used by Controller

  private static AnnotationEvent mapAnnotationRequestToEvent(AnnotationRequest annotation,
      String userId) {
    return new AnnotationEvent(annotation.type(), annotation.motivation(), userId, Instant.now(),
        annotation.target(), annotation.body());
  }

  public JsonApiWrapper getAnnotation(String id, String path) {
    var annotation = repository.getAnnotation(id);
    var dataNode = new JsonApiData(id, annotation.type(), annotation, mapper);
    return new JsonApiWrapper(dataNode, new JsonApiLinks(path));
  }

  public JsonApiListResponseWrapper getLatestAnnotations(int pageNumber, int pageSize,
      String path) throws IOException {
    var annotationsPlusOne = elasticRepository.getLatestAnnotations(pageNumber, pageSize);
    return wrapListResponse(annotationsPlusOne, pageNumber, pageSize, path);
  }

  public JsonApiWrapper getAnnotationByVersion(String id, int version, String path)
      throws NotFoundException, JsonProcessingException {
    var eventNode = mongoRepository.getByVersion(id, version, "annotation_provenance");
    var annotationNode = (ObjectNode) eventNode.get(ANNOTATION);
    annotationNode.set(VERSION, eventNode.get(VERSION));
    validateAnnotationNode(annotationNode);
    var type = annotationNode.get("type").asText();
    var dataNode = new JsonApiData(id, type, annotationNode);
    return new JsonApiWrapper(dataNode, new JsonApiLinks(path));
  }

  public JsonApiListResponseWrapper getAnnotations(int pageNumber, int pageSize,
      String path) {
    var annotationsPlusOne = repository.getAnnotations(pageNumber, pageSize);
    return wrapListResponse(annotationsPlusOne, pageNumber, pageSize, path);
  }

  public JsonApiWrapper persistAnnotation(AnnotationRequest annotationRequest, String userId,
      String path) throws ForbiddenException {
    var user = getUserInformation(userId);
    var event = mapAnnotationRequestToEvent(annotationRequest, user.orcid());
    var response = annotationClient.postAnnotation(event);
    if (response != null) {
      AnnotationResponse annotationResponse = parseToAnnotationResponse(response);
      var dataNode = new JsonApiData(annotationResponse.id(), annotationResponse.type(),
          annotationResponse, mapper);
      return new JsonApiWrapper(dataNode, new JsonApiLinks(path));
    }
    return null;
  }

  private User getUserInformation(String userId) throws ForbiddenException {
    var user = userService.getUser(userId);
    if (user.orcid() == null){
      throw new ForbiddenException("No ORCID is provided");
    }
    return user;
  }

  private AnnotationResponse parseToAnnotationResponse(JsonNode response) {
    return new AnnotationResponse(
        response.get("id").asText(),
        response.get(VERSION).asInt(),
        response.get(ANNOTATION).get("type").asText(),
        response.get(ANNOTATION).get("motivation").asText(),
        response.get(ANNOTATION).get("target"),
        response.get(ANNOTATION).get("body"),
        response.get(ANNOTATION).get("preferenceScore").asInt(),
        response.get(ANNOTATION).get("creator").asText(),
        Instant.from(formatter.parse(response.get(ANNOTATION).get("created").asText())),
        response.get(ANNOTATION).get("generator"),
        Instant.from(formatter.parse(response.get(ANNOTATION).get("generated").asText())),
        null
    );
  }

  public JsonApiWrapper updateAnnotation(String id, AnnotationRequest annotation, String userId,
      String path) throws NoAnnotationFoundException, ForbiddenException {
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
      int pageSize, String path) throws IOException {
    var elasticSearchResults = elasticRepository.getAnnotationsForCreator(userId, pageNumber,
        pageSize);
    return wrapListResponseElasticSearchResults(elasticSearchResults, pageNumber, pageSize, path);
  }

  public JsonApiWrapper getAnnotationVersions(String id, String path) throws NotFoundException {
    var versions = mongoRepository.getVersions(id, "annotation_provenance");
    var versionsNode = createVersionNode(versions, mapper);
    var dataNode = new JsonApiData(id, "annotationVersions", versionsNode);
    return new JsonApiWrapper(dataNode, new JsonApiLinks(path));
  }

  public boolean deleteAnnotation(String prefix, String suffix, String userId)
      throws NoAnnotationFoundException {
    var id = prefix + "/" + suffix;
    var result = repository.getAnnotationForUser(id, userId);
    if (result > 0) {
      annotationClient.deleteAnnotation(prefix, suffix);
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
  private void validateAnnotationNode(JsonNode annotationNode) throws JsonProcessingException {
    mapper.treeToValue(annotationNode, AnnotationResponse.class);
  }

  private JsonApiListResponseWrapper wrapListResponse(List<AnnotationResponse> annotationsPlusOne,
      int pageNumber, int pageSize, String path) {
    List<JsonApiData> dataNodePlusOne = new ArrayList<>();
    annotationsPlusOne.forEach(annotation -> dataNodePlusOne.add(
        new JsonApiData(annotation.id(), annotation.type(), annotation, mapper)));
    return new JsonApiListResponseWrapper(dataNodePlusOne, pageNumber, pageSize, path);
  }

  private JsonApiListResponseWrapper wrapListResponseElasticSearchResults(
      Pair<Long, List<AnnotationResponse>> elasticSearchResults, int pageNumber, int pageSize,
      String path) {
    List<JsonApiData> dataNodePlusOne = new ArrayList<>();
    var annotationsPlusOne = elasticSearchResults.getRight();
    annotationsPlusOne.forEach(annotation -> dataNodePlusOne.add(
        new JsonApiData(annotation.id(), annotation.type(), annotation, mapper)));
    return new JsonApiListResponseWrapper(dataNodePlusOne, pageNumber, pageSize, path,
        new JsonApiMeta(elasticSearchResults.getLeft()));
  }

  private JsonApiListResponseWrapper wrapListResponse(List<AnnotationResponse> annotations,
      String path) {
    List<JsonApiData> dataNode = new ArrayList<>();
    annotations.forEach(annotation -> dataNode.add(
        new JsonApiData(annotation.id(), annotation.type(), annotation, mapper)));
    return new JsonApiListResponseWrapper(dataNode, new JsonApiLinksFull(path));
  }
}
