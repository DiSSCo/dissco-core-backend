package eu.dissco.backend.repository;

import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.MAPPER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import eu.dissco.backend.exceptions.NotFoundException;
import java.util.List;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
class MongoRepositoryIT {

  private static final DockerImageName MONGODB =
      DockerImageName.parse("mongo:6.0.4");

  @Container
  private static final MongoDBContainer CONTAINER = new MongoDBContainer(MONGODB);
  private MongoDatabase database;
  private MongoClient client;
  private MongoRepository repository;

  @BeforeEach
  void prepareDocumentStore() {
    client = MongoClients.create(CONTAINER.getConnectionString());
    database = client.getDatabase("dissco");
    repository = new MongoRepository(database, MAPPER);
  }

  @AfterEach
  void disposeDocumentStore() {
    database.drop();
    client.close();
  }

  @Test
  void testGetVersion() throws JsonProcessingException, NotFoundException {
    // Given
    populateMongoDB();
    var expected = givenExpectedSpecimen(4);

    // When
    var result = repository.getByVersion(ID, 4, "digital_specimen_provenance");

    // Then
    assertThat(result).isEqualTo(expected.get("eventRecord"));
  }

  @Test
  void testGetVersionNotFound() {
    // Given

    // When
    var exception = assertThrowsExactly(NotFoundException.class,
        () -> repository.getByVersion(ID, 2, "digital_specimen_provenance"));

    // Then
    assertThat(exception).isInstanceOf(NotFoundException.class);
  }


  @Test
  void testGetVersions() throws JsonProcessingException, NotFoundException {
    // Given
    populateMongoDB();
    var expected = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

    // When
    var result = repository.getVersions("20.5000.1025/4AF-E6L-9VQ", "digital_specimen_provenance");

    // Then
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void testGetVersionsNotFound() {
    // Given

    // When
    var exception = assertThrowsExactly(NotFoundException.class,
        () -> repository.getVersions(ID, "digital_specimen_provenance"));

    // Then
    assertThat(exception).isInstanceOf(NotFoundException.class);
  }

  private JsonNode givenExpectedSpecimen(int version) throws JsonProcessingException {
    return MAPPER.readValue(
        """
            {
              "_id": "20.5000.1025/4AF-E6L-9VQ/""" + version + "\", \n" +
            """
                "id": "d87a0aa9-2487-4573-a8eb-5b77a57bac3f",
                "eventType": "create",
                "agent": "digital-specimen-processing-service",
                "subject": "20.5000.1025/4AF-E6L-9VQ",
                "subjectType": "DigitalSpecimen",
                "timestamp": "2023-03-02T13:14:04.022893674Z",
                "eventRecord": {
                  "id": "20.5000.1025/4AF-E6L-9VQ",
                  "midsLevel": 0,
                  "version": """ + version + ", \n" +
            """
                       "created": 1677762844.0073225,
                       "digitalSpecimen": {
                         "ods:physicalSpecimenId": "https://geocollections.info/specimen/358109",
                         "ods:type": "GeologyRockSpecimen",
                         "ods:attributes": {
                           "ods:physicalSpecimenIdType": "cetaf",
                           "ods:organisationId": "https://ror.org/0443cwa12",
                           "ods:sourceSystemId": "20.5000.1025/MN0-5XP-FFD",
                           "dwca:id": null,
                           "dcterms:license": "http://creativecommons.org/licenses/by-nc/4.0/",
                           "ods:specimenName": "Trypanites solitaria",
                           "ods:physicalSpecimenCollection": null,
                           "ods:datasetId": null,
                           "ods:objectType": "single specimen",
                           "ods:modified": null,
                           "ods:dateCollected": null,
                           "ods:collectingNumber": null,
                           "ods:collector": "Toom, Ursula",
                           "dwc:typeStatus": "null | null | null",
                           "dwc:continent": null,
                           "dwc:country": "Estonia",
                           "dwc:countryCode": "EE",
                           "dwc:county": null,
                           "dwc:decimalLatitude": "59.178075",
                           "dwc:decimalLongitude": "24.619181",
                           "dwc:geodeticDatum": "WGS84",
                           "dwc:island": null,
                           "dwc:islandGroup": null,
                           "dwc:locality": "Sutlema old quarry",
                           "dwc:stateProvince": null,
                           "dwc:waterBody": null
                         },
                         "ods:originalAttributes": {
                           "abcd:unitGUID": "https://geocollections.info/specimen/358109",
                           "abcd:sourceInstitutionID": "Department of Geology, TalTech",
                           "abcd:sourceID": "GIT",
                           "abcd:unitID": "881-9-3",
                           "abcd:unitIDNumeric": 358109,
                           "abcd:identifications/identification/0/result/taxonIdentified/higherTaxa/higherTaxon/0/higherTaxonName": "Ichnofossils",
                           "abcd:identifications/identification/0/result/taxonIdentified/higherTaxa/higherTaxon/0/higherTaxonRank": "Ichnofossil group",
                           "abcd:identifications/identification/0/result/taxonIdentified/scientificName/fullScientificNameString": "Trypanites solitaria",
                           "abcd:identifications/identification/0/result/taxonIdentified/scientificName/nameAtomised/zoological/speciesEpithet": "solitaria",
                           "abcd:identifications/identification/0/preferredFlag": true,
                           "abcd:recordBasis": "FossilSpecimen",
                           "abcd:kindOfUnit/0/value": "single specimen",
                           "abcd:kindOfUnit/0/language": "en",
                           "abcd:gathering/dateTime/isodateTimeBegin": "2022-08-28",
                           "abcd:gathering/agents/gatheringAgent/0/agentText": "Toom, Ursula",
                           "abcd:gathering/agents/gatheringAgent/0/person/fullName": "Toom, Ursula",
                           "abcd:gathering/agents/gatheringAgent/0/person/atomisedName/inheritedName": "Toom",
                           "abcd:gathering/agents/gatheringAgent/0/person/atomisedName/givenNames": "Ursula",
                           "abcd:gathering/localityText/value": "Sutlema old quarry",
                           "abcd:gathering/localityText/language": "en",
                           "abcd:gathering/country/name/value": "Estonia",
                           "abcd:gathering/country/iso3166Code": "EE",
                           "abcd:gathering/siteCoordinateSets/siteCoordinates/0/coordinateMethod": "Est Land Board map server",
                           "abcd:gathering/siteCoordinateSets/siteCoordinates/0/coordinatesLatLong/longitudeDecimal": 24.619181,
                           "abcd:gathering/siteCoordinateSets/siteCoordinates/0/coordinatesLatLong/latitudeDecimal": 59.178075,
                           "abcd:gathering/siteCoordinateSets/siteCoordinates/0/coordinatesLatLong/spatialDatum": "WGS84",
                           "abcd:gathering/siteCoordinateSets/siteCoordinates/0/coordinatesLatLong/accuracyStatement": "100-1000 m",
                           "abcd:gathering/siteCoordinateSets/siteCoordinates/0/coordinatesLatLong/coordinateErrorDistanceInMeters": 1000,
                           "abcd:gathering/stratigraphy/stratigraphyText/value": "Nabala/Vormsi Boundary Bed",
                           "abcd:recordURI": "https://geocollections.info/specimen/358109"
                         }
                       }
                     },
                     "change": null,
                     "comment": "Specimen newly created"
                   }
                """, JsonNode.class
    );
  }

  private void populateMongoDB() throws JsonProcessingException {
    var collection = database.getCollection("digital_specimen_provenance");
    for (int i = 1; i < 11; i++) {
      var specimen = givenExpectedSpecimen(i);
      var versionId = ID + '/' + i;
      var document = Document.parse(MAPPER.writeValueAsString(specimen));
      document.append("_id", versionId);
      collection.insertOne(document);
    }

  }

}
