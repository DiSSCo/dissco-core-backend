package eu.dissco.backend.utils;

import static eu.dissco.backend.TestUtils.CREATED;
import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.ORCID;
import static eu.dissco.backend.TestUtils.SANDBOX_URI;
import static eu.dissco.backend.TestUtils.TARGET_ID;
import static eu.dissco.backend.TestUtils.USER_ID_TOKEN;

import eu.dissco.backend.domain.annotation.AggregateRating;
import eu.dissco.backend.domain.annotation.Annotation;
import eu.dissco.backend.domain.annotation.Body;
import eu.dissco.backend.domain.annotation.Creator;
import eu.dissco.backend.domain.annotation.FieldSelector;
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

  public static final String ANNOTATION_URI = "/api/v1/annotation/";
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
    return Annotation.builder()
        .odsId(annotationId)
        .odsVersion(1)
        .oaBody(givenOaBody())
        .oaMotivation(Motivation.COMMENTING)
        .oaTarget(givenOaTarget(targetId))
        .oaCreator(givenCreator(userId))
        .dcTermsCreated(CREATED)
        .oaGenerated(CREATED)
        .asGenerator(givenGenerator())
        .odsAggregateRating(givenAggregationRating())
        .build();
  }

  public static Annotation givenAnnotationKafkaRequest(boolean isUpdate){
    var request = givenAnnotationRequest()
        .setOaCreator(givenCreator(ORCID));
    if (!isUpdate){
      return request;
    }
    return request
        .setDcTermsCreated(CREATED);
  }

  public static Annotation givenAnnotationRequest(String targetId) {
    return Annotation.builder()
        .oaBody(givenOaBody())
        .oaMotivation(Motivation.COMMENTING)
        .oaTarget(givenOaTarget(targetId))
        .build();
  }

  public static Annotation givenAnnotationRequest() {
    return givenAnnotationRequest(TARGET_ID);
  }

  public static Body givenOaBody() {
    return Body.builder()
        .odsType("ods:specimenName")
        .oaValue(new ArrayList<>(List.of("a comment")))
        .dcTermsReference("https://medialib.naturalis.nl/file/id/ZMA.UROCH.P.1555/format/large")
        .odsScore(0.99)
        .build();
  }

  public static Target givenOaTarget(String targetId) {
    return Target.builder()
        .odsId(targetId)
        .oaSelector(givenSelector())
        .odsType("digital_specimen")
        .build();
  }

  public static FieldSelector givenSelector() {
    return new FieldSelector()
        .withOdsField("ods:specimenName");
  }

  public static Creator givenCreator(String userId) {
    return Creator.builder()
        .foafName("Test User")
        .odsId(userId)
        .odsType("ORCID")
        .build();
  }

  public static Generator givenGenerator(){
    return Generator.builder()
        .foafName("DiSSCo backend")
        .odsId("https://sandbox.dissco.tech")
        .odsType("Technical Backend")
        .build();
  }

  public static AggregateRating givenAggregationRating(){
    return AggregateRating.builder()
        .ratingValue(0.1)
        .odsType("Score")
        .ratingCount(0.2)
        .build();
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
