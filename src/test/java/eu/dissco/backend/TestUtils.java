package eu.dissco.backend;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.dissco.backend.domain.AnnotationEvent;
import eu.dissco.backend.domain.AnnotationRequest;
import eu.dissco.backend.domain.AnnotationResponse;
import eu.dissco.backend.domain.DigitalMediaObject;
import eu.dissco.backend.domain.JsonApiData;
import eu.dissco.backend.domain.JsonApiLinks;
import eu.dissco.backend.domain.JsonApiLinksFull;
import eu.dissco.backend.domain.JsonApiMeta;
import eu.dissco.backend.domain.JsonApiMetaWrapper;
import eu.dissco.backend.domain.JsonApiWrapper;
import eu.dissco.backend.domain.User;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j

public class TestUtils {

  public static final String USER_ID_TOKEN = "e2befba6-9324-4bb4-9f41-d7dfae4a44b0";
  public final static String TYPE = "users";
  public final static String FORBIDDEN_MESSAGE =
      "User: " + USER_ID_TOKEN + " is not allowed to perform this action";

  public static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();
  public static final Instant CREATED = Instant.parse("2022-11-01T09:59:24.00Z");
  public static final String SANDBOX_URI = "https://sandbox.dissco.tech/";

  public static JsonApiWrapper givenUserResponse() {
    return new JsonApiWrapper(givenJsonApiData(),
        new JsonApiLinks("https://sandbox.dissco.tech/api/v1/users/" + USER_ID_TOKEN));
  }

  public static JsonApiWrapper givenUserRequest() {
    return givenUserRequest(USER_ID_TOKEN);
  }

  public static JsonApiWrapper givenUserRequestInvalidType() {
    return new JsonApiWrapper(
        new JsonApiData(USER_ID_TOKEN, "annotations", MAPPER.valueToTree(givenUser())), null);
  }

  public static JsonApiWrapper givenUserRequest(String id) {
    return new JsonApiWrapper(givenJsonApiData(id), null);
  }

  public static JsonApiData givenJsonApiData() {
    return givenJsonApiData(USER_ID_TOKEN);
  }

  public static JsonApiData givenJsonApiData(String id) {
    return new JsonApiData(id, TYPE, MAPPER.valueToTree(givenUser()));
  }

  public static User givenUser() {
    return new User(
        "Test",
        "User",
        "test@gmail.com",
        "https://orcid.org/0000-0002-XXXX-XXXX",
        "https://ror.org/XXXXXXXXX"
    );
  }

  public static AnnotationRequest givenAnnotationRequest(){
    return new AnnotationRequest("Annotation", "motivation", givenAnnotationTarget(), givenAnnotationBody());
  }

  public static AnnotationEvent givenAnnotationEvent(AnnotationRequest annotation){
    return new AnnotationEvent(
        annotation.type(),
        annotation.motivation(),
        USER_ID_TOKEN,
        CREATED,
        annotation.target(),
        annotation.body()
    );
  }

  public static AnnotationResponse givenAnnotationResponse(){
    return givenAnnotationResponse(USER_ID_TOKEN, "id");
  }

  public static AnnotationResponse givenAnnotationResponse(String userId, String annotationId){
    return new AnnotationResponse(
        annotationId,
        1,
        "Annotation",
        "motivation",
        givenAnnotationTarget(),
        givenAnnotationBody(),
        100,
        userId,
        CREATED,
        givenAnnotationGenerator(),
        CREATED,
        null
    );
  }


  public static JsonNode givenAnnotationTarget(){
    ObjectNode target = MAPPER.createObjectNode();
    target.put("id", "targetId");
    target.put("type", "digitalSpecimen");
    return target;
  }

  public static JsonNode givenAnnotationBody(){
    ObjectNode body = MAPPER.createObjectNode();
    ObjectNode bodyValues = MAPPER.createObjectNode();
    bodyValues.put("class", "leaf");
    bodyValues.put("score", 0.99);
    body.put("source", "https://medialib.naturalis.nl/file/id/ZMA.UROCH.P.1555/format/large");
    body.set("values", bodyValues);
    return body;
  }

  public static JsonNode givenAnnotationGenerator(){
    ObjectNode generator = MAPPER.createObjectNode();
    generator.put("id", "generatorId");
    generator.put("name", "annotation processing service");
    return generator;
  }

  public static JsonApiMetaWrapper givenAnnotationJsonResponse(String path, int pageNumber, int pageSize, int totalPageCount,
      String userId, String annotationId){
    JsonApiMeta metaNode = new JsonApiMeta(totalPageCount);
    JsonApiLinksFull linksNode = givenJsonApiLinksFull(path, pageNumber, pageSize, totalPageCount);

    var dataNodes = givenAnnotationJsonApiDataList(pageSize, userId, annotationId);

    return new JsonApiMetaWrapper(dataNodes, linksNode, metaNode);
  }

  public static JsonApiMetaWrapper givenAnnotationJsonResponse(String path, int pageNumber, int pageSize, int totalPageCount,
      String userId, List<String> annotationIds){
    JsonApiMeta metaNode = new JsonApiMeta(totalPageCount);
    JsonApiLinksFull linksNode = givenJsonApiLinksFull(path, pageNumber, pageSize, totalPageCount);

    var dataNodes = givenAnnotationJsonApiDataList(userId, annotationIds);
    return new JsonApiMetaWrapper(dataNodes, linksNode, metaNode);
  }


  public static List<JsonApiData> givenAnnotationJsonApiDataList(int pageSize, String userId, String annotationId) {
    return Collections.nCopies(pageSize, new JsonApiData("id", "Annotation", MAPPER.valueToTree(givenAnnotationResponse(userId, annotationId))));
  }

  public static List<JsonApiData> givenAnnotationJsonApiDataList(String userId, List<String> annotationIds) {
    List<JsonApiData> dataNodes = new ArrayList<>();
    ObjectMapper mapper = new ObjectMapper().findAndRegisterModules().configure(
        SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    for (String annotationId : annotationIds){
      ObjectNode annotation = mapper.valueToTree(givenAnnotationResponse(userId, annotationId));
      annotation.put("deleted", annotation.get("deleted_on").asText());
      annotation.remove("deleted_on");
      dataNodes.add(new JsonApiData(annotationId, "Annotation", annotation));
    }
    return dataNodes;
  }

  public static JsonApiData givenAnnotationJsonApiData(String userId, String annotationId){
    ObjectMapper mapper = new ObjectMapper().findAndRegisterModules().configure(
        SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    ObjectNode dataNode = mapper.valueToTree(givenAnnotationResponse(userId, annotationId));
    dataNode.put("deleted", dataNode.get("deleted_on").asText());
    dataNode.remove("deleted_on");
    return new JsonApiData(annotationId, "Annotation", dataNode);
  }

  private static JsonApiLinksFull givenJsonApiLinksFull(String path, int pageNumber, int pageSize,
      int totalPageCount) {
    String pn = "?pageNumber=";
    String ps = "&pageSize=";
    String self = path + pn + pageNumber + ps + pageSize;
    String first = path + pn + "1" + ps + pageSize;
    String last = path + pn + totalPageCount + ps + pageSize;
    String prev = (pageNumber <= 1) ? null
        : path + pn + (pageNumber - 1) + ps + pageSize;
    String next = (pageNumber >= totalPageCount) ? null
        : path + pn + (pageNumber + 1) + ps + pageSize;
    return new JsonApiLinksFull(self, first, last, prev, next);
  }

  // Digital Media Objects
  public static DigitalMediaObject givenDigitalMediaObject(String id){
    return new DigitalMediaObject(
        id,
        1,
        CREATED,
        "2DImageObject",
        "20.5000.1025/460-A7R-QMJ",
        "https://dissco.com",
        "image/jpeg",
        "20.5000.1025/GW0-TYL-YRU",
        givenDigitalMediaObjectData(),
        givenDigitalMediaObjectOrigintalData()
    );
  }
  private static JsonNode givenDigitalMediaObjectData() {
    ObjectNode data = MAPPER.createObjectNode();
    data.put("dcterms:title", "19942272");
    data.put("dcterms:publisher", "Royal Botanic Garden Edinburg");
    return data;
  }

  private static JsonNode givenDigitalMediaObjectOrigintalData() {
    ObjectNode originalData = MAPPER.createObjectNode();
    originalData.put("dcterms:title", "19942272");
    originalData.put("dcterms:type", "StillImage");
    return originalData;
  }

  public static JsonApiMetaWrapper givenMediaJsonResponse(String path, int pageNumber, int pageSize, int totalPageCount, List<String> mediaIds){
    JsonApiMeta metaNode = new JsonApiMeta(totalPageCount);
    JsonApiLinksFull linksNode = givenJsonApiLinksFull(path, pageNumber, pageSize, totalPageCount);
    List<JsonApiData> dataNode = new ArrayList<>();
    for (String id: mediaIds){
      var mediaObject = givenDigitalMediaObject(id);
      dataNode.add(new JsonApiData(id, "2dImageObject", MAPPER.valueToTree(mediaObject)));
    }
    return new JsonApiMetaWrapper(dataNode, linksNode, metaNode);
  }

  public static JsonApiWrapper givenMediaJsonResponse(String path, String mediaId){
    JsonApiLinks linksNode = new JsonApiLinks(path);
    JsonApiData dataNode = new JsonApiData(mediaId, "2dImageObject", MAPPER.valueToTree(givenDigitalMediaObject(mediaId)));
    return new JsonApiWrapper(dataNode, linksNode);
  }
}
