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
    var result = repository.getVersion(ID, 4, "digital_specimen_provenance");

    // Then
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void testGetVersionNotFound() {
    // Given

    // When
    var exception = assertThrowsExactly(NotFoundException.class,
        () -> repository.getVersion(ID, 2, "digital_specimen_provenance"));

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
                "dcterms:title": "19942272",
                "dcterms:publisher": "Royal Botanic Garden Edinburgh"
              },
              "originalData": {
                "dcterms:title": "19942272",
                "dcterms:type": "StillImage"
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
