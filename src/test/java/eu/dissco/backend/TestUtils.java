package eu.dissco.backend;


import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import eu.dissco.backend.configuration.InstantDeserializer;
import eu.dissco.backend.configuration.InstantSerializer;
import eu.dissco.backend.domain.DigitalSpecimenWrapper;
import eu.dissco.backend.domain.User;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.domain.jsonapi.JsonApiLinks;
import eu.dissco.backend.domain.jsonapi.JsonApiLinksFull;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import eu.dissco.backend.schema.DigitalSpecimen;
import eu.dissco.backend.schema.DigitalSpecimen.OdsPhysicalSpecimenIdType;
import eu.dissco.backend.schema.DigitalSpecimen.OdsTopicDiscipline;
import eu.dissco.backend.schema.Location;
import eu.dissco.backend.schema.Occurrences;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class TestUtils {

  public static final String USER_ID_TOKEN = "e2befba6-9324-4bb4-9f41-d7dfae4a44b0";
  public static final String TYPE = "users";
  public static final String FORBIDDEN_MESSAGE =
      "User: " + USER_ID_TOKEN + " is not allowed to perform this action";
  public static final String HANDLE = "https://hdl.handle.net/";
  public static final String DOI = "https://doi.org/";
  public static final String PREFIX = "20.5000.1025";
  public static final String SUFFIX = "ABC-123-XYZ";
  public static final String ID = PREFIX + "/" + SUFFIX;
  public static final String ID_ALT = PREFIX + "/" + "AAA-111-ZZZ";
  public static final String TARGET_ID = PREFIX + "/TAR_GET_001";
  public static final String SOURCE_SYSTEM_ID_1 = HANDLE + "20.5000.1025/3XA-8PT-SAY";
  public static final String SOURCE_SYSTEM_ID_2 = HANDLE + "20.5000.1025/ANO-THE-RAY";

  public static final ObjectMapper MAPPER;
  public static final Instant CREATED = Instant.parse("2022-11-01T09:59:24.00Z");
  public static final String SANDBOX_URI = "https://sandbox.dissco.tech";
  public static final String ORCID = "https://orcid.org/0000-0002-XXXX-XXXX";

  public static final String DIGITAL_SPECIMEN_TYPE = "https://doi.org/21.T11148/894b1e6cad57e921764e";

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
    return new User("Test", "User", "test@gmail.com", ORCID,
        "https://ror.org/XXXXXXXXX");
  }

  // General
  public static JsonApiLinksFull givenJsonApiLinksFull(String path, int pageNumber, int pageSize,
      boolean hasNextPage) {
    return new JsonApiLinksFull(pageNumber, pageSize, hasNextPage, path);
  }

  // Digital Specimen
  public static DigitalSpecimenWrapper givenDigitalSpecimenWrapper(String id)
      throws JsonProcessingException {
    return givenDigitalSpecimenWrapper(id, "global_id_123123");
  }

  public static DigitalSpecimenWrapper givenDigitalSpecimenSourceSystem(String id,
      String sourceSystem)
      throws JsonProcessingException {
    return givenDigitalSpecimenWrapper(id, "global_id_123123", 1, sourceSystem);
  }

  public static DigitalSpecimenWrapper givenDigitalSpecimenWrapper(String id, String physicalId)
      throws JsonProcessingException {
    return givenDigitalSpecimenWrapper(id, physicalId, 1, SOURCE_SYSTEM_ID_1);
  }

  public static DigitalSpecimenWrapper givenDigitalSpecimenWrapper(String id, String physicalId,
      String sourceSystem)
      throws JsonProcessingException {
    return givenDigitalSpecimenWrapper(id, physicalId, 1, sourceSystem);
  }

  public static DigitalSpecimenWrapper givenDigitalSpecimenWrapper(String id, String physicalId,
      Integer version,
      String sourceSystemId)
      throws JsonProcessingException {
    return new DigitalSpecimenWrapper(
        givenDigitalSpecimen(id, physicalId, version, sourceSystemId),
        givenSpecimenOriginalData());
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

  private static DigitalSpecimen givenDigitalSpecimen(String id, String physicalId, Integer version,
      String sourceSystemId) {
    return new DigitalSpecimen()
        .withOdsId(id)
        .withOdsPhysicalSpecimenId(physicalId)
        .withOdsVersion(version)
        .withOdsMidsLevel(0)
        .withOdsCreated(CREATED.toString())
        .withOdsType(DIGITAL_SPECIMEN_TYPE)
        .withOdsTopicDiscipline(OdsTopicDiscipline.BOTANY)
        .withDctermsModified("03/12/2012")
        .withDwcDatasetName("Royal Botanic Garden Edinburgh Herbarium")
        .withDwcPreparations("")
        .withDctermsLicense("http://creativecommons.org/licenses/by/4.0/legalcode")
        .withOdsSpecimenName("Leucanthemum ircutianum (Turcz.) Turcz.ex DC.")
        .withDwcInstitutionId("https://ror.org/0349vqz63")
        .withDwcInstitutionName("Royal Botanic Garden Edinburgh Herbarium")
        .withOdsSourceSystem(sourceSystemId)
        .withOdsPhysicalSpecimenIdType(OdsPhysicalSpecimenIdType.RESOLVABLE)
        .withOdsMarkedAsType(Boolean.TRUE)
        .withOdsHasMedia(Boolean.TRUE)
        .withOccurrences(
            List.of(new Occurrences().withLocation(new Location().withDwcCountry("Scotland"))));
  }

  public static Map<String, Map<String, Long>> givenAggregationMap() {
    return Map.of(
        "sourceSystem", Map.of(SOURCE_SYSTEM_ID_1, 5L, SOURCE_SYSTEM_ID_2, 5L),
        "typeStatus", Map.of("type", 10L),
        "hasMedia", Map.of("true", 10L)
    );
  }
}
