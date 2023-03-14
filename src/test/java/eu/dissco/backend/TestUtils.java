package eu.dissco.backend;


import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.dissco.backend.domain.DigitalMediaObject;
import eu.dissco.backend.domain.DigitalSpecimen;
import eu.dissco.backend.domain.User;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.domain.jsonapi.JsonApiLinks;
import eu.dissco.backend.domain.jsonapi.JsonApiLinksFull;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class TestUtils {

  public static final String USER_ID_TOKEN = "e2befba6-9324-4bb4-9f41-d7dfae4a44b0";
  public static final String TYPE = "users";
  public static final String FORBIDDEN_MESSAGE =
      "User: " + USER_ID_TOKEN + " is not allowed to perform this action";
  public static final String PREFIX = "20.5000.1025";
  public static final String POSTFIX = "ABC-123-XYZ";
  public static final String ID = PREFIX + "/" + POSTFIX;
  public static final String ID_ALT = PREFIX + "/" + "AAA-111-ZZZ";
  public static final String TARGET_ID = PREFIX + "/TAR_GET_001";

  public static final ObjectMapper MAPPER = new ObjectMapper()
      .findAndRegisterModules()
      .setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS"))
      .setSerializationInclusion(Include.NON_NULL);
  public static final Instant CREATED = Instant.parse("2022-11-01T09:59:24.00Z");

  public static final String SANDBOX_URI = "https://sandbox.dissco.tech/";

  // Users
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
    return new User("Test", "User", "test@gmail.com", "https://orcid.org/0000-0002-XXXX-XXXX",
        "https://ror.org/XXXXXXXXX");
  }

  // Annotation


  // General
  public static JsonApiLinksFull givenJsonApiLinksFull(String path, int pageNumber, int pageSize,
      boolean hasNextPage) {
    return new JsonApiLinksFull(pageNumber, pageSize, hasNextPage, path);
  }

  // Digital Media Objects
  public static DigitalMediaObject givenDigitalMediaObject(String id) {
    return new DigitalMediaObject(id, 1, CREATED, "2DImageObject", "20.5000.1025/460-A7R-QMJ",
        "https://dissco.com", "image/jpeg", "20.5000.1025/GW0-TYL-YRU",
        givenDigitalMediaObjectData(), givenDigitalMediaObjectOriginalData());
  }

  public static DigitalMediaObject givenDigitalMediaObject(String mediaId, String specimenId) {
    return new DigitalMediaObject(mediaId, 1, CREATED, "2DImageObject", specimenId,
        "https://dissco.com", "image/jpeg", "20.5000.1025/GW0-TYL-YRU",
        givenDigitalMediaObjectData(), givenDigitalMediaObjectOriginalData());
  }

  private static JsonNode givenDigitalMediaObjectData() {
    ObjectNode data = MAPPER.createObjectNode();
    data.put("dcterms:title", "19942272");
    data.put("dcterms:publisher", "Royal Botanic Garden Edinburgh");
    return data;
  }

  private static JsonNode givenDigitalMediaObjectOriginalData() {
    ObjectNode originalData = MAPPER.createObjectNode();
    originalData.put("dcterms:title", "19942272");
    originalData.put("dcterms:type", "StillImage");
    return originalData;
  }

  public static JsonApiListResponseWrapper givenDigitalMediaJsonResponse(String path,
      int pageNumber,
      int pageSize, List<String> mediaIds) {
    JsonApiLinksFull linksNode = new JsonApiLinksFull(pageNumber, pageSize, true, path);
    List<JsonApiData> dataNode = new ArrayList<>();
    for (String id : mediaIds) {
      var mediaObject = givenDigitalMediaObject(id);
      dataNode.add(new JsonApiData(id, "2dImageObject", MAPPER.valueToTree(mediaObject)));
    }
    return new JsonApiListResponseWrapper(dataNode, linksNode);
  }

  public static JsonApiWrapper givenDigitalMediaJsonResponse(String path, String mediaId) {
    JsonApiLinks linksNode = new JsonApiLinks(path);
    JsonApiData dataNode = new JsonApiData(mediaId, "2dImageObject",
        MAPPER.valueToTree(givenDigitalMediaObject(mediaId)));
    return new JsonApiWrapper(dataNode, linksNode);
  }

  public static JsonApiData givenDigitalMediaJsonApiData(String id) {
    return new JsonApiData(id, "2dImageObject", MAPPER.valueToTree(givenDigitalMediaObject(id)));
  }

  public static JsonApiData givenMediaObjectJsonApiDataWithSpeciesName(
      DigitalMediaObject mediaObject, DigitalSpecimen specimen) {
    ObjectNode attributeNode = MAPPER.createObjectNode();
    ObjectNode specimenNode = MAPPER.createObjectNode();

    attributeNode.put("id", mediaObject.id());
    attributeNode.put("version", mediaObject.version());
    attributeNode.put("type", mediaObject.type());
    attributeNode.put("created", String.valueOf(mediaObject.created()));
    attributeNode.put("digitalSpecimenId", String.valueOf(mediaObject.digitalSpecimenId()));
    attributeNode.put("mediaUrl", mediaObject.mediaUrl());
    attributeNode.put("format", mediaObject.format());
    attributeNode.put("sourceSystemId", mediaObject.sourceSystemId());
    attributeNode.set("data", mediaObject.data());
    attributeNode.set("originalData", mediaObject.originalData());
    specimenNode.put("digitalSpecimenName", specimen.specimenName());
    specimenNode.put("digitalSpecimenVersion", specimen.version());
    attributeNode.set("digitalSpecimen", specimenNode);

    return new JsonApiData(attributeNode.get("id").asText(), attributeNode.get("type").asText(),
        attributeNode);
  }

  public static DigitalSpecimen givenDigitalSpecimen(String id) throws JsonProcessingException {
    return givenDigitalSpecimen(id, 1);
  }

  // Digital Specimen
  public static DigitalSpecimen givenDigitalSpecimen(String id, int version)
      throws JsonProcessingException {
    return new DigitalSpecimen(
        id,
        1,
        version,
        CREATED,
        "BotanySpecimen",
        "123",
        "cetaf",
        "Leucanthemum ircutianum (Turcz.) Turcz.ex DC.",
        "https://ror.org/0349vqz63",
        "Royal Botanic Garden Edinburgh Herbarium",
        "http://biocol.org/urn:lsid:biocol.org:col:15670",
        "20.5000.1025/3XA-8PT-SAY",
        givenSpecimenData(),
        givenSpecimenOriginalData(),
        "http://data.rbge.org.uk/herb/E00586417");
  }

  private static JsonNode givenSpecimenOriginalData() throws JsonProcessingException {
    return MAPPER.readValue(
        """
              {
                "dwc:class": "Malacostraca",
                "dwc:genus": "Mesuca",
                "dwc:order": "Decapoda",
                "dwc:family": "Ocypodidae",
                "dwc:phylum": "Arthropoda",
                "dwc:country": "Nicobar Islands",
                "dwc:locality": "Harbour",
                "dwc:continent": "Eastern Indian Ocean",
                "dwc:eventDate": "01/01/1846",
                "dwc:recordedBy": "Rosen",
                "dcterms:license": "http://creativecommons.org/licenses/by/4.0/legalcode",
                "dwc:datasetName": "Natural History Museum Denmark Invertebrate Zoology",
                "dcterms:modified": "03/12/2012",
                "dwc:occurrenceID": "debe5b20-e945-40e8-8a55-6d92391ff495",
                "dwc:preparations": "various - 1",
                "dwc:basisOfRecord": "PreservedSpecimen",
                "dwc:catalogNumber": "NHMD79044",
                "dwc:institutionID": "http://grbio.org/cool/mci8-ehqk",
                "dwc:collectionCode": "IV",
                "dwc:higherGeography": "Eastern Indian Ocean, Nicobar Islands",
                "dwc:institutionCode": "NHMD",
                "dwc:specificEpithet": "dussumieri",
                "dwc:acceptedNameUsage": "Mesuca dussumieri",
                "dwc:otherCatalogNumbers": "CRU-001196"
              }
            """, JsonNode.class);
  }

  private static JsonNode givenSpecimenData() throws JsonProcessingException {
    return MAPPER.readValue(
        """
            {
              "dwca:id": "http://data.rbge.org.uk/herb/E00586417",
              "ods:modified": "03/12/2012",
              "ods:datasetId": "Royal Botanic Garden Edinburgh Herbarium",
              "ods:objectType": "",
              "dcterms:license": "http://creativecommons.org/licenses/by/4.0/legalcode",
              "ods:specimenName": "Leucanthemum ircutianum (Turcz.) Turcz.ex DC.",
              "ods:organizationId": "https://ror.org/0349vqz63",
              "ods:sourceSystemId": "20.5000.1025/3XA-8PT-SAY",
              "ods:physicalSpecimenIdType": "cetaf",
              "ods:physicalSpecimenCollection": "http://biocol.org/urn:lsid:biocol.org:col:15670"      
            }
            """, JsonNode.class);
  }
}
