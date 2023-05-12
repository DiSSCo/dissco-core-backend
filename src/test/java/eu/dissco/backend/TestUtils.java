package eu.dissco.backend;


import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import eu.dissco.backend.configuration.InstantDeserializer;
import eu.dissco.backend.configuration.InstantSerializer;
import eu.dissco.backend.domain.DigitalSpecimen;
import eu.dissco.backend.domain.User;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.domain.jsonapi.JsonApiLinks;
import eu.dissco.backend.domain.jsonapi.JsonApiLinksFull;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import java.time.Instant;
import java.util.Map;

public class TestUtils {

  public static final String USER_ID_TOKEN = "e2befba6-9324-4bb4-9f41-d7dfae4a44b0";
  public static final String TYPE = "users";
  public static final String FORBIDDEN_MESSAGE =
      "User: " + USER_ID_TOKEN + " is not allowed to perform this action";
  public static final String HANDLE = "https://hdl.handle.net/";
  public static final String PREFIX = "20.5000.1025";
  public static final String SUFFIX = "ABC-123-XYZ";
  public static final String ID = PREFIX + "/" + SUFFIX;
  public static final String ID_ALT = PREFIX + "/" + "AAA-111-ZZZ";
  public static final String TARGET_ID = PREFIX + "/TAR_GET_001";
  public static final String SOURCE_SYSTEM_ID_1 = "20.5000.1025/3XA-8PT-SAY";
  public static final String SOURCE_SYSTEM_ID_2 = "20.5000.1025/ANO-THE-RAY";

  public static final ObjectMapper MAPPER;
  public static final Instant CREATED = Instant.parse("2022-11-01T09:59:24.00Z");
  public static final String SANDBOX_URI = "https://sandbox.dissco.tech";

  static {
    var mapper = new ObjectMapper().findAndRegisterModules();
    SimpleModule dateModule = new SimpleModule();
    dateModule.addSerializer(Instant.class, new InstantSerializer());
    dateModule.addDeserializer(Instant.class, new InstantDeserializer());
    mapper.registerModule(dateModule);
    mapper.setSerializationInclusion(Include.NON_NULL);
    MAPPER = mapper.copy();
  }

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

  // General
  public static JsonApiLinksFull givenJsonApiLinksFull(String path, int pageNumber, int pageSize,
      boolean hasNextPage) {
    return new JsonApiLinksFull(pageNumber, pageSize, hasNextPage, path);
  }

  // Digital Specimen
  public static DigitalSpecimen givenDigitalSpecimen(String id) throws JsonProcessingException {
    return givenDigitalSpecimen(id, "global_id_123123");
  }

  public static DigitalSpecimen givenDigitalSpecimenSourceSystem(String id, String sourceSystem)
      throws JsonProcessingException {
    return givenDigitalSpecimen(id, "global_id_123123", 1, sourceSystem);
  }

  public static DigitalSpecimen givenDigitalSpecimen(String id, String physicalId)
      throws JsonProcessingException {
    return givenDigitalSpecimen(id, physicalId, 1, SOURCE_SYSTEM_ID_1);
  }

  public static DigitalSpecimen givenDigitalSpecimen(String id, String physicalId, String sourceSystem)
      throws JsonProcessingException {
    return givenDigitalSpecimen(id, physicalId, 1, sourceSystem);
  }

  public static DigitalSpecimen givenDigitalSpecimen(String id, String physicalId, int version,
      String sourceSystemId)
      throws JsonProcessingException {
    return new DigitalSpecimen(
        id,
        1,
        version,
        CREATED,
        "BotanySpecimen",
        physicalId,
        "cetaf",
        "Leucanthemum ircutianum (Turcz.) Turcz.ex DC.",
        "https://ror.org/0349vqz63",
        "Royal Botanic Garden Edinburgh Herbarium",
        "http://biocol.org/urn:lsid:biocol.org:col:15670",
        sourceSystemId,
        givenSpecimenData(sourceSystemId),
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

  private static JsonNode givenSpecimenData(String sourceSystemId) {
    var node = MAPPER.createObjectNode();
    node.put("dwca:id", "http://data.rbge.org.uk/herb/E00586417");
    node.put("ods:modified", "03/12/2012");
    node.put("ods:datasetId", "Royal Botanic Garden Edinburgh Herbarium");
    node.put("ods:objectType", "");
    node.put("dcterms:license", "http://creativecommons.org/licenses/by/4.0/legalcode");
    node.put("ods:specimenName", "Leucanthemum ircutianum (Turcz.) Turcz.ex DC.");
    node.put("ods:organisationId", "https://ror.org/0349vqz63");
    node.put("ods:sourceSystemId", sourceSystemId);
    node.put("ods:physicalSpecimenIdType", "cetaf");
    node.put("ods:physicalSpecimenCollection", "http://biocol.org/urn:lsid:biocol.org:col:15670");
    node.put("ods:specimenName", "Leucanthemum ircutianum (Turcz.) Turcz.ex DC.");
    node.put("dwc:typeStatus", "holotype");
    node.put("dwc:country", "Scotland");
    node.put("ods:hasMedia", "true");
    return node;
  }

  public static Map<String, Map<String, Long>> givenAggregationMap() {
    return Map.of(
        "sourceSystem", Map.of(SOURCE_SYSTEM_ID_1, 5L, SOURCE_SYSTEM_ID_2, 5L),
        "typeStatus", Map.of("type", 10L),
        "hasMedia", Map.of("true", 10L)
    );
  }
}
