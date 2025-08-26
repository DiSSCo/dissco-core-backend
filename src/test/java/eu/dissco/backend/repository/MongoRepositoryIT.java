package eu.dissco.backend.repository;

import static eu.dissco.backend.TestUtils.DOI;
import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.repository.MongoRepository.ODS_VERSION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
    var result = repository.getByVersion(DOI + ID, 4, "digital_specimen_provenance");

    // Then
    assertThat(result).isEqualTo(expected.get("prov:Entity").get("prov:value"));
  }

  @Test
  void testGetVersionNotFound() {
    // Given

    // When
    var exception = assertThrowsExactly(NotFoundException.class,
        () -> repository.getByVersion(DOI + ID, 2, "digital_specimen_provenance"));

    // Then
    assertThat(exception).isInstanceOf(NotFoundException.class);
  }


  @Test
  void testGetVersions() throws JsonProcessingException, NotFoundException {
    // Given
    populateMongoDB();
    var expected = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

    // When
    var result = repository.getVersions("https://doi.org/20.5000.1025/4AF-E6L-9VQ",
        "digital_specimen_provenance", ODS_VERSION);

    // Then
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void testGetVersionsNotFound() {
    // Given

    // When
    var exception = assertThrowsExactly(NotFoundException.class,
        () -> repository.getVersions(DOI + ID, "digital_specimen_provenance", ODS_VERSION));

    // Then
    assertThat(exception).isInstanceOf(NotFoundException.class);
  }

  private ObjectNode givenExpectedSpecimen(int version) throws JsonProcessingException {
    return MAPPER.readValue(
        """
            {
              "_id": "https://doi.org/20.5000.1025/4AF-E6L-9VQ/""" + version + "\", \n" +
            """
                "@id": "https://doi.org/20.5000.1025/4AF-E6L-9VQ//1",
                "@type": "ods:CreateUpdateTombstoneEvent",
                "dcterms:identifier": "https://doi.org/20.5000.1025/4AF-E6L-9VQ//1",
                "ods:fdoType": "https://doi.org/10.15468/1a2b3c",
                "prov:Activity": {
                  "@id": "7ba628d4-2e28-4ce4-ad1e-e99c97c20507",
                  "@type": "ods:Create",
                  "prov:wasAssociatedWith": [
                    {
                      "@id": "https://orcid.org/0000-0002-1825-0097",
                      "prov:hadRole": "ods:Approver"
                    },
                    {
                      "@id": "https://hdl.handle.net/20.5000.1025/XXX-XXX-XXX",
                      "prov:hadRole": "ods:Requestor"
                    },
                    {
                      "@id": "https://hdl.handle.net/20.5000.1025/XXX-YYY-XXX",
                      "prov:hadRole": "ods:Generator"
                    }
                  ],
                  "prov:endedAtTime": "2024-06-11T09:14:00.348Z",
                  "prov:used": "https://doi.org/20.5000.1025/4AF-E6L-9VQ//1",
                  "rdfs:comment": "This activity was created by the user",
                  "ods:changeValue": []
                },
                "prov:Entity": {
                  "@id": "https://doi.org/20.5000.1025/4AF-E6L-9VQ/1",
                  "@type": "ods:DataMapping",
                  "prov:value": {
                    "@id": "https://doi.org/20.5000.1025/4AF-E6L-9VQ",
                    "@type": "ods:DigitalSpecimen",
                    "dcterms:identifier": "https://doi.org/20.5000.1025/4AF-E6L-9VQ",
                    "ods:fdoType": "https://doi.org/10.15468/1a2b3c",
                    "ods:version": """ + version + " \n" +
            """
                     },
                     "prov:wasGeneratedBy": "7ba628d4-2e28-4ce4-ad1e-e99c97c20507"
                   },
                   "ods:hasProvAgent": [
                     {
                       "@id": "https://orcid.org/0000-0002-1825-0097",
                       "@type": "prov:Person",
                       "schema:name": "John Doe"
                     },
                     {
                       "@id": "https://hdl.handle.net/20.5000.1025/XXX-XXX-XXX",
                       "@type": "prov:SoftwareAgent",
                       "schema:name": "GBIF Linker Service"
                     },
                     {
                       "@id": "https://hdl.handle.net/20.5000.1025/XXX-YYY-XXX",
                       "@type": "prov:SoftwareAgent",
                       "schema:name": "Digital Specimen Processor"
                     }
                   ]
                 }
                """, ObjectNode.class
    );
  }

  private void populateMongoDB() throws JsonProcessingException {
    var collection = database.getCollection("digital_specimen_provenance");
    for (int i = 1; i < 11; i++) {
      var specimen = givenExpectedSpecimen(i);
      var versionId = DOI + ID + '/' + i;
      var document = Document.parse(MAPPER.writeValueAsString(specimen));
      document.append("_id", versionId);
      collection.insertOne(document);
    }

  }

}
