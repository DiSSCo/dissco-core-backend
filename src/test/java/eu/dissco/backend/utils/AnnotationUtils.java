package eu.dissco.backend.utils;

import static eu.dissco.backend.TestUtils.CREATED;
import static eu.dissco.backend.TestUtils.HANDLE;
import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.ORCID;
import static eu.dissco.backend.TestUtils.SANDBOX_URI;
import static eu.dissco.backend.TestUtils.TARGET_ID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import eu.dissco.backend.domain.annotation.batch.AnnotationEvent;
import eu.dissco.backend.domain.annotation.batch.AnnotationEventRequest;
import eu.dissco.backend.domain.annotation.batch.BatchMetadata;
import eu.dissco.backend.domain.annotation.batch.SearchParam;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.domain.jsonapi.JsonApiLinks;
import eu.dissco.backend.domain.jsonapi.JsonApiLinksFull;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiRequest;
import eu.dissco.backend.domain.jsonapi.JsonApiRequestWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import eu.dissco.backend.schema.Agent;
import eu.dissco.backend.schema.Agent.Type;
import eu.dissco.backend.schema.Annotation;
import eu.dissco.backend.schema.AnnotationBody;
import eu.dissco.backend.schema.AnnotationProcessingRequest;
import eu.dissco.backend.schema.AnnotationProcessingRequest.OaMotivation;
import eu.dissco.backend.schema.OaHasSelector;
import eu.dissco.backend.schema.AnnotationTarget;
import eu.dissco.backend.schema.SchemaAggregateRating;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class AnnotationUtils {

  public static final String ANNOTATION_URI = "/api/v1/annotationRequests/";
  public static final String ANNOTATION_PATH = SANDBOX_URI + ANNOTATION_URI;

  private AnnotationUtils() {
  }

  public static List<Annotation> givenAnnotationResponseList(String annotationId, int n) {
    return Collections.nCopies(n, givenAnnotationResponse(annotationId));
  }

  public static Annotation givenAnnotationResponse() {
    return givenAnnotationResponse(ID, ORCID, TARGET_ID);
  }

  public static Annotation givenAnnotationResponse(String annotationId) {
    return givenAnnotationResponse(annotationId, ORCID, TARGET_ID);
  }

  public static Annotation givenAnnotationResponse(String annotationId, String userId) {
    return givenAnnotationResponse(annotationId, userId, TARGET_ID);
  }

  public static Annotation givenAnnotationResponse(String annotationId, String userId,
      String targetId) {
    return new Annotation()
        .withId(annotationId)
        .withOdsID(annotationId)
        .withType("ods:Anotation")
        .withOdsVersion(1)
        .withOaHasBody(givenOaBodyAnnotation())
        .withRdfType("ods:Annotation")
        .withOaMotivation(Annotation.OaMotivation.OA_COMMENTING)
        .withDctermsCreator(givenCreator(userId))
        .withOaHasTarget(givenOaTargetAnnotation(targetId))
        .withDctermsCreated(Date.from(CREATED))
        .withDctermsModified(Date.from(CREATED))
        .withDctermsIssued(Date.from(CREATED))
        .withAsGenerator(givenGenerator())
        .withSchemaAggregateRating(givenAggregationRating());
  }

  public static Annotation givenAnnotationProcessingRequest(boolean isUpdate) {
    var annotation = new Annotation()
        .withOaHasBody(givenOaBodyAnnotation())
        .withOaMotivation(Annotation.OaMotivation.OA_COMMENTING)
        .withDctermsCreator(givenCreator(ORCID))
        .withOaHasTarget(givenOaTargetAnnotation(TARGET_ID))
        .withDctermsCreated(Date.from(CREATED));
    if (isUpdate) {
      annotation.withId(HANDLE + ID)
          .withOdsID(HANDLE + ID);
    }
    return annotation;
  }

  public static AnnotationProcessingRequest givenAnnotationRequest(String targetId) {
    return new AnnotationProcessingRequest()
        .withOaHasBody(givenOaBody())
        .withOaHasTarget(givenOaTarget(targetId))
        .withOaMotivation(OaMotivation.OA_COMMENTING);
  }

  public static AnnotationProcessingRequest givenAnnotationRequest() {
    return givenAnnotationRequest(TARGET_ID);
  }

  public static AnnotationBody givenOaBody() {
    return new AnnotationBody()
        .withType("ods:DigitalSpecimen")
        .withOaValue(new ArrayList<>(List.of("a comment")))
        .withDctermsReferences(
            "https://medialib.naturalis.nl/file/id/ZMA.UROCH.P.1555/format/large")
        .withOdsScore(0.99);
  }

  public static AnnotationBody givenOaBodyAnnotation() {
    return new AnnotationBody()
        .withType("ods:DigitalSpecimen")
        .withOaValue(new ArrayList<>(List.of("a comment")))
        .withDctermsReferences(
            "https://medialib.naturalis.nl/file/id/ZMA.UROCH.P.1555/format/large")
        .withOdsScore(0.99);
  }

  public static AnnotationTarget givenOaTarget(String targetId) {
    return new AnnotationTarget()
        .withId(targetId)
        .withOdsID(targetId)
        .withType("ods:DigitalSpecimen")
        .withOdsType("https://doi.org/21.T11148/894b1e6cad57e921764e")
        .withOaHasSelector(givenSelector());
  }

  public static AnnotationTarget givenOaTargetAnnotation(String targetId) {
    return new AnnotationTarget()
        .withId(targetId)
        .withOdsID(targetId)
        .withType("ods:DigitalSpecimen")
        .withOdsType("https://doi.org/21.T11148/894b1e6cad57e921764e")
        .withOaHasSelector(givenSelectorAnnotation());
  }

  public static OaHasSelector givenSelector() {
    return new OaHasSelector()
        .withAdditionalProperty("@type", "ods:FieldSelector")
        .withAdditionalProperty("ods:field", "ods:specimenName");
  }

  public static OaHasSelector givenSelectorAnnotation() {
    return new OaHasSelector()
        .withAdditionalProperty("@type", "ods:FieldSelector")
        .withAdditionalProperty("field", "ods:specimenName");
  }

  public static Agent givenCreator(String userId) {
    return new Agent()
        .withId(userId)
        .withType(Type.SCHEMA_PERSON)
        .withSchemaName("User");
  }

  public static Agent givenGenerator() {
    return new Agent()
        .withSchemaName("DiSSCo backend")
        .withId("https://sandbox.dissco.tech")
        .withType(Type.AS_APPLICATION);
  }

  public static SchemaAggregateRating givenAggregationRating() {
    return new SchemaAggregateRating()
        .withType("schema:AggregateRating")
        .withSchemaRatingValue(0.1)
        .withSchemaRatingCount(2);
  }

  public static JsonApiRequestWrapper givenJsonApiAnnotationRequest(Object request) {
    return new JsonApiRequestWrapper(
        new JsonApiRequest(
            "ods:Annotation",
            MAPPER.valueToTree(request)
        )
    );
  }

  // JsonApiWrapper
  public static JsonApiWrapper givenAnnotationResponseSingleDataNode(String path) {
    return givenAnnotationResponseSingleDataNode(path, ORCID);
  }

  public static JsonApiWrapper givenAnnotationResponseSingleDataNode(String path, String userId) {
    var annotation = givenAnnotationResponse(ID, userId);
    var dataNode = new JsonApiData(ID, "ods:Annotation", MAPPER.valueToTree(annotation));
    return new JsonApiWrapper(dataNode, new JsonApiLinks(path));
  }

  public static JsonApiWrapper givenAnnotationResponseBatch(String path, String userId) {
    var annotation = givenAnnotationResponse(ID, userId).withOdsPlaceInBatch(1);
    var dataNode = new JsonApiData(ID, "ods:Annotation", MAPPER.valueToTree(annotation));
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
    return Collections.nCopies(pageSize, new JsonApiData(annotationId, "ods:Annotation",
        MAPPER.valueToTree(givenAnnotationResponse(annotationId, userId, TARGET_ID))));
  }

  public static JsonApiListResponseWrapper givenAnnotationJsonResponseNoPagination(String path,
      List<String> annotationIds) {
    JsonApiLinksFull linksNode = new JsonApiLinksFull(path);
    List<JsonApiData> dataNodes = new ArrayList<>();

    annotationIds.forEach(id -> dataNodes.add(new JsonApiData(id, "ods:Annotation",
        MAPPER.valueToTree(givenAnnotationResponse(id)))));
    return new JsonApiListResponseWrapper(dataNodes, linksNode);
  }

  public static AnnotationEventRequest givenAnnotationEventRequest() {
    return new AnnotationEventRequest(List.of(givenAnnotationRequest()),
        List.of(new BatchMetadata(List.of(givenSearchParam()))));
  }

  public static AnnotationEvent givenAnnotationEventProcessed() {
    return new AnnotationEvent(List.of(givenAnnotationProcessingRequest(false)),
        List.of(new BatchMetadata(List.of(givenSearchParam()))));
  }

  public static BatchMetadata givenBatchMetadata() {
    return new BatchMetadata(List.of(givenSearchParam()));
  }

  public static SearchParam givenSearchParam() {
    return new SearchParam(
        "ods:hasEvent.ods:Location.dwc:country.keyword",
        "Netherlands"
    );
  }

  public static SearchParam givenSearchParam(String country) {
    return new SearchParam(
        "ods:hasEvent[*].ods:Location.dwc:country",
        country
    );
  }

  public static JsonNode givenAnnotationCountRequest() throws JsonProcessingException {
    return MAPPER.readTree("""
        {
          "data": {
            "type": "batchAnnotationCount",
            "attributes": {
              "annotationTargetType": "https://doi.org/21.T11148/894b1e6cad57e921764e",
              "batchMetadata": {
                "searchParams": [
                  {
                    "inputField": "ods:hasEvent.ods:Location.dwc:country.keyword",
                    "inputValue": "Netherlands"
                  }
                ]
              }
            }
          }
        }
        """);
  }


}
