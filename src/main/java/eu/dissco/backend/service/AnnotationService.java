package eu.dissco.backend.service;

import static eu.dissco.backend.service.ServiceUtils.createVersionNode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.dissco.backend.client.AnnotationClient;
import eu.dissco.backend.domain.User;
import eu.dissco.backend.domain.annotation.Annotation;
import eu.dissco.backend.domain.annotation.AnnotationTargetType;
import eu.dissco.backend.domain.annotation.Creator;
import eu.dissco.backend.domain.annotation.batch.AnnotationEvent;
import eu.dissco.backend.domain.annotation.batch.BatchMetadata;
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
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.common.errors.InvalidRequestException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnnotationService {

  private static final String ANNOTATION = "annotations";
  private static final String ATTRIBUTES = "attributes";
  private static final String DATA = "data";
  private static final String VERSION = "version";

  private final AnnotationRepository repository;
  private final AnnotationClient annotationClient;
  private final ElasticSearchRepository elasticRepository;
  private final MongoRepository mongoRepository;
  private final UserService userService;
  private final ObjectMapper mapper;

  public JsonApiWrapper getAnnotation(String id, String path) {
    var annotation = repository.getAnnotation(id);
    var dataNode = new JsonApiData(id, ANNOTATION, mapper.valueToTree(annotation));
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
    var type = annotationNode.get("rdf:type").asText();
    var dataNode = new JsonApiData(id, type, annotationNode);
    return new JsonApiWrapper(dataNode, new JsonApiLinks(path));
  }

  public JsonApiListResponseWrapper getAnnotations(int pageNumber, int pageSize,
      String path) {
    var annotationsPlusOne = repository.getAnnotations(pageNumber, pageSize);
    return wrapListResponse(annotationsPlusOne, pageNumber, pageSize, path);
  }

  public JsonApiWrapper persistAnnotation(Annotation annotationRequest, String userId,
      String path) throws ForbiddenException, JsonProcessingException {
    var user = getUserInformation(userId);
    var annotation = processAnnotation(annotationRequest, user, false);
    var response = annotationClient.postAnnotation(annotation);
    return formatResponse(response, path);
  }

  public JsonApiWrapper persistAnnotation(AnnotationEvent eventRequest, String userId, String path)
      throws ForbiddenException, JsonProcessingException {
    var user = getUserInformation(userId);
    var processedAnnotation = processAnnotation(eventRequest.annotations().get(0), user, false)
        .setPlaceInBatch(1);
    var processedEvent = new AnnotationEvent(List.of(processedAnnotation),
        eventRequest.batchMetadata());
    var response = annotationClient.postAnnotationBatch(processedEvent);
    return formatResponse(response, path);
  }

  public JsonApiWrapper formatResponse(JsonNode response, String path)
      throws JsonProcessingException {
    if (response != null) {
      var annotationResponse = parseToAnnotation(response);
      var dataNode = new JsonApiData(annotationResponse.getOdsId(), ANNOTATION,
          response);
      return new JsonApiWrapper(dataNode, new JsonApiLinks(path));
    }
    return null;
  }

  public JsonNode getCountForBatchAnnotations(JsonNode annotationCountJson) throws IOException {
    var annotationCountRequest = getAnnotationBatchCount(annotationCountJson);
    var count = elasticRepository.getCountForBatchAnnotations(annotationCountRequest.getLeft(),
        annotationCountRequest.getRight());
    return mapper.createObjectNode()
        .set(DATA, mapper.createObjectNode()
            .put("type", "batchAnnotationCount")
            .set(ATTRIBUTES, mapper.createObjectNode()
                .put("objectAffected", count)
                .set("batchMetadata", mapper.valueToTree(annotationCountRequest.getLeft()))));
  }

  private Pair<BatchMetadata, AnnotationTargetType> getAnnotationBatchCount(
      JsonNode annotationCountRequest) {
    try {
      var batchMetadata = mapper.treeToValue(
          annotationCountRequest.get(DATA).get(ATTRIBUTES).get("batchMetadata"),
          BatchMetadata.class);
      var targetType = mapper.treeToValue(
          annotationCountRequest.get(DATA).get(ATTRIBUTES).get("annotationTargetType"),
          AnnotationTargetType.class);
      assert (batchMetadata != null);
      assert (targetType != null);
      return Pair.of(batchMetadata, targetType);
    } catch (JsonProcessingException | NullPointerException | AssertionError e) {
      log.info("Unable to read request body for batch annotation count", e);
      throw new InvalidRequestException("Invalid request for batch annotation request");
    }
  }

  private User getUserInformation(String userId) throws ForbiddenException {
    var user = userService.getUser(userId);
    if (user.orcid() == null) {
      throw new ForbiddenException("No ORCID is provided");
    }
    return user;
  }

  private Annotation processAnnotation(Annotation annotationRequest, User user, boolean isUpdate) {
    annotationRequest
        .setOaCreator(processCreator(user));
    if (isUpdate) {
      return annotationRequest;
    }
    return annotationRequest.setDcTermsCreated(Instant.now());
  }

  private Creator processCreator(User user) {
    return Creator.builder()
        .odsId(user.orcid())
        .foafName(user.firstName() + " " + user.lastName())
        .odsType("ORCID")
        .build();
  }

  private Annotation parseToAnnotation(JsonNode response) throws JsonProcessingException {
    return mapper.treeToValue(response, Annotation.class);
  }

  public JsonApiWrapper updateAnnotation(String id, Annotation annotation, String userId,
      String path, String prefix, String suffix)
      throws NoAnnotationFoundException, ForbiddenException, JsonProcessingException {
    var user = getUserInformation(userId);
    var result = repository.getAnnotationForUser(id, user.orcid());
    if (result > 0) {
      if (annotation.getOdsId() == null) {
        annotation.setOdsId(id);
      }
      processAnnotation(annotation, user, true);
      var response = annotationClient.updateAnnotation(prefix, suffix, annotation);
      return formatResponse(response, path);
    } else {
      log.info("No active annotations with id: {} found for user {} with orcid {}", id, userId,
          user.orcid());
      throw new NoAnnotationFoundException(
          "No active annotations with id: " + id + " was found for user");
    }
  }

  public JsonApiListResponseWrapper getAnnotationsForUser(String userToken, int pageNumber,
      int pageSize, String path) throws IOException, ForbiddenException {
    var user = getUserInformation(userToken);
    var elasticSearchResults = elasticRepository.getAnnotationsForCreator(user.orcid(), pageNumber,
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
    var orcid = userService.getUser(userId).orcid();
    var result = repository.getAnnotationForUser(id, orcid);
    if (result > 0) {
      annotationClient.deleteAnnotation(prefix, suffix);
      return true;
    } else {
      log.info("No active annotations with id: {} found for user: {}", id, userId);
      throw new NoAnnotationFoundException(
          "No active annotations with id: " + id + " was found for user");
    }
  }

  // Used by other services
  public List<Annotation> getAnnotationForTargetObject(String id) {
    var fullId = getFullId(id);
    return repository.getForTarget(fullId);
  }

  public JsonApiListResponseWrapper getAnnotationForTarget(String id, String path) {
    var fullId = getFullId(id);
    var annotations = repository.getForTarget(fullId);
    return wrapListResponse(annotations, path);
  }

  private String getFullId(String id) {
    return (id.contains("https://doi.org/")) ? id : "https://doi.org/" + id;
  }

  // Response Constructors
  private void validateAnnotationNode(JsonNode annotationNode) throws JsonProcessingException {
    mapper.treeToValue(annotationNode.get(ANNOTATION), Annotation.class);
  }

  private JsonApiListResponseWrapper wrapListResponse(List<Annotation> annotationsPlusOne,
      int pageNumber, int pageSize, String path) {
    List<JsonApiData> dataNodePlusOne = new ArrayList<>();
    annotationsPlusOne.forEach(annotation -> dataNodePlusOne.add(
        new JsonApiData(annotation.getOdsId(), ANNOTATION,
            mapper.valueToTree(annotation))));
    return new JsonApiListResponseWrapper(dataNodePlusOne, pageNumber, pageSize, path);
  }

  private JsonApiListResponseWrapper wrapListResponseElasticSearchResults(
      Pair<Long, List<Annotation>> elasticSearchResults, int pageNumber, int pageSize,
      String path) {
    List<JsonApiData> dataNodePlusOne = new ArrayList<>();
    var annotationsPlusOne = elasticSearchResults.getRight();
    annotationsPlusOne.forEach(annotation -> dataNodePlusOne.add(
        new JsonApiData(annotation.getOdsId(), ANNOTATION,
            mapper.valueToTree(annotation))));
    return new JsonApiListResponseWrapper(dataNodePlusOne, pageNumber, pageSize, path,
        new JsonApiMeta(elasticSearchResults.getLeft()));
  }

  private JsonApiListResponseWrapper wrapListResponse(List<Annotation> annotations,
      String path) {
    List<JsonApiData> dataNode = new ArrayList<>();
    annotations.forEach(annotation -> dataNode.add(
        new JsonApiData(annotation.getOdsId(), ANNOTATION,
            mapper.valueToTree(annotation))));
    return new JsonApiListResponseWrapper(dataNode, new JsonApiLinksFull(path));
  }
}
