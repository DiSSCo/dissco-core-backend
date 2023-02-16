package eu.dissco.backend.repository;


import static eu.dissco.backend.TestUtils.CREATED;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.PREFIX;
import static eu.dissco.backend.TestUtils.USER_ID_TOKEN;
import static eu.dissco.backend.TestUtils.givenAnnotationResponse;
import static eu.dissco.backend.TestUtils.givenDigitalSpecimen;
import static org.assertj.core.api.Assertions.assertThat;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import eu.dissco.backend.domain.AnnotationResponse;
import eu.dissco.backend.domain.DigitalSpecimen;
import eu.dissco.backend.domain.JsonApiData;
import eu.dissco.backend.repository.ElasticSearchTestRecords.AnnotationTestRecord;
import eu.dissco.backend.repository.ElasticSearchTestRecords.DigitalSpecimenTestRecord;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
class ElasticSearchRepositoryIT {

  private static final DockerImageName ELASTIC_IMAGE = DockerImageName.parse(
      "docker.elastic.co/elasticsearch/elasticsearch").withTag("8.6.1");
  private static final String INDEX = "new-dissco";
  private static final String ELASTICSEARCH_USERNAME = "elastic";
  private static final String ELASTICSEARCH_PASSWORD = "s3cret";
  private static final String CREATED_ALT = "2022-09-02T09:59:24Z";
  private static final ElasticsearchContainer container = new ElasticsearchContainer(
      ELASTIC_IMAGE).withExposedPorts(9200).withPassword(ELASTICSEARCH_PASSWORD);
  private static ElasticsearchClient client;
  private static RestClient restClient;
  private ElasticSearchRepository repository;

  @BeforeAll
  static void initContainer() {
    // Create the elasticsearch container.
    container.start();

    final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    credentialsProvider.setCredentials(AuthScope.ANY,
        new UsernamePasswordCredentials(ELASTICSEARCH_USERNAME, ELASTICSEARCH_PASSWORD));

    restClient = RestClient.builder(HttpHost.create(container.getHttpHostAddress()))
        .setHttpClientConfigCallback(httpClientBuilder -> {
          return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        }).build();

    ElasticsearchTransport transport = new RestClientTransport(restClient,
        new JacksonJsonpMapper(MAPPER));

    client = new ElasticsearchClient(transport);
  }

  @BeforeEach
  void initRepository() {
    repository = new ElasticSearchRepository(client);
  }

  @AfterEach
  void clearIndex() throws IOException {
    client.indices().delete(b -> b.index(INDEX));
  }

  @AfterAll
  public static void closeResources() throws Exception {
    restClient.close();
  }

  @Test
  void testSearch() throws IOException {
    // Given
    List<DigitalSpecimenTestRecord> specimenTestRecords = new ArrayList<>();
    String targetId = PREFIX + "/0";
    var targetSpecimen = givenDigitalSpecimen(targetId);

    for (int i = 0; i < 10; i++) {
      var specimen = givenDigitalSpecimen(PREFIX + "/" + i);
      specimenTestRecords.add(givenDigitalSpecimenTestRecord(specimen));
    }
    postDigitalSpecimens(specimenTestRecords);

    // When
    var responseReceived = repository.search(targetId, 1, 1);

    // Then
    assertThat(responseReceived).contains(targetSpecimen);
  }

  @Test
  void testSearchSecondPage() throws IOException {
    // Given
    int pageNumber = 2;
    int pageSize = 5;
    List<DigitalSpecimenTestRecord> specimenTestRecords = new ArrayList<>();

    for (int i = 0; i < 10; i++) {
      var specimen = givenDigitalSpecimen(PREFIX + "/" + i);
      specimenTestRecords.add(givenDigitalSpecimenTestRecord(specimen));
    }
    postDigitalSpecimens(specimenTestRecords);

    // When
    var responseReceived = repository.search(PREFIX, pageNumber, pageSize);

    // Then
    assertThat(responseReceived).hasSize(pageSize);
  }


  @Test
  void testGetLatestSpecimen() throws IOException {
    // Given
    int pageSize = 10;
    int pageNumber = 1;
    List<DigitalSpecimenTestRecord> specimenTestRecordsLatest = new ArrayList<>();
    List<DigitalSpecimenTestRecord> specimenTestRecordsOlder = new ArrayList<>();
    List<DigitalSpecimen> responseExpected = new ArrayList<>();
    for (int i = 0; i < pageSize; i++) {
      var specimen = givenDigitalSpecimen(PREFIX + "/" + i);
      responseExpected.add(specimen);
      specimenTestRecordsLatest.add(givenDigitalSpecimenTestRecord(specimen));
    }
    for (int i = pageSize; i < pageSize * 2; i++) {
      var specimen = givenDigitalSpecimen(PREFIX + "/" + i);
      specimenTestRecordsOlder.add(givenOlderDigitalSpecimenTestRecord(specimen));
    }
    List<DigitalSpecimenTestRecord> specimenTestRecords = new ArrayList<>();
    specimenTestRecords.addAll(specimenTestRecordsLatest);
    specimenTestRecords.addAll(specimenTestRecordsOlder);
    var responsePost = postDigitalSpecimens(specimenTestRecords);

    // When
    var responseReceived = repository.getLatestSpecimen(pageNumber, pageSize);

    // Then
    assertThat(responseReceived).hasSize(pageSize).hasSameElementsAs(responseExpected);
  }

  @Test
  void testGetLatestSpecimenSecondPage() throws IOException {
    // Given
    int pageSize = 10;
    int pageNumber = 2;
    List<DigitalSpecimenTestRecord> specimenTestRecordsLatest = new ArrayList<>();
    List<DigitalSpecimenTestRecord> specimenTestRecordsOlder = new ArrayList<>();
    List<DigitalSpecimen> responseExpected = new ArrayList<>();
    for (int i = 0; i < pageSize; i++) {
      var specimen = givenDigitalSpecimen(PREFIX + "/" + i);
      specimenTestRecordsLatest.add(givenDigitalSpecimenTestRecord(specimen));
    }
    for (int i = pageSize; i < pageSize * 2; i++) {
      var specimen = givenOlderSpecimen(PREFIX + "/" + i);
      responseExpected.add(specimen);
      specimenTestRecordsOlder.add(givenOlderDigitalSpecimenTestRecord(specimen));
    }
    List<DigitalSpecimenTestRecord> specimenTestRecords = new ArrayList<>();
    specimenTestRecords.addAll(specimenTestRecordsLatest);
    specimenTestRecords.addAll(specimenTestRecordsOlder);
    postDigitalSpecimens(specimenTestRecords);

    // When
    var responseReceived = repository.getLatestSpecimen(pageNumber, pageSize);
    var allElems = repository.getLatestAnnotations(1, pageSize * 2);

    // Then
    assertThat(responseReceived).hasSize(pageSize).hasSameElementsAs(responseExpected);
  }

  @Test
  void testGetLatestAnnotations() throws IOException {
    // Given
    int pageNumber = 1;
    int pageSize = 10;
    List<AnnotationTestRecord> annotationTestRecordsLatest = new ArrayList<>();
    List<AnnotationTestRecord> annotationTestRecordsOlder = new ArrayList<>();
    List<AnnotationResponse> responseExpected = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      var annotation = givenAnnotationResponse(USER_ID_TOKEN, PREFIX + "/" + i);
      responseExpected.add(annotation);
      annotationTestRecordsLatest.add(givenAnnotationTestRecord(annotation));
    }
    for (int i = 11; i < 15; i++) {
      var annotation = givenAnnotationResponse(USER_ID_TOKEN, PREFIX + "/" + i);
      annotationTestRecordsOlder.add(givenOlderAnnotationTestRecord(annotation));
    }
    List<AnnotationTestRecord> annotationTestRecords = new ArrayList<>();
    annotationTestRecords.addAll(annotationTestRecordsLatest);
    annotationTestRecords.addAll(annotationTestRecordsOlder);
    var responsePost = postAnnotations(annotationTestRecords);

    // When
    var responseReceived = repository.getLatestAnnotations(pageNumber, pageSize);

    // Then
    assertThat(responseReceived).hasSize(pageSize).hasSameElementsAs(responseExpected);
  }

  @Test
  void testGetLatestAnnotationsJsonResponse() throws IOException {
    // Given
    int pageNumber = 1;
    int pageSize = 10;
    List<AnnotationTestRecord> annotationTestRecordsLatest = new ArrayList<>();
    List<AnnotationTestRecord> annotationTestRecordsOlder = new ArrayList<>();
    List<JsonApiData> responseExpected = new ArrayList<>();
    for (int i = 0; i < pageSize; i++) {
      String id = PREFIX + "/" + i;
      var annotation = givenAnnotationResponse(USER_ID_TOKEN, id);
      responseExpected.add(givenAnnotationJsonApiData(id));
      annotationTestRecordsLatest.add(givenAnnotationTestRecord(annotation));
    }
    for (int i = 11; i < pageSize * 2; i++) {
      var annotation = givenAnnotationResponse(USER_ID_TOKEN, PREFIX + "/" + i);
      annotationTestRecordsOlder.add(givenOlderAnnotationTestRecord(annotation));
    }
    List<AnnotationTestRecord> annotationTestRecords = new ArrayList<>();
    annotationTestRecords.addAll(annotationTestRecordsLatest);
    annotationTestRecords.addAll(annotationTestRecordsOlder);
    var responsePost = postAnnotations(annotationTestRecords);

    // When
    var responseReceived = repository.getLatestAnnotationsJsonResponse(pageNumber, pageSize);

    // Then
    assertThat(responseReceived).hasSize(pageSize).hasSameElementsAs(responseExpected);
  }

  @Test
  void testGetLatestAnnotationsJsonResponseSecondPage() throws IOException {
    // Given
    int pageNumber = 2;
    int pageSize = 10;
    List<AnnotationTestRecord> annotationTestRecordsLatest = new ArrayList<>();
    List<AnnotationTestRecord> annotationTestRecordsOlder = new ArrayList<>();
    List<JsonApiData> responseExpected = new ArrayList<>();
    for (int i = 0; i < pageSize; i++) {
      String id = PREFIX + "/" + i;
      var annotation = givenAnnotationResponse(USER_ID_TOKEN, id);
      annotationTestRecordsLatest.add(givenAnnotationTestRecord(annotation));
    }
    for (int i = pageSize; i < pageSize * 2; i++) {
      String id = PREFIX + "/" + i;
      responseExpected.add(givenAnnotationJsonApiData(id));
      var annotation = givenAnnotationResponse(USER_ID_TOKEN, id);
      annotationTestRecordsOlder.add(givenOlderAnnotationTestRecord(annotation));
    }
    List<AnnotationTestRecord> annotationTestRecords = new ArrayList<>();
    annotationTestRecords.addAll(annotationTestRecordsLatest);
    annotationTestRecords.addAll(annotationTestRecordsOlder);
    var responsePost = postAnnotations(annotationTestRecords);

    // When
    var responseReceived = repository.getLatestAnnotationsJsonResponse(pageNumber, pageSize);

    // Then
    assertThat(responseReceived).hasSize(pageSize).hasSameElementsAs(responseExpected);
  }

  private DigitalSpecimenTestRecord givenDigitalSpecimenTestRecord(DigitalSpecimen specimen) {
    return new DigitalSpecimenTestRecord(specimen.id(), 1, 1, CREATED, specimen);
  }

  private DigitalSpecimenTestRecord givenOlderDigitalSpecimenTestRecord(DigitalSpecimen specimen) {
    Instant created = Instant.parse("2022-09-02T09:59:24Z");
    return new DigitalSpecimenTestRecord(specimen.id(), 1, 1, created, specimen);
  }

  private DigitalSpecimen givenOlderSpecimen(String id) {
    var spec = givenDigitalSpecimen(id);
    return new DigitalSpecimen(spec.id(), spec.midsLevel(), spec.version(),
        Instant.parse(CREATED_ALT), spec.type(), spec.physicalSpecimenId(),
        spec.physicalSpecimenIdType(), spec.specimenName(), spec.organizationId(), spec.datasetId(),
        spec.physicalSpecimenCollection(), spec.sourceSystemId(), spec.data(), spec.originalData(),
        spec.dwcaId());
  }

  public BulkResponse postDigitalSpecimens(Collection<DigitalSpecimenTestRecord> digitalSpecimens)
      throws IOException {
    var bulkRequest = new BulkRequest.Builder();
    for (var digitalSpecimen : digitalSpecimens) {
      bulkRequest.operations(op -> op.index(
          idx -> idx.index(INDEX).id(digitalSpecimen.id()).document(digitalSpecimen)));
    }
    var response = client.bulk(bulkRequest.build());
    client.indices().refresh(b -> b.index(INDEX));
    return response;
  }


  private AnnotationTestRecord givenAnnotationTestRecord(AnnotationResponse annotation) {
    return new AnnotationTestRecord(annotation.id(), annotation.version(), CREATED, annotation);
  }

  private AnnotationTestRecord givenOlderAnnotationTestRecord(AnnotationResponse annotation) {
    Instant created = Instant.parse(CREATED_ALT);
    return new AnnotationTestRecord(annotation.id(), annotation.version(), created, annotation);
  }

  private BulkResponse postAnnotations(List<AnnotationTestRecord> annotations) throws IOException {
    var bulkRequest = new BulkRequest.Builder();
    for (var annotation : annotations) {
      bulkRequest.operations(
          op -> op.index(idx -> idx.index(INDEX).id(annotation.id()).document(annotation)));
    }
    var response = client.bulk(bulkRequest.build());
    client.indices().refresh(b -> b.index(INDEX));
    return response;
  }

  private static JsonApiData givenAnnotationJsonApiData(String annotationId) {
    ObjectMapper mapper = new ObjectMapper().findAndRegisterModules()
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    mapper.setSerializationInclusion(Include.NON_NULL);

    ObjectNode dataNode = mapper.valueToTree(givenAnnotationResponse(USER_ID_TOKEN, annotationId));
    return new JsonApiData(annotationId, "Annotation", dataNode);
  }


}

