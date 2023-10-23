package eu.dissco.backend.utils;

import static eu.dissco.backend.TestUtils.CREATED;
import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.ID_ALT;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.ORCID;
import static eu.dissco.backend.TestUtils.SANDBOX_URI;
import static eu.dissco.backend.TestUtils.TARGET_ID;
import static eu.dissco.backend.TestUtils.USER_ID_TOKEN;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.dissco.backend.domain.annotation.AggregateRating;
import eu.dissco.backend.domain.annotation.Annotation;
import eu.dissco.backend.domain.annotation.Body;
import eu.dissco.backend.domain.annotation.Creator;
import eu.dissco.backend.domain.annotation.FieldValueSelector;
import eu.dissco.backend.domain.annotation.Generator;
import eu.dissco.backend.domain.annotation.Motivation;
import eu.dissco.backend.domain.annotation.Target;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.domain.jsonapi.JsonApiLinks;
import eu.dissco.backend.domain.jsonapi.JsonApiLinksFull;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiRequest;
import eu.dissco.backend.domain.jsonapi.JsonApiRequestWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AnnotationUtils {

  public static final String ANNOTATION_URI = "/api/v1/annotations/";
  public static final String ANNOTATION_PATH = SANDBOX_URI + ANNOTATION_URI;

  private AnnotationUtils() {
  }

  public static List<Annotation> givenAnnotationResponseList(String annotationId, int n) {
    return Collections.nCopies(n, givenAnnotationResponse(annotationId));
  }

  public static Annotation givenAnnotationResponse(){
    return givenAnnotationResponse(ID, USER_ID_TOKEN, TARGET_ID);
  }

  public static Annotation givenAnnotationResponse(String annotationId) {
    return givenAnnotationResponse(annotationId, USER_ID_TOKEN, TARGET_ID);
  }
  public static Annotation givenAnnotationResponse(String annotationId, String userId) {
    return givenAnnotationResponse(annotationId, userId, TARGET_ID);
  }

  public static Annotation givenAnnotationResponse(String annotationId, String userId, String targetId) {
    return new Annotation()
        .withOdsId(annotationId)
        .withRdfType("Annotation")
        .withOdsVersion(1)
        .withOaBody(givenOaBody())
        .withOaMotivation(Motivation.COMMENTING)
        .withOaTarget(givenOaTarget(targetId))
        .withOaCreator(givenCreator(userId))
        .withDcTermsCreated(CREATED)
        .withOaGenerated(CREATED)
        .withAsGenerator(givenGenerator())
        .withOdsAggregateRating(givenAggregationRating());
  }

  public static Annotation givenAnnotationKafkaRequest(boolean isUpdate){
    var request = givenAnnotationRequest()
        .withOaCreator(givenCreator(ORCID));
    if (!isUpdate){
      return request;
    }
    return request
        .withDcTermsCreated(CREATED);
  }

  public static Annotation givenAnnotationRequest(String targetId) {
    return new Annotation()
        .withOaBody(givenOaBody())
        .withOaMotivation(Motivation.COMMENTING)
        .withOaTarget(givenOaTarget(targetId));
  }

  public static Annotation givenAnnotationRequest() {
    return givenAnnotationRequest(TARGET_ID);
  }

  public static Body givenOaBody() {
    return new Body()
        .withOdsType("ods:specimenName")
        .withOaValue(new ArrayList<>(List.of("a comment")))
        .withDcTermsReference("https://medialib.naturalis.nl/file/id/ZMA.UROCH.P.1555/format/large")
        .withOdsScore(0.99);
  }

  public static Target givenOaTarget(String targetId) {
    return new Target()
        .withOdsId(targetId)
        .withSelector(givenSelector())
        .withOdsType("digital_specimen");
  }

  public static FieldValueSelector givenSelector() {
    return new FieldValueSelector()
        .withOdsField("ods:specimenName");
  }

  public static Creator givenCreator(String userId) {
    return new Creator()
        .withFoafName("Test User")
        .withOdsId(userId)
        .withOdsType("ORCID");
  }

  public static Generator givenGenerator(){
    return new Generator()
        .withFoafName("DiSSCo backend")
        .withOdsId("https://sandbox.dissco.tech")
        .withOdsType("Technical Backend");
  }

  public static AggregateRating givenAggregationRating(){
    return new AggregateRating()
        .withRatingValue(0.1)
        .withOdsType("Score")
        .withRatingCount(0.2);
  }

  public static JsonApiRequestWrapper givenJsonApiAnnotationRequest(Annotation request) {
    return new JsonApiRequestWrapper(
        new JsonApiRequest(
            "annotation",
            MAPPER.valueToTree(request)
        )
    );
  }

  // JsonApiWrapper

  public static JsonApiWrapper givenAnnotationResponseSingleDataNode(String path) {
    return givenAnnotationResponseSingleDataNode(path, USER_ID_TOKEN);
  }

  public static JsonApiWrapper givenAnnotationResponseSingleDataNode(String path, String userId) {
    var annotation = givenAnnotationResponse(ID, userId);
    var dataNode = new JsonApiData(ID, "annotation", MAPPER.valueToTree(annotation));
    return new JsonApiWrapper(dataNode, new JsonApiLinks(path));
  }

  // JsonApiListResponseWrapper
  public static JsonApiListResponseWrapper givenAnnotationJsonResponse(String path, int pageNumber,
      int pageSize, String userId, String annotationId, boolean hasNextPage) {
    JsonApiLinksFull linksNode = new JsonApiLinksFull(pageNumber, pageSize, hasNextPage, path);
    var dataNodes = givenAnnotationJsonApiDataList(pageSize, userId, annotationId);
    return new JsonApiListResponseWrapper(dataNodes, linksNode);
  }

  public static List<JsonApiData> givenAnnotationJsonApiDataList(int pageSize, String userId,
      String annotationId) {
    return Collections.nCopies(pageSize, new JsonApiData(annotationId, "annotation",
        MAPPER.valueToTree(givenAnnotationResponse(annotationId, userId, TARGET_ID))));
  }

  public static JsonApiListResponseWrapper givenAnnotationJsonResponseNoPagination(String path,
      List<String> annotationIds) {
    JsonApiLinksFull linksNode = new JsonApiLinksFull(path);
    List<JsonApiData> dataNodes = new ArrayList<>();

    annotationIds.forEach(id -> dataNodes.add(new JsonApiData(id, "annotation",
        MAPPER.valueToTree(givenAnnotationResponse(id)))));
    return new JsonApiListResponseWrapper(dataNodes, linksNode);
  }

}
