package eu.dissco.backend.repository;

import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.givenDigitalSpecimen;
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
    var expected = givenExpectedSpecimen();

    // When
    var result = repository.getByVersion(ID, 4, "digital_specimen_provenance");

    // Then
    assertThat(result).isEqualTo(expected);
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
    var result = repository.getVersions(ID, "digital_specimen_provenance");

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

  private JsonNode givenExpectedSpecimen() throws JsonProcessingException {
    return MAPPER.readValue(
        """
            {
              "_id": "20.5000.1025/ABC-123-XYZ/4",
              "id": "20.5000.1025/ABC-123-XYZ",
              "midsLevel": 1,
              "version": 4,
              "created": 1.667296764E9,
              "type": "BotanySpecimen",
              "physicalSpecimenId": "123",
              "physicalSpecimenIdType": "cetaf",
              "specimenName": "Leucanthemum ircutianum (Turcz.) Turcz.ex DC.",
              "organizationId": "https://ror.org/0349vqz63",
              "datasetId": "Royal Botanic Garden Edinburgh Herbarium",
              "physicalSpecimenCollection": "http://biocol.org/urn:lsid:biocol.org:col:15670",
              "sourceSystemId": "20.5000.1025/3XA-8PT-SAY",
              "data": {
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
              },
              "originalData": {
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
              },
              "dwcaId": "http://data.rbge.org.uk/herb/E00586417"
            }
            """, JsonNode.class
    );
  }

  private void populateMongoDB() throws JsonProcessingException {
    var collection = database.getCollection("digital_specimen_provenance");
    for (int i = 1; i < 11; i++) {
      var specimen = givenDigitalSpecimen(ID, i);
      var versionId = ID + '/' + i;
      var document = Document.parse(MAPPER.writeValueAsString(specimen));
      document.append("_id", versionId);
      collection.insertOne(document);
    }

  }

}
