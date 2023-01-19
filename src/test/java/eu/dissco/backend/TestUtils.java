package eu.dissco.backend;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.dissco.backend.domain.AnnotationEvent;
import eu.dissco.backend.domain.AnnotationRequest;
import eu.dissco.backend.domain.AnnotationResponse;
import eu.dissco.backend.domain.JsonApiData;
import eu.dissco.backend.domain.JsonApiLinks;
import eu.dissco.backend.domain.JsonApiLinksFull;
import eu.dissco.backend.domain.JsonApiMeta;
import eu.dissco.backend.domain.JsonApiMetaWrapper;
import eu.dissco.backend.domain.JsonApiWrapper;
import eu.dissco.backend.domain.User;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

public class TestUtils {

  public static final String USER_ID_TOKEN = "e2befba6-9324-4bb4-9f41-d7dfae4a44b0";
  public final static String TYPE = "users";
  public final static String FORBIDDEN_MESSAGE =
      "User: " + USER_ID_TOKEN + " is not allowed to perform this action";

  public static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();
  public static final Instant CREATED = Instant.parse("2022-11-01T09:59:24.00Z");

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
    return givenAnnotationResponse(USER_ID_TOKEN);
  }

  public static AnnotationResponse givenAnnotationResponse(String userId){
    return new AnnotationResponse(
        "id",
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

  private static JsonNode givenAnnotationTarget(){
    ObjectNode target = MAPPER.createObjectNode();
    target.put("id", "targetId");
    target.put("type", "digitalSpecimen");
    return target;
  }

  private static JsonNode givenAnnotationBody(){
    ObjectNode body = MAPPER.createObjectNode();
    ObjectNode bodyValues = MAPPER.createObjectNode();
    bodyValues.put("class", "leaf");
    bodyValues.put("score", 0.99);
    body.put("source", "https://medialib.naturalis.nl/file/id/ZMA.UROCH.P.1555/format/large");
    body.set("values", bodyValues);
    return body;
  }

  private static JsonNode givenAnnotationGenerator(){
    ObjectNode generator = MAPPER.createObjectNode();
    generator.put("id", "generatorId");
    generator.put("name", "annotation processing service");
    return generator;
  }

  public static JsonApiMetaWrapper givenAnnotationJsonResponse(String path, int pageNumber, int pageSize, int totalPageCount){
    JsonApiMeta metaNode = new JsonApiMeta(totalPageCount);
    JsonApiLinksFull linksNode = givenJsonApiLinksFull(path, pageNumber, pageSize, totalPageCount);
    var dataNodes = givenAnnotationJsonApiData(pageSize);
    return new JsonApiMetaWrapper(dataNodes, linksNode, metaNode);
  }

  public static List<JsonApiData> givenAnnotationJsonApiData(int pageSize){
    return Collections.nCopies(pageSize, new JsonApiData("id", "Annotation", MAPPER.valueToTree(givenAnnotationResponse())));
  }
  public static List<JsonApiData> givenAnnotationJsonApiData(int pageSize, String userId){
    return Collections.nCopies(pageSize, new JsonApiData("id", "Annotation", MAPPER.valueToTree(givenAnnotationResponse(userId))));
  }

  private static JsonApiLinksFull givenJsonApiLinksFull(String path, int pageNumber, int pageSize,
      int totalPageCount) {
    String pn = "?pageNumber=";
    String ps = "&pageSize=";
    String self = path + pn + pageNumber + ps + pageSize;
    String first = path + pn + "0" + ps + pageSize;
    String last = path + pn + totalPageCount + ps + pageSize;
    String prev = (pageNumber == 0) ? null
        : path + pn + (pageNumber - 1) + ps + pageSize;
    String next = (pageNumber >= totalPageCount) ? null
        : path + pn + (pageNumber + 1) + ps + pageSize;
    return new JsonApiLinksFull(self, first, last, prev, next);
  }


}
