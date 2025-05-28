package eu.dissco.backend.service;

import static eu.dissco.backend.domain.FdoType.ANNOTATION;
import static eu.dissco.backend.service.DigitalServiceUtils.createVersionNode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.client.AnnotationClient;
import eu.dissco.backend.domain.FdoType;
import eu.dissco.backend.domain.annotation.AnnotationTombstoneWrapper;
import eu.dissco.backend.domain.annotation.batch.AnnotationEvent;
import eu.dissco.backend.domain.annotation.batch.AnnotationEventRequest;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.domain.jsonapi.JsonApiLinks;
import eu.dissco.backend.domain.jsonapi.JsonApiLinksFull;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiMeta;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import eu.dissco.backend.domain.openapi.annotation.BatchAnnotationCountRequest;
import eu.dissco.backend.exceptions.NoAnnotationFoundException;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.repository.AnnotationRepository;
import eu.dissco.backend.repository.ElasticSearchRepository;
import eu.dissco.backend.repository.MongoRepository;
import eu.dissco.backend.schema.Agent;
import eu.dissco.backend.schema.Annotation;
import eu.dissco.backend.schema.Annotation.OaMotivation;
import eu.dissco.backend.schema.AnnotationProcessingRequest;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnnotationService {

  private static final String ATTRIBUTES = "attributes";
  private static final String DATA = "data";
  private final AnnotationRepository repository;
  private final AnnotationClient annotationClient;
  private final ElasticSearchRepository elasticRepository;
  private final MongoRepository mongoRepository;
  private final ObjectMapper mapper;
  private final MasJobRecordService masJobRecordService;

  public JsonApiWrapper getAnnotation(String id, String path) {
    var annotation = repository.getAnnotation(id);
    var dataNode = new JsonApiData(id, FdoType.ANNOTATION.getName(),
        mapper.valueToTree(annotation));
    return new JsonApiWrapper(dataNode, new JsonApiLinks(path));
  }

  public JsonApiWrapper getAnnotationByVersion(String id, int version, String path)
      throws NotFoundException, JsonProcessingException {
    var eventNode = mongoRepository.getByVersion(id, version, "annotation_provenance");
    validateAnnotationNode(eventNode);
    var dataNode = new JsonApiData(id, ANNOTATION.getName(), eventNode);
    return new JsonApiWrapper(dataNode, new JsonApiLinks(path));
  }

  public JsonApiListResponseWrapper getAnnotations(int pageNumber, int pageSize,
      String path) {
    var annotationsPlusOne = repository.getAnnotations(pageNumber, pageSize);
    return wrapListResponse(annotationsPlusOne, pageNumber, pageSize, path);
  }

  public JsonApiWrapper persistAnnotation(AnnotationProcessingRequest annotationProcessingRequest,
      Agent agent, String path) throws JsonProcessingException {
    var annotation = buildAnnotation(annotationProcessingRequest, agent, false);
    var response = annotationClient.postAnnotation(annotation);
    return formatResponse(response, path);
  }

  public JsonApiWrapper persistAnnotationBatch(AnnotationEventRequest eventRequest, Agent agent,
      String path) throws JsonProcessingException {
    var processedAnnotation = buildAnnotation(eventRequest.annotationRequests().get(0), agent,
        false)
        .withOdsPlaceInBatch(1);
    String jobId = null;
    if (eventRequest.batchMetadata() != null) {
      jobId = masJobRecordService.createJobRecordForDisscover(processedAnnotation, agent.getId());
    }
    var processedEvent = new AnnotationEvent(List.of(processedAnnotation),
        eventRequest.batchMetadata(), jobId);
    var response = annotationClient.postAnnotationBatch(processedEvent);
    return formatResponse(response, path);
  }

  public JsonApiWrapper formatResponse(JsonNode response, String path)
      throws JsonProcessingException {
    if (response != null) {
      var annotationResponse = parseToAnnotation(response);
      var dataNode = new JsonApiData(annotationResponse.getId(), ANNOTATION.getName(),
          response);
      return new JsonApiWrapper(dataNode, new JsonApiLinks(path));
    }
    return null;
  }

  public JsonNode getCountForBatchAnnotations(BatchAnnotationCountRequest annotationCountRequest)
      throws IOException {
    var count = elasticRepository.getCountForBatchAnnotations(
        annotationCountRequest.data().attributes()
            .batchMetadata(),
        annotationCountRequest.data().attributes().annotationTargetType());
    return mapper.createObjectNode()
        .set(DATA, mapper.createObjectNode()
            .put("type", "batchAnnotationCount")
            .set(ATTRIBUTES, mapper.createObjectNode()
                .put("objectAffected", count)
                .set("batchMetadata", mapper.valueToTree(annotationCountRequest.data().attributes()
                    .batchMetadata()))));
  }

  private Annotation buildAnnotation(AnnotationProcessingRequest annotationProcessingRequest,
      Agent agent, boolean isUpdate) {
    var annotation = new Annotation()
        .withOaMotivation(
            OaMotivation.fromValue(annotationProcessingRequest.getOaMotivation().value()))
        .withOaMotivatedBy(annotationProcessingRequest.getOaMotivatedBy())
        .withOaHasBody(annotationProcessingRequest.getOaHasBody())
        .withOaHasTarget(annotationProcessingRequest.getOaHasTarget())
        .withDctermsCreated(Date.from(Instant.now()))
        .withDctermsCreator(agent);
    if (isUpdate) {
      annotation.setId(annotationProcessingRequest.getDctermsIdentifier());
      annotation.setDctermsIdentifier(annotationProcessingRequest.getDctermsIdentifier());
    }
    return annotation;
  }

  private Annotation parseToAnnotation(JsonNode response) throws JsonProcessingException {
    return mapper.treeToValue(response, Annotation.class);
  }

  public JsonApiWrapper updateAnnotation(String id,
      AnnotationProcessingRequest annotationProcessingRequest,
      Agent agent,
      String path, String prefix, String suffix)
      throws NoAnnotationFoundException, JsonProcessingException {
    var result = repository.getActiveAnnotation(id, agent.getId());
    if (result.isPresent()) {
      if (annotationProcessingRequest.getDctermsIdentifier() == null) {
        annotationProcessingRequest.setDctermsIdentifier(id);
      }
      var annotation = buildAnnotation(annotationProcessingRequest, agent, true);
      var response = annotationClient.updateAnnotation(prefix, suffix, annotation);
      return formatResponse(response, path);
    } else {
      log.info("No active annotationRequests with id: {} found for user {}", id,
          agent.getId());
      throw new NoAnnotationFoundException(
          "No active annotationRequests with id: " + id + " was found for user");
    }
  }

  public JsonApiListResponseWrapper getAnnotationsForUser(String orcid, int pageNumber,
      int pageSize, String path) throws IOException {
    var elasticSearchResults = elasticRepository.getAnnotationsForCreator(orcid, pageNumber,
        pageSize);
    return wrapListResponseElasticSearchResults(elasticSearchResults, pageNumber, pageSize, path);
  }

  public JsonApiWrapper getAnnotationVersions(String id, String path) throws NotFoundException {
    var versions = mongoRepository.getVersions(id, "annotation_provenance");
    var versionsNode = createVersionNode(versions, mapper);
    var dataNode = new JsonApiData(id, "annotationVersions", versionsNode);
    return new JsonApiWrapper(dataNode, new JsonApiLinks(path));
  }

  public boolean tombstoneAnnotation(String prefix, String suffix, Agent agent, boolean isAdmin)
      throws NoAnnotationFoundException {
    var id = prefix + "/" + suffix;
    Optional<Annotation> result;
    if (isAdmin) {
      log.info("Admin tombstoning annotation {}", id);
      result = repository.getActiveAnnotation(id, null);
    } else {
      log.info("Creator tombstoning annotation {}", id);
      result = repository.getActiveAnnotation(id, agent.getId());
    }
    if (result.isPresent()) {
      annotationClient.tombstoneAnnotation(prefix, suffix,
          new AnnotationTombstoneWrapper(result.get(), agent));
      return true;
    } else {
      if (isAdmin) {
        log.info("No active annotations with id: {}", id);
      } else {
        log.info("No active annotations with id: {} found for user: {}", id, agent.getId());
      }
      throw new NoAnnotationFoundException(
          "No active annotationRequests with id: " + id + " was found");
    }
  }

  // Used by other services
  public List<Annotation> getAnnotationForTargetObject(String id) {
    var fullId = getFullId(id);
    return repository.getForTarget(fullId);
  }

  public Map<String, List<Annotation>> getAnnotationForTargetObjects(List<String> ids) {
    var annotations = repository.getForTargets(ids.stream().map(AnnotationService::getFullId).toList());
    var annotationMap = new HashMap<String, List<Annotation>>();
    annotations.forEach(
        annotation -> annotationMap.computeIfAbsent(
                annotation.getOaHasTarget().getId(), k -> new ArrayList<>())
            .add(annotation));
    return annotationMap;
  }

  public JsonApiListResponseWrapper getAnnotationForTarget(String id, String path) {
    var fullId = getFullId(id);
    var annotations = repository.getForTarget(fullId);
    return wrapListResponse(annotations, path);
  }

  private static String getFullId(String id) {
    return (id.contains("https://doi.org/")) ? id : "https://doi.org/" + id;
  }

  // Response Constructors
  private void validateAnnotationNode(JsonNode annotationNode) throws JsonProcessingException {
    mapper.treeToValue(annotationNode.get(ANNOTATION.getName()), Annotation.class);
  }

  private JsonApiListResponseWrapper wrapListResponse(List<Annotation> annotationsPlusOne,
      int pageNumber, int pageSize, String path) {
    List<JsonApiData> dataNodePlusOne = new ArrayList<>();
    annotationsPlusOne.forEach(annotation -> dataNodePlusOne.add(
        new JsonApiData(annotation.getId(), FdoType.ANNOTATION.getName(),
            mapper.valueToTree(annotation))));
    return new JsonApiListResponseWrapper(dataNodePlusOne, pageNumber, pageSize, path);
  }

  private JsonApiListResponseWrapper wrapListResponseElasticSearchResults(
      Pair<Long, List<Annotation>> elasticSearchResults, int pageNumber, int pageSize,
      String path) {
    List<JsonApiData> dataNodePlusOne = new ArrayList<>();
    var annotationsPlusOne = elasticSearchResults.getRight();
    annotationsPlusOne.forEach(annotation -> dataNodePlusOne.add(
        new JsonApiData(annotation.getId(), FdoType.ANNOTATION.getName(),
            mapper.valueToTree(annotation))));
    return new JsonApiListResponseWrapper(dataNodePlusOne, pageNumber, pageSize, path,
        new JsonApiMeta(elasticSearchResults.getLeft()));
  }

  private JsonApiListResponseWrapper wrapListResponse(List<Annotation> annotations,
      String path) {
    List<JsonApiData> dataNode = new ArrayList<>();
    annotations.forEach(annotation -> dataNode.add(
        new JsonApiData(annotation.getId(), ANNOTATION.getName(),
            mapper.valueToTree(annotation))));
    return new JsonApiListResponseWrapper(dataNode, new JsonApiLinksFull(path));
  }
}
