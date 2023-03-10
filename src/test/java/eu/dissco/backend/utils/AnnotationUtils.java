package eu.dissco.backend.utils;

import static eu.dissco.backend.TestUtils.CREATED;
import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.SANDBOX_URI;
import static eu.dissco.backend.TestUtils.TARGET_ID;
import static eu.dissco.backend.TestUtils.USER_ID_TOKEN;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.dissco.backend.domain.AnnotationRequest;
import eu.dissco.backend.domain.AnnotationResponse;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.domain.jsonapi.JsonApiLinks;
import eu.dissco.backend.domain.jsonapi.JsonApiLinksFull;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AnnotationUtils {
  private AnnotationUtils(){}

  public static final String ANNOTATION_URI = "api/v1/annotations/";
  public static final String ANNOTATION_PATH = SANDBOX_URI + ANNOTATION_URI;

  public static AnnotationRequest givenAnnotationRequest() {
    return new AnnotationRequest("Annotation", "motivation", givenAnnotationTarget(TARGET_ID),
        givenAnnotationBody());
  }

  public static AnnotationResponse givenAnnotationResponse() {
    return givenAnnotationResponse(USER_ID_TOKEN, ID);
  }

  public static AnnotationResponse givenAnnotationResponseTarget(String id, String target){
    return givenAnnotationResponse(USER_ID_TOKEN, id, target);
  }

  public static AnnotationResponse givenAnnotationResponse(String userId, String annotationId) {
    return givenAnnotationResponse(userId, annotationId, TARGET_ID);
  }

  public static AnnotationResponse givenAnnotationResponse(String userId, String annotationId, String targetId) {
    return new AnnotationResponse(annotationId, 1, "Annotation", "motivation",
        givenAnnotationTarget(targetId), givenAnnotationBody(), 100, userId, CREATED,
        givenAnnotationGenerator(), CREATED, null);
  }

  public static JsonNode givenAnnotationTarget(String targetId) {
    ObjectNode target = MAPPER.createObjectNode();
    target.put("id", targetId);
    target.put("type", "digitalSpecimen");
    return target;
  }

  public static JsonNode givenAnnotationBody() {
    ObjectNode body = MAPPER.createObjectNode();
    ArrayNode bodyValues = MAPPER.createArrayNode();
    ObjectNode value = MAPPER.createObjectNode();
    value.put("class", "leaf");
    value.put("score", 0.99);
    body.put("source", "https://medialib.naturalis.nl/file/id/ZMA.UROCH.P.1555/format/large");
    bodyValues.add(value);
    body.set("values", bodyValues);
    return body;
  }

  public static JsonNode givenAnnotationGenerator() {
    ObjectNode generator = MAPPER.createObjectNode();
    generator.put("id", "generatorId");
    generator.put("name", "annotation processing service");
    return generator;
  }

  public static JsonApiListResponseWrapper givenAnnotationJsonResponse(String path, int pageNumber,
      int pageSize, String userId, String annotationId, boolean hasNextPage) {
    JsonApiLinksFull linksNode = new JsonApiLinksFull(pageNumber, pageSize, hasNextPage, path);
    var dataNodes = givenAnnotationJsonApiDataList(pageSize, userId, annotationId);
    return new JsonApiListResponseWrapper(dataNodes, linksNode);
  }

  public static JsonApiListResponseWrapper givenAnnotationJsonResponse(String path, int pageNumber,
      int pageSize, String userId, List<String> annotationIds, boolean hasNextPage) {
    JsonApiLinksFull linksNode = new JsonApiLinksFull(pageNumber, pageSize, hasNextPage, path);

    var dataNodes = givenAnnotationJsonApiDataList(userId, annotationIds);
    return new JsonApiListResponseWrapper(dataNodes, linksNode);
  }

  public static List<JsonApiData> givenAnnotationJsonApiDataList(int pageSize, String userId,
      String annotationId) {
    return Collections.nCopies(pageSize, new JsonApiData("id", "Annotation",
        MAPPER.valueToTree(givenAnnotationResponse(userId, annotationId))));
  }

  public static List<JsonApiData> givenAnnotationJsonApiDataList(String userId,
      List<String> annotationIds) {
    List<JsonApiData> dataNodes = new ArrayList<>();
    ObjectMapper mapper = new ObjectMapper().findAndRegisterModules()
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    for (String annotationId : annotationIds) {
      ObjectNode annotation = mapper.valueToTree(givenAnnotationResponse(userId, annotationId));
      annotation.put("deleted", annotation.get("deleted_on").asText());
      annotation.remove("deleted_on");
      dataNodes.add(new JsonApiData(annotationId, "Annotation", annotation));
    }
    return dataNodes;
  }

  public static JsonApiData givenAnnotationJsonApiData(String userId, String annotationId) {
    ObjectMapper mapper = new ObjectMapper().findAndRegisterModules()
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    ObjectNode dataNode = mapper.valueToTree(givenAnnotationResponse(userId, annotationId));
    dataNode.put("deleted", dataNode.get("deleted_on").asText());
    dataNode.remove("deleted_on");
    return new JsonApiData(annotationId, "Annotation", dataNode);
  }

  public static JsonApiWrapper givenAnnotationResponseSingleDataNode(String path){
    var annotation = givenAnnotationResponse();
    var dataNode = new JsonApiData(annotation.id(),"Annotation", MAPPER.valueToTree(annotation));
    return new JsonApiWrapper(dataNode, new JsonApiLinks(path));
  }

}