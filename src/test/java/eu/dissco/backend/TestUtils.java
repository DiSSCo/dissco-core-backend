package eu.dissco.backend;


import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.domain.jsonapi.JsonApiLinksFull;
import eu.dissco.backend.schema.Agent;
import eu.dissco.backend.schema.Agent.Type;
import eu.dissco.backend.schema.DigitalSpecimen;
import eu.dissco.backend.schema.DigitalSpecimen.OdsPhysicalSpecimenIDType;
import eu.dissco.backend.schema.Event;
import eu.dissco.backend.schema.Identifier.DctermsType;
import eu.dissco.backend.schema.Identifier.OdsGupriLevel;
import eu.dissco.backend.schema.Identifier.OdsIdentifierStatus;
import eu.dissco.backend.schema.Location;
import eu.dissco.backend.schema.OdsHasRole;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
  public static final String TARGET_ID = HANDLE + PREFIX + "/TAR-GET-001";
  public static final String SOURCE_SYSTEM_ID_1 = HANDLE + "20.5000.1025/3XA-8PT-SAY";
  public static final String SOURCE_SYSTEM_ID_2 = HANDLE + "20.5000.1025/ANO-THE-RAY";
  public static final String MAS_ID = HANDLE + "20.5000.1025/ABC-123-XYZ";
  public static final String PHYSICAL_ID = "global_id_123123";

  public static final String SPECIMEN_NAME = "Abyssothyris Thomson, 1927";
  public static final String SPECIMEN_NAME_2 = "Aackia Yosii, 1966";

  public static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();
  public static final Instant CREATED = Instant.parse("2022-11-01T09:59:24.00Z");
  public static final String SANDBOX_URI = "https://sandbox.dissco.tech";
  public static final String ORCID = "https://orcid.org/0000-0002-XXXX-XXXX";
  public static final UUID BATCH_ID = UUID.fromString("f43e4ec6-ca1c-4a88-9aac-08f6da4b0b1c");

  public static final String DIGITAL_SPECIMEN_TYPE = "https://doi.org/21.T11148/894b1e6cad57e921764e";

  // Users
  public static Agent givenAgent() {
    return new Agent()
        .withType(Type.SCHEMA_PERSON)
        .withSchemaName("Sam Leeflang")
        .withSchemaIdentifier(ORCID)
        .withId(ORCID)
        .withOdsHasRoles(List.of(new OdsHasRole().withType("schema:Role")
            .withSchemaRoleName("annotator")))
        .withOdsHasIdentifiers(List.of(
            new eu.dissco.backend.schema.Identifier()
                .withType("ods:Identifier")
                .withId(ORCID)
                .withDctermsIdentifier(ORCID)
                .withOdsIsPartOfLabel(false)
                .withOdsIdentifierStatus(OdsIdentifierStatus.PREFERRED)
                .withOdsGupriLevel(OdsGupriLevel.GLOBALLY_UNIQUE_STABLE_PERSISTENT_RESOLVABLE)
                .withDctermsType(DctermsType.URL)
                .withDctermsTitle("orcid")
        ));
  }

  // Token
  public static Map<String, Object> givenClaims() {
    return Map.of(
        "orcid", ORCID,
        "given_name", "Sam",
        "family_name", "Leeflang"
    );
  }

  // General
  public static JsonApiLinksFull givenJsonApiLinksFull(String path, int pageNumber, int pageSize,
      boolean hasNextPage) {
    return new JsonApiLinksFull(pageNumber, pageSize, hasNextPage, path);
  }

  // Digital Specimen
  public static DigitalSpecimen givenDigitalSpecimenWrapper(String id) {
    return givenDigitalSpecimenWrapper(id, PHYSICAL_ID);
  }

  public static DigitalSpecimen givenDigitalSpecimenSourceSystem(String id,
      String sourceSystem) {
    return givenDigitalSpecimenWrapper(id, PHYSICAL_ID, 1, sourceSystem, SPECIMEN_NAME);
  }

  public static DigitalSpecimen givenDigitalSpecimenWrapper(String id, String physicalId) {
    return givenDigitalSpecimenWrapper(id, physicalId, 1, SOURCE_SYSTEM_ID_1, SPECIMEN_NAME);
  }

  public static DigitalSpecimen givenDigitalSpecimenWrapper(String id, String physicalId,
      String sourceSystem) {
    return givenDigitalSpecimenWrapper(id, physicalId, 1, sourceSystem, SPECIMEN_NAME);
  }

  public static DigitalSpecimen givenDigitalSpecimenSpecimenName(String id, String specimenName) {
    return givenDigitalSpecimenWrapper(id, PHYSICAL_ID, 1, SOURCE_SYSTEM_ID_1, specimenName);
  }

  public static DigitalSpecimen givenDigitalSpecimenWrapper(String id, String physicalId,
      Integer version, String sourceSystemId, String specimenName) {
    return givenDigitalSpecimen(id, physicalId, version, sourceSystemId, specimenName);
  }

  private static DigitalSpecimen givenDigitalSpecimen(String id, String physicalId, Integer version,
      String sourceSystemId, String specimenName, String country) {
    return new eu.dissco.backend.schema.DigitalSpecimen()
        .withId(id)
        .withType("ods:DigitalSpecimen")
        .withDctermsIdentifier(id)
        .withOdsPhysicalSpecimenID(physicalId)
        .withOdsVersion(version)
        .withOdsMidsLevel(0)
        .withDctermsCreated(Date.from(CREATED))
        .withOdsFdoType(DIGITAL_SPECIMEN_TYPE)
        .withDctermsModified("03/12/2012")
        .withDwcDatasetName("Royal Botanic Garden Edinburgh Herbarium")
        .withDwcPreparations("")
        .withDctermsLicense("http://creativecommons.org/licenses/by/4.0/legalcode")
        .withOdsSpecimenName(specimenName)
        .withOdsOrganisationID("https://ror.org/0349vqz63")
        .withOdsOrganisationName("Royal Botanic Garden Edinburgh Herbarium")
        .withOdsSourceSystemID(sourceSystemId)
        .withOdsPhysicalSpecimenIDType(OdsPhysicalSpecimenIDType.RESOLVABLE)
        .withOdsIsMarkedAsType(Boolean.TRUE)
        .withOdsIsKnownToContainMedia(Boolean.TRUE)
        .withOdsHasEvents(
            List.of(new Event().withOdsHasLocation(new Location().withDwcCountry(country))));
  }

  private static DigitalSpecimen givenDigitalSpecimen(String id, String physicalId, Integer version,
      String sourceSystemId, String specimenName) {
    return givenDigitalSpecimen(id, physicalId, version, sourceSystemId, specimenName, "Scotland");
  }


  public static DigitalSpecimen givenDigitalSpecimenAltCountry(String id) {
    return givenDigitalSpecimen(id, PHYSICAL_ID, 1, SOURCE_SYSTEM_ID_1, "Alt Country Specimen",
        "Netherlands");
  }


  public static Map<String, Map<String, Long>> givenAggregationMap() {
    return Map.of(
        "sourceSystem", Map.of(SOURCE_SYSTEM_ID_1, 5L, SOURCE_SYSTEM_ID_2, 5L),
        "typeStatus", Map.of("type", 10L),
        "hasMedia", Map.of("true", 10L)
    );
  }

  public static Map<String, Map<String, Long>> givenTaxonAggregationMap() {
    return Map.of(
        "phylum", Map.of("Chordata", 3782L)
    );
  }
}
