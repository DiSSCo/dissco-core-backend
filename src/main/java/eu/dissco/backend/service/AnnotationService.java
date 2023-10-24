package eu.dissco.backend.service;

import static eu.dissco.backend.service.ServiceUtils.createVersionNode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.dissco.backend.client.AnnotationClient;
import eu.dissco.backend.domain.User;
import eu.dissco.backend.domain.annotation.Annotation;
import eu.dissco.backend.domain.annotation.Creator;
import eu.dissco.backend.domain.annotation.Generator;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.domain.jsonapi.JsonApiLinks;
import eu.dissco.backend.domain.jsonapi.JsonApiLinksFull;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiMeta;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import eu.dissco.backend.exceptions.ForbiddenException;
import eu.dissco.backend.exceptions.NoAnnotationFoundException;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.exceptions.PidAuthenticationException;
import eu.dissco.backend.exceptions.PidCreationException;
import eu.dissco.backend.properties.ApplicationProperties;
import eu.dissco.backend.repository.AnnotationRepository;
import eu.dissco.backend.repository.ElasticSearchAnnotationRepository;
import eu.dissco.backend.repository.MongoRepository;
import eu.dissco.backend.web.HandleComponent;
import java.io.IOException;
import java.time.Instant;
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
  private final ElasticSearchAnnotationRepository elasticRepository;
  private final MongoRepository mongoRepository;
  private final UserService userService;
  private final ObjectMapper mapper;
  private final ApplicationProperties applicationProperties;
  private final HandleComponent handleComponent;
  private final FdoRecordService fdoRecordService;

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
      String path)
      throws ForbiddenException, JsonProcessingException, PidAuthenticationException, PidCreationException {
    var user = getUserInformation(userId);
    var annotation = enrichNewAnnotation(annotationRequest, user);
    var handleRequest = fdoRecordService.buildPostHandleRequest(annotation);
    var a = handleComponent.postHandle(handleRequest);

    var response = annotationClient.postAnnotation(annotation);
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

  private User getUserInformation(String userId) throws ForbiddenException {
    var user = userService.getUser(userId);
    if (user.orcid() == null) {
      throw new ForbiddenException("No ORCID is provided");
    }
    return user;
  }

  private Annotation enrichNewAnnotation(Annotation annotationRequest, User user) {
    var timestamp = Instant.now();
    return annotationRequest
        .withOaCreator(processCreator(user))
        .withAsGenerator(createGenerator())
        .withDcTermsCreated(timestamp)
        .withOaGenerated(timestamp)
        .withOdsVersion(1);
  }

  private void enrichUpdateAnnotation(Annotation annotation, Annotation currentAnnotation) {
    annotation
        .withOaCreator(currentAnnotation.getOaCreator())
        .withAsGenerator(currentAnnotation.getAsGenerator())
        .withOdsVersion(currentAnnotation.getOdsVersion() + 1)
        .withOdsId(currentAnnotation.getOdsId())
        .withDcTermsCreated(currentAnnotation.getDcTermsCreated())
        .withOaGenerated(currentAnnotation.getOaGenerated());
  }

  private Generator createGenerator() {
    return new Generator()
        .withOdsId(applicationProperties.getGeneratorHandle())
        .withFoafName("Annotation Processing Service")
        .withOdsType("tool/Software");
  }

  private Creator processCreator(User user) {
    return new Creator()
        .withOdsId(user.orcid())
        .withFoafName(user.firstName() + " " + user.lastName())
        .withOdsType("ORCID");
  }

  private Annotation parseToAnnotation(JsonNode response) throws JsonProcessingException {
    return mapper.treeToValue(response, Annotation.class);
  }

  public JsonApiWrapper updateAnnotation(String id, Annotation annotation, String userId,
      String path, String prefix, String suffix)
      throws NoAnnotationFoundException, ForbiddenException, JsonProcessingException {
    var result = repository.getAnnotationForUser(id, userId);
    if (result > 0) {
      if (annotation.getOdsId() == null) {
        annotation.withOdsId(id);
      }
      var user = getUserInformation(userId);
      enrichNewAnnotation(annotation, user, );
      var response = annotationClient.updateAnnotation(prefix, suffix, annotation);
      return formatResponse(response, path);
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
  public List<Annotation> getAnnotationForTargetObject(String id) {
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
