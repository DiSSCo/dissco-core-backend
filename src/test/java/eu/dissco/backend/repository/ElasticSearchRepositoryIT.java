package eu.dissco.backend.repository;


import static eu.dissco.backend.TestUtils.DOI;
import static eu.dissco.backend.TestUtils.HANDLE;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.PREFIX;
import static eu.dissco.backend.TestUtils.SOURCE_SYSTEM_ID_1;
import static eu.dissco.backend.TestUtils.SOURCE_SYSTEM_ID_2;
import static eu.dissco.backend.TestUtils.USER_ID_TOKEN;
import static eu.dissco.backend.TestUtils.givenDigitalSpecimenSourceSystem;
import static eu.dissco.backend.TestUtils.givenDigitalSpecimenWrapper;
import static eu.dissco.backend.domain.MappingTerms.SOURCE_SYSTEM_ID;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationResponse;
import static org.assertj.core.api.Assertions.assertThat;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import eu.dissco.backend.domain.DigitalSpecimenWrapper;
import eu.dissco.backend.domain.annotation.Annotation;
import eu.dissco.backend.properties.ElasticSearchProperties;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
class ElasticSearchRepositoryIT {

  private static final DockerImageName ELASTIC_IMAGE = DockerImageName.parse(
      "docker.elastic.co/elasticsearch/elasticsearch").withTag("8.7.1");
  private static final String DIGITAL_SPECIMEN_INDEX = "digital-specimen";
  private static final String ANNOTATION_INDEX = "annotation";
  private static final String ELASTICSEARCH_USERNAME = "elastic";
  private static final String ELASTICSEARCH_PASSWORD = "s3cret";
  private static final String CREATED_ALT = "2022-09-02T09:59:24Z";
  private static final ElasticsearchContainer container = new ElasticsearchContainer(
      ELASTIC_IMAGE).withPassword(ELASTICSEARCH_PASSWORD);
  private static ElasticsearchClient client;
  private static RestClient restClient;
  private final ElasticSearchProperties properties = new ElasticSearchProperties();
  private ElasticSearchRepository repository;

  @BeforeAll
  static void initContainer() {
    // Create the elasticsearch container.
    container.start();

    final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    credentialsProvider.setCredentials(AuthScope.ANY,
        new UsernamePasswordCredentials(ELASTICSEARCH_USERNAME, ELASTICSEARCH_PASSWORD));

    HttpHost host = new HttpHost("localhost",
        container.getMappedPort(9200), "https");
    final RestClientBuilder builder = RestClient.builder(host);

    builder.setHttpClientConfigCallback(clientBuilder -> {
      clientBuilder.setSSLContext(container.createSslContextFromCa());
      clientBuilder.setDefaultCredentialsProvider(credentialsProvider);
      return clientBuilder;
    });
    restClient = builder.build();

    ElasticsearchTransport transport = new RestClientTransport(restClient,
        new JacksonJsonpMapper(MAPPER));

    client = new ElasticsearchClient(transport);
  }

  @AfterAll
  public static void closeResources() throws Exception {
    restClient.close();
  }

  private static Stream<Arguments> provideKeyValue() {
    return Stream.of(
        Arguments.of("digitalSpecimenWrapper.ods:physicalSpecimenId.keyword", "global_id_45634",
            1L),
        Arguments.of("q", PREFIX + "/0", 10L)
    );
  }

  @BeforeEach
  void initRepository() {
    repository = new ElasticSearchRepository(client, MAPPER, properties);
  }

  @AfterEach
  void clearIndex() throws IOException {
    if (client.indices().exists(re -> re.index(DIGITAL_SPECIMEN_INDEX)).value()) {
      client.indices().delete(b -> b.index(DIGITAL_SPECIMEN_INDEX));
    }
    if (client.indices().exists(re -> re.index(ANNOTATION_INDEX)).value()) {
      client.indices().delete(b -> b.index(ANNOTATION_INDEX));
    }
  }

  @ParameterizedTest
  @MethodSource("provideKeyValue")
  void testSearch(String field, String value, Long totalHits) throws IOException {
    // Given
    List<DigitalSpecimenWrapper> specimenTestRecords = new ArrayList<>();
    String targetId = PREFIX + "/0";
    var physicalId = "global_id_45634";
    var targetSpecimen = givenDigitalSpecimenWrapper(targetId, physicalId);
    specimenTestRecords.add(targetSpecimen);
    for (int i = 1; i < 10; i++) {
      var specimen = givenDigitalSpecimenWrapper(PREFIX + "/" + i);
      specimenTestRecords.add(specimen);
    }
    postDigitalSpecimens(parseToElasticFormat(specimenTestRecords));

    // When
    var responseReceived = repository.search(Map.of(field, List.of(value)), 1, 1);

    // Then
    assertThat(responseReceived.getLeft()).isEqualTo(totalHits);
    assertThat(responseReceived.getRight()).contains(
        givenDigitalSpecimenWrapper(DOI + targetId, physicalId, SOURCE_SYSTEM_ID_1));
  }

  @Test
  void testGetSpecimens() throws Exception {
    // Given
    int pageNumber = 0;
    int pageSize = 10;
    long totalHits = 15L;
    var digitalSpecimens = new ArrayList<DigitalSpecimenWrapper>();
    for (int i = 0; i < totalHits; i++) {
      String id = PREFIX + "/" + i;
      digitalSpecimens.add(givenDigitalSpecimenWrapper(id));
    }
    postDigitalSpecimens(parseToElasticFormat(digitalSpecimens));

    // When
    var responseReceived = repository.getSpecimens(pageNumber, pageSize);

    // Then
    assertThat(responseReceived.getLeft()).isEqualTo(totalHits);
    assertThat(responseReceived.getRight()).hasSize(pageSize + 1);
  }

  @Test
  void testSearchSecondPage() throws IOException {
    // Given
    int pageNumber = 2;
    int pageSize = 5;
    List<DigitalSpecimenWrapper> specimenTestRecords = new ArrayList<>();

    for (int i = 0; i < 10; i++) {
      var specimen = givenDigitalSpecimenWrapper(PREFIX + "/" + i);
      specimenTestRecords.add(specimen);
    }
    postDigitalSpecimens(parseToElasticFormat(specimenTestRecords));

    // When
    var responseReceived = repository.search(
        Map.of("digitalSpecimenWrapper.ods:attributes.dwc:institutionId.keyword",
            List.of("https://ror.org/0349vqz63")), pageNumber, pageSize);

    // Then
    assertThat(responseReceived.getLeft()).isEqualTo(10L);
    assertThat(responseReceived.getRight()).hasSize(pageSize);
  }

  @Test
  void testAggregations() throws IOException {
    // Given
    List<DigitalSpecimenWrapper> specimenTestRecords = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      DigitalSpecimenWrapper specimen;
      if (i < 5) {
        specimen = givenDigitalSpecimenSourceSystem(PREFIX + "/" + i, SOURCE_SYSTEM_ID_1);
      } else {
        specimen = givenDigitalSpecimenSourceSystem(PREFIX + "/" + i, SOURCE_SYSTEM_ID_2);
      }
      specimenTestRecords.add(specimen);
    }

    postDigitalSpecimens(parseToElasticFormat(specimenTestRecords));

    // When
    var responseReceived = repository.getAggregations(
        Map.of(SOURCE_SYSTEM_ID.getFullName(), List.of(SOURCE_SYSTEM_ID_2)));

    // Then
    assertThat(responseReceived.get("midsLevel")).containsEntry("0", 5L);
    assertThat(responseReceived.get("sourceSystemId")).containsEntry(SOURCE_SYSTEM_ID_2, 5L);
    assertThat(responseReceived.get("sourceSystemId").get(SOURCE_SYSTEM_ID_1)).isNull();
  }

  @Test
  void testAggregation() throws IOException {
    // Given
    List<DigitalSpecimenWrapper> specimenTestRecords = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      DigitalSpecimenWrapper specimen;
      if (i < 5) {
        specimen = givenDigitalSpecimenSourceSystem(PREFIX + "/" + i, SOURCE_SYSTEM_ID_1);
      } else {
        specimen = givenDigitalSpecimenSourceSystem(PREFIX + "/" + i, SOURCE_SYSTEM_ID_2);
      }
      specimenTestRecords.add(specimen);
    }

    postDigitalSpecimens(parseToElasticFormat(specimenTestRecords));

    // When
    var responseReceived = repository.getAggregation(SOURCE_SYSTEM_ID);

    // Then
    var aggregation = responseReceived.getRight();
    assertThat(aggregation.get("sourceSystemId")).containsEntry(SOURCE_SYSTEM_ID_2, 5L);
    assertThat(aggregation.get("sourceSystemId")).containsEntry(SOURCE_SYSTEM_ID_1, 5L);
    assertThat(responseReceived.getLeft()).isEqualTo(10L);
  }

  @Test
  void testSearchTermValue() throws IOException {
    // Given
    List<DigitalSpecimenWrapper> specimenTestRecords = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      DigitalSpecimenWrapper specimen;
      if (i < 5) {
        specimen = givenDigitalSpecimenSourceSystem(PREFIX + "/" + i, SOURCE_SYSTEM_ID_1);
      } else {
        specimen = givenDigitalSpecimenSourceSystem(PREFIX + "/" + i, SOURCE_SYSTEM_ID_2);
      }
      specimenTestRecords.add(specimen);
    }

    postDigitalSpecimens(parseToElasticFormat(specimenTestRecords));

    // When
    var responseReceived = repository.searchTermValue(SOURCE_SYSTEM_ID.getName(),
        SOURCE_SYSTEM_ID.getFullName(), SOURCE_SYSTEM_ID_2);

    // Then
    assertThat(responseReceived.get("sourceSystemId")).containsEntry(SOURCE_SYSTEM_ID_2, 5L);
    assertThat(responseReceived.get("sourceSystemId")).doesNotContainKey(SOURCE_SYSTEM_ID_1);
  }

  @Test
  void testGetLatestSpecimen() throws IOException {
    // Given
    int pageSize = 10;
    int pageNumber = 1;
    var givenSpecimens = new ArrayList<DigitalSpecimenWrapper>();
    var responseExpected = new ArrayList<DigitalSpecimenWrapper>();

    for (int i = 0; i < pageSize + 1; i++) {
      var specimen = givenDigitalSpecimenWrapper(PREFIX + "/" + i);
      responseExpected.add(
          givenDigitalSpecimenSourceSystem(DOI + PREFIX + "/" + i, SOURCE_SYSTEM_ID_1));
      givenSpecimens.add(specimen);
    }
    for (int i = pageSize; i < pageSize * 2; i++) {
      var specimen = givenDigitalSpecimenWrapper(PREFIX + "/" + i);
      givenSpecimens.add(specimen);
    }
    postDigitalSpecimens(parseToElasticFormat(givenSpecimens));

    // When
    var responseReceived = repository.getLatestSpecimen(pageNumber, pageSize);

    // Then
    assertThat(responseReceived.getRight()).hasSize(11).hasSameElementsAs(responseExpected);
  }

  @Test
  void testGetLatestSpecimenSecondPage() throws IOException {
    // Given
    int pageSize = 10;
    int pageNumber = 2;
    List<DigitalSpecimenWrapper> givenSpecimens = new ArrayList<>();
    List<DigitalSpecimenWrapper> responseExpected = new ArrayList<>();

    for (int i = 0; i < pageSize; i++) {
      var specimen = givenDigitalSpecimenWrapper(PREFIX + "/" + i);
      givenSpecimens.add(specimen);
    }

    for (int i = pageSize; i < pageSize * 2; i++) {
      var specimen = givenOlderSpecimen(PREFIX + "/" + i);
      responseExpected.add(
          givenOlderSpecimen(DOI + PREFIX + "/" + i, SOURCE_SYSTEM_ID_1));
      givenSpecimens.add(specimen);
    }
    postDigitalSpecimens(parseToElasticFormat(givenSpecimens));

    // When
    var responseReceived = repository.getLatestSpecimen(pageNumber, pageSize);

    // Then
    assertThat(responseReceived.getRight()).hasSize(pageSize).hasSameElementsAs(responseExpected);
  }

  private List<JsonNode> parseToElasticFormat(List<DigitalSpecimenWrapper> givenSpecimens) {
    return givenSpecimens.stream().map(this::toElasticFormat).toList();
  }

  @Test
  void testGetLatestAnnotations() throws IOException {
    // Given
    int pageNumber = 1;
    int pageSize = 10;
    List<Annotation> givenAnnotations = new ArrayList<>();
    List<Annotation> expected = new ArrayList<>();
    for (int i = 0; i < pageSize + 1; i++) {
      String id = PREFIX + "/" + i;
      var annotation = givenAnnotationResponse(id);
      expected.add(givenAnnotationResponse(HANDLE + id));
      givenAnnotations.add(annotation);
    }
    for (int i = 11; i < pageSize * 2; i++) {
      var annotation = givenAnnotationResponse( PREFIX + "/" + i);
      givenAnnotations.add(annotation);
    }
    postAnnotations(parseAnnotationToElasticFormat(givenAnnotations));

    // When
    var responseReceived = repository.getLatestAnnotations(pageNumber, pageSize);

    // Then
    assertThat(responseReceived).hasSize(11).hasSameElementsAs(expected);
  }

  @Test
  void testGetLatestAnnotationsNullAggregate() throws IOException {
    // Given
    int pageNumber = 1;
    int pageSize = 10;
    List<Annotation> givenAnnotations = new ArrayList<>();
    List<Annotation> expected = new ArrayList<>();
    for (int i = 0; i < pageSize + 1; i++) {
      String id = PREFIX + "/" + i;
      var annotation = givenAnnotationResponse(id);
      expected.add(givenAnnotationResponse(HANDLE + id).withOdsAggregateRating(null));
      givenAnnotations.add(annotation.withOdsAggregateRating(null));
    }
    for (int i = 11; i < pageSize * 2; i++) {
      var annotation = givenAnnotationResponse( PREFIX + "/" + i);
      givenAnnotations.add(annotation);
    }
    postAnnotations(parseAnnotationToElasticFormat(givenAnnotations));

    // When
    var responseReceived = repository.getLatestAnnotations(pageNumber, pageSize);

    // Then
    assertThat(responseReceived).hasSize(11).hasSameElementsAs(expected);
  }

  @Test
  void testGetAnnotationsForCreator() throws IOException {
    // Given
    int pageNumber = 1;
    int pageSize = 10;
    var totalHits = 15L;
    List<Annotation> givenAnnotations = new ArrayList<>();
    List<Annotation> expected = new ArrayList<>();
    for (long i = 0; i < totalHits; i++) {
      String id = PREFIX + "/" + i;
      if (i <= pageSize) {
        expected.add(givenAnnotationResponse(HANDLE + id, USER_ID_TOKEN));
      }
      givenAnnotations.add(givenAnnotationResponse(id, USER_ID_TOKEN));
      givenAnnotations.add(givenAnnotationResponse(id + "1", "A different User"));
    }
    postAnnotations(parseAnnotationToElasticFormat(givenAnnotations));

    // When
    var responseReceived = repository.getAnnotationsForCreator(USER_ID_TOKEN, pageNumber, pageSize);

    // Then
    assertThat(responseReceived.getLeft()).isEqualTo(totalHits);
    assertThat(responseReceived.getRight()).isEqualTo(expected);
  }

  @Test
  void testGetAnnotationsForCreatorEmpty() throws IOException {
    // Given
    int pageNumber = 1;
    int pageSize = 10;
    List<Annotation> givenAnnotations = new ArrayList<>();
    for (long i = 0; i < 5; i++) {
      String id = PREFIX + "/" + i;
      givenAnnotations.add(givenAnnotationResponse(id));
    }
    postAnnotations(parseAnnotationToElasticFormat(givenAnnotations));

    // When
    var responseReceived = repository.getAnnotationsForCreator("a different user", pageNumber,
        pageSize);

    // Then
    assertThat(responseReceived.getLeft()).isZero();
    assertThat(responseReceived.getRight()).isEmpty();
  }

  private List<JsonNode> parseAnnotationToElasticFormat(List<Annotation> annotations) {
    return annotations.stream().map(this::annotationToElasticFormat).toList();
  }

  private JsonNode toElasticFormat(DigitalSpecimenWrapper specimen) {
    var objectNode = MAPPER.createObjectNode();
    objectNode.put("id", specimen.digitalSpecimen().getOdsId());
    objectNode.put("midsLevel", specimen.digitalSpecimen().getOdsMidsLevel());
    objectNode.put("created", specimen.digitalSpecimen().getOdsCreated());
    objectNode.put("version", specimen.digitalSpecimen().getOdsVersion());
    var wrapperNode = MAPPER.createObjectNode();
    wrapperNode.put("ods:physicalSpecimenId",
        specimen.digitalSpecimen().getOdsPhysicalSpecimenId());
    wrapperNode.put("ods:type", specimen.digitalSpecimen().getOdsType());
    wrapperNode.set("ods:attributes", MAPPER.valueToTree(specimen.digitalSpecimen()));
    wrapperNode.set("ods:originalAttributes", specimen.originalData());
    objectNode.set("digitalSpecimenWrapper", wrapperNode);
    return objectNode;
  }

  private DigitalSpecimenWrapper givenOlderSpecimen(String id) throws JsonProcessingException {
    return givenOlderSpecimen(id, SOURCE_SYSTEM_ID_1);
  }

  private DigitalSpecimenWrapper givenOlderSpecimen(String id, String sourceSystem)
      throws JsonProcessingException {
    var spec = givenDigitalSpecimenSourceSystem(id, sourceSystem);
    return new DigitalSpecimenWrapper(
        spec.digitalSpecimen().withOdsCreated(CREATED_ALT),
        spec.originalData());
  }

  public BulkResponse postDigitalSpecimens(List<JsonNode> digitalSpecimens)
      throws IOException {
    var bulkRequest = new BulkRequest.Builder();
    for (var digitalSpecimen : digitalSpecimens) {
      bulkRequest.operations(op -> op.index(
          idx -> idx.index(DIGITAL_SPECIMEN_INDEX).id(digitalSpecimen.get("id").asText())
              .document(digitalSpecimen)));
    }
    var response = client.bulk(bulkRequest.build());
    client.indices().refresh(b -> b.index(DIGITAL_SPECIMEN_INDEX));
    return response;
  }

  private JsonNode annotationToElasticFormat(Annotation annotation) {
    var objectNode = MAPPER.createObjectNode();
    objectNode.put("id", annotation.getOdsId());
    objectNode.put("created", annotation.getDcTermsCreated().toString());
    objectNode.put("version", annotation.getOdsVersion());
    var annotationNode = MAPPER.createObjectNode();
    annotationNode.put("motivation", annotation.getOaMotivation().toString());
    annotationNode.put("type", annotation.getRdfType());
    annotationNode.set("aggregateRating", MAPPER.valueToTree(annotation.getOdsAggregateRating()));
    annotationNode.set("creator", MAPPER.valueToTree(annotation.getOaCreator()));
    annotationNode.put("creatorId", annotation.getOaCreator().getOdsId());
    annotationNode.put("created", annotation.getDcTermsCreated().toString());
    annotationNode.put("generated", annotation.getOaGenerated().toString());
    annotationNode.set("target", MAPPER.valueToTree(annotation.getOaTarget()));
    annotationNode.set("body", MAPPER.valueToTree(annotation.getOaBody()));
    annotationNode.set("generator", MAPPER.valueToTree(annotation.getAsGenerator()));
    objectNode.set("annotation", annotationNode);
    return objectNode;
  }

  private BulkResponse postAnnotations(List<JsonNode> annotations) throws IOException {
    var bulkRequest = new BulkRequest.Builder();
    for (var annotation : annotations) {
      bulkRequest.operations(
          op -> op.index(
              idx -> idx.index(ANNOTATION_INDEX).id(annotation.get("id").asText())
                  .document(annotation)));
    }
    var response = client.bulk(bulkRequest.build());
    client.indices().refresh(b -> b.index(ANNOTATION_INDEX));
    return response;
  }
}

