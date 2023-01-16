package eu.dissco.backend.service;

import static eu.dissco.backend.database.jooq.Tables.NEW_ANNOTATION;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.dissco.backend.client.AnnotationClient;
import eu.dissco.backend.domain.AnnotationEvent;
import eu.dissco.backend.domain.AnnotationRequest;
import eu.dissco.backend.domain.AnnotationResponse;
import eu.dissco.backend.domain.JsonApiData;
import eu.dissco.backend.domain.JsonApiLinks;
import eu.dissco.backend.domain.JsonApiMeta;
import eu.dissco.backend.domain.JsonApiMetaWrapper;
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

  public List<AnnotationResponse> getAnnotationsForUser(String userId, int pageNumber,
      int pageSize) {
    return repository.getAnnotationsForUser(userId, pageNumber, pageSize);
  }

  public JsonApiMetaWrapper getAnnotationsForUserJsonResponse(String userId, int pageNumber,
      int pageSize, String path) {
    var annotations = repository.getAnnotationsForUser(userId, pageNumber, pageSize);
    var dataNode = annotations.stream()
        .map(this::mapAnnotationToJsonDataNode)
        .toList();
    JsonApiLinks linksNode = new JsonApiLinks(path + "?pageNumber="+pageNumber+"&pageSize="+pageSize);
    JsonApiMeta metaNode = new JsonApiMeta(repository.getAnnotationsCountForUser(userId, pageSize));
    return new JsonApiMetaWrapper(dataNode, linksNode, metaNode);
  }

  private JsonApiData mapAnnotationToJsonDataNode(AnnotationResponse annotation) {
    ObjectNode attributeNode = mapper.createObjectNode();

    attributeNode.put("id", annotation.id());
    attributeNode.put("version", annotation.version());
    attributeNode.put("type", annotation.type());
    attributeNode.put("motivation", annotation.motivation());
    attributeNode.set("target", annotation.target());
    attributeNode.set("body", annotation.body());
    attributeNode.put("preferenceScore", annotation.preferenceScore());
    attributeNode.put("creator", annotation.creator());
    attributeNode.put("created", String.valueOf(annotation.created()));
    attributeNode.set("generator", annotation.generator());
    attributeNode.put("generated", String.valueOf(annotation.generated()));
    attributeNode.put("deletedOn", String.valueOf(annotation.deleted_on()));

    JsonApiData dataNode = new JsonApiData(annotation.id(), "Annotation", attributeNode);

    return dataNode;
  }

    public AnnotationResponse getAnnotation (String id){
      return repository.getAnnotation(id);
    }

    public List<AnnotationResponse> getAnnotations ( int pageNumber, int pageSize){
      return repository.getAnnotations(pageNumber, pageSize);
    }

  public JsonApiMetaWrapper getAnnotationsJsonResponse (int pageNumber, int pageSize, String path){
    var annotations = repository.getAnnotations(pageNumber, pageSize);
    var dataNode = annotations.stream()
        .map(this::mapAnnotationToJsonDataNode)
        .toList();
    JsonApiLinks linksNode = new JsonApiLinks(path + "?pageNumber="+pageNumber+"&pageSize="+pageSize);
    JsonApiMeta metaNode = new JsonApiMeta(repository.getAnnotationsCountGlobal(pageSize));
    return new JsonApiMetaWrapper(dataNode, linksNode, metaNode);
  }

    public List<AnnotationResponse> getLatestAnnotations ( int pageNumber, int pageSize)
      throws IOException {
      return elasticRepository.getLatestAnnotations(pageNumber, pageSize);
    }

    public AnnotationResponse persistAnnotation (AnnotationRequest annotation, String userId){
      var event = mapAnnotationRequestToEvent(annotation, userId);
      var response = annotationClient.postAnnotation(event);
      if (response != null) {
        return mapResponseToAnnotationResponse(response);
      }
      return null;
    }

    private AnnotationResponse mapResponseToAnnotationResponse (JsonNode response){
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

    public AnnotationResponse getAnnotationVersion (String id,int version){
      return repository.getAnnotationVersion(id, version);
    }

    public List<AnnotationResponse> getAnnotationForTarget (String id){
      var fullId = "https://hdl.handle.net/" + id;
      return repository.getForTarget(fullId);
    }

    public List<Integer> getAnnotationVersions (String id){
      return repository.getAnnotationVersions(id);
    }

    public boolean deleteAnnotation (String prefix, String postfix, String userId)
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

    public AnnotationResponse updateAnnotation (String id, AnnotationRequest annotation, String
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

    public List<AnnotationResponse> getLatestAnnotation () throws IOException {
      return elasticRepository.getLatestAnnotation();
    }

  }
