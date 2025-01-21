package eu.dissco.backend.repository;


import static eu.dissco.backend.TestUtils.DOI;
import static eu.dissco.backend.TestUtils.HANDLE;
import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.ID_ALT;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.PHYSICAL_ID;
import static eu.dissco.backend.TestUtils.PREFIX;
import static eu.dissco.backend.TestUtils.SOURCE_SYSTEM_ID_1;
import static eu.dissco.backend.TestUtils.SOURCE_SYSTEM_ID_2;
import static eu.dissco.backend.TestUtils.SPECIMEN_NAME;
import static eu.dissco.backend.TestUtils.SPECIMEN_NAME_2;
import static eu.dissco.backend.TestUtils.USER_ID_TOKEN;
import static eu.dissco.backend.TestUtils.givenDigitalSpecimenAltCountry;
import static eu.dissco.backend.TestUtils.givenDigitalSpecimenSourceSystem;
import static eu.dissco.backend.TestUtils.givenDigitalSpecimenSpecimenName;
import static eu.dissco.backend.TestUtils.givenDigitalSpecimenWrapper;
import static eu.dissco.backend.domain.DefaultMappingTerms.SOURCE_SYSTEM_ID;
import static eu.dissco.backend.domain.DefaultMappingTerms.SOURCE_SYSTEM_NAME;
import static eu.dissco.backend.domain.DefaultMappingTerms.getAggregationSet;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationResponse;
import static eu.dissco.backend.utils.AnnotationUtils.givenSearchParam;
import static org.assertj.core.api.Assertions.assertThat;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.dissco.backend.domain.DefaultMappingTerms;
import eu.dissco.backend.domain.annotation.AnnotationTargetType;
import eu.dissco.backend.domain.annotation.batch.BatchMetadata;
import eu.dissco.backend.domain.annotation.batch.SearchParam;
import eu.dissco.backend.properties.ElasticSearchProperties;
import eu.dissco.backend.schema.Annotation;
import eu.dissco.backend.schema.DigitalSpecimen;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
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
      "docker.elastic.co/elasticsearch/elasticsearch").withTag("8.15.0");
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
        Arguments.of("ods:physicalSpecimenID.keyword", "global_id_45634",
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
    List<DigitalSpecimen> specimenTestRecords = new ArrayList<>();
    String targetId = DOI + PREFIX + "/0";
    var physicalId = "global_id_45634";
    var targetSpecimen = givenDigitalSpecimenWrapper(targetId, physicalId);
    specimenTestRecords.add(targetSpecimen);
    for (int i = 1; i < 10; i++) {
      var specimen = givenDigitalSpecimenWrapper(DOI + PREFIX + "/" + i);
      specimenTestRecords.add(specimen);
    }
    postDigitalSpecimens(parseToElasticFormat(specimenTestRecords));

    // When
    var responseReceived = repository.search(Map.of(field, List.of(value)), 1, 1);

    // Then
    assertThat(responseReceived.getLeft()).isEqualTo(totalHits);
    assertThat(responseReceived.getRight()).contains(
        givenDigitalSpecimenWrapper(targetId, physicalId, SOURCE_SYSTEM_ID_1));
  }

  @Test
  void testElvisSearchIdentifier() throws Exception {
    // Given
    String searchId = PREFIX + "/0";
    var targetId = DOI + searchId + "000";
    var targetSpecimen = givenDigitalSpecimenWrapper(targetId, PHYSICAL_ID);
    List<DigitalSpecimen> specimenTestRecords = new ArrayList<>();
    specimenTestRecords.add(targetSpecimen);
    for (int i = 1; i < 10; i++) {
      var specimen = givenDigitalSpecimenWrapper(DOI + PREFIX + "/" + i);
      specimenTestRecords.add(specimen);
    }
    postDigitalSpecimens(parseToElasticFormat(specimenTestRecords));
    var map = Map.of(
        "ods:physicalSpecimenID.keyword", List.of("*" + searchId + "*"),
        "dcterms:identifier.keyword", List.of(DOI + searchId + "*"));

    // When
    var result = repository.elvisSearch(map, 1, 11);

    // then
    assertThat(result.getLeft()).isEqualTo(1);
    assertThat(result.getRight()).isEqualTo(List.of(targetSpecimen));
  }

  @Test
  void testElvisSearchPhysicalId() throws Exception {
    // Given
    String searchId = PREFIX + "/0";
    var targetId = searchId + "000";
    var targetSpecimen = givenDigitalSpecimenWrapper(HANDLE, targetId);
    List<DigitalSpecimen> specimenTestRecords = new ArrayList<>();
    specimenTestRecords.add(targetSpecimen);
    for (int i = 1; i < 10; i++) {
      var specimen = givenDigitalSpecimenWrapper(DOI + PREFIX + "/" + i);
      specimenTestRecords.add(specimen);
    }
    postDigitalSpecimens(parseToElasticFormat(specimenTestRecords));
    var map = Map.of(
        "ods:physicalSpecimenID.keyword", List.of("*" + searchId + "*"),
        "dcterms:identifier.keyword", List.of(DOI + searchId + "*"));

    // When
    var result = repository.elvisSearch(map, 1, 11);

    // then
    assertThat(result.getLeft()).isEqualTo(1);
    assertThat(result.getRight()).isEqualTo(List.of(targetSpecimen));
  }


  @Test
  void testGetSpecimens() throws Exception {
    // Given
    int pageNumber = 0;
    int pageSize = 10;
    long totalHits = 15L;
    var digitalSpecimens = new ArrayList<DigitalSpecimen>();
    for (int i = 0; i < totalHits; i++) {
      String id = DOI + PREFIX + "/" + i;
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
    List<DigitalSpecimen> specimenTestRecords = new ArrayList<>();

    for (int i = 0; i < 10; i++) {
      var specimen = givenDigitalSpecimenWrapper(DOI + PREFIX + "/" + i);
      specimenTestRecords.add(specimen);
    }
    postDigitalSpecimens(parseToElasticFormat(specimenTestRecords));

    // When
    var responseReceived = repository.search(
        Map.of("ods:organisationID.keyword", List.of("https://ror.org/0349vqz63")), pageNumber,
        pageSize);

    // Then
    assertThat(responseReceived.getLeft()).isEqualTo(10L);
    assertThat(responseReceived.getRight()).hasSize(pageSize);
  }

  @Test
  void testAggregations() throws IOException {
    // Given
    var anotherSourceSystemName = "Another source system";
    List<DigitalSpecimen> specimenTestRecords = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      DigitalSpecimen specimen;
      if (i < 5) {
        specimen = givenDigitalSpecimenSourceSystem(DOI + PREFIX + "/" + i,
            SOURCE_SYSTEM_ID_1);
      } else {
        specimen = givenDigitalSpecimenSourceSystem(DOI + PREFIX + "/" + i,
            SOURCE_SYSTEM_ID_2);
        specimen.setOdsSourceSystemName(anotherSourceSystemName);
      }
      specimenTestRecords.add(specimen);
    }

    postDigitalSpecimens(parseToElasticFormat(specimenTestRecords));

    // When
    var responseReceived = repository.getAggregations(
        Map.of(SOURCE_SYSTEM_NAME.fullName(), List.of(anotherSourceSystemName)),
        getAggregationSet(),
        false);

    // Then
    assertThat(responseReceived.get("midsLevel")).containsEntry("0", 5L);
    assertThat(responseReceived.get("sourceSystemName")).containsEntry(anotherSourceSystemName, 5L);
    assertThat(responseReceived.get("sourceSystemName").get(SOURCE_SYSTEM_NAME)).isNull();
  }

  @Test
  void testAggregation() throws IOException {
    // Given
    List<DigitalSpecimen> specimenTestRecords = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      DigitalSpecimen specimen;
      if (i < 5) {
        specimen = givenDigitalSpecimenSourceSystem(DOI + PREFIX + "/" + i,
            SOURCE_SYSTEM_ID_1);
      } else {
        specimen = givenDigitalSpecimenSourceSystem(DOI + PREFIX + "/" + i,
            SOURCE_SYSTEM_ID_2);
      }
      specimenTestRecords.add(specimen);
    }

    postDigitalSpecimens(parseToElasticFormat(specimenTestRecords));

    // When
    var responseReceived = repository.getAggregation(SOURCE_SYSTEM_ID);

    // Then
    var aggregation = responseReceived.getRight();
    assertThat(aggregation.get("sourceSystemID")).containsEntry(SOURCE_SYSTEM_ID_2, 5L);
    assertThat(aggregation.get("sourceSystemID")).containsEntry(SOURCE_SYSTEM_ID_1, 5L);
    assertThat(responseReceived.getLeft()).isEqualTo(10L);
  }

  @Test
  void testSearchTermValue() throws IOException {
    // Given
    List<DigitalSpecimen> specimenTestRecords = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      DigitalSpecimen specimen;
      if (i < 5) {
        specimen = givenDigitalSpecimenSourceSystem(DOI + PREFIX + "/" + i,
            SOURCE_SYSTEM_ID_1);
      } else {
        specimen = givenDigitalSpecimenSourceSystem(DOI + PREFIX + "/" + i,
            SOURCE_SYSTEM_ID_2);
      }
      specimenTestRecords.add(specimen);
    }

    postDigitalSpecimens(parseToElasticFormat(specimenTestRecords));

    // When
    var responseReceived = repository.aggregateTermValue(SOURCE_SYSTEM_ID.requestName(),
        SOURCE_SYSTEM_ID.fullName(), SOURCE_SYSTEM_ID_2, false);

    // Then
    assertThat(responseReceived.get("sourceSystemID")).containsEntry(SOURCE_SYSTEM_ID_2, 5L);
    assertThat(responseReceived.get("sourceSystemID")).doesNotContainKey(SOURCE_SYSTEM_ID_1);
  }

  @Test
  void testSearchTermValueOrderAsc() throws IOException {
    // Given
    List<DigitalSpecimen> specimenTestRecords = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      DigitalSpecimen specimen;
      if (i < 9) {
        specimen = givenDigitalSpecimenSpecimenName(DOI + PREFIX + "/" + i, SPECIMEN_NAME);
      } else {
        specimen = givenDigitalSpecimenSpecimenName(DOI + PREFIX + "/" + i, SPECIMEN_NAME_2);
      }
      specimenTestRecords.add(specimen);
    }

    postDigitalSpecimens(parseToElasticFormat(specimenTestRecords));

    // When
    var responseReceived = repository.aggregateTermValue(
        DefaultMappingTerms.SPECIMEN_NAME.requestName(),
        DefaultMappingTerms.SPECIMEN_NAME.fullName(), "A", true);

    // Then
    var iterator = responseReceived.get("specimenName").keySet().iterator();
    assertThat(iterator.next()).isEqualTo(SPECIMEN_NAME_2);
    assertThat(iterator.next()).isEqualTo(SPECIMEN_NAME);
  }

  @Test
  void testGetLatestSpecimen() throws IOException {
    // Given
    int pageSize = 10;
    int pageNumber = 1;
    var givenSpecimens = new ArrayList<DigitalSpecimen>();
    var responseExpected = new ArrayList<DigitalSpecimen>();

    for (int i = 0; i < pageSize + 1; i++) {
      var specimen = givenDigitalSpecimenWrapper(DOI + PREFIX + "/" + i);
      responseExpected.add(
          givenDigitalSpecimenSourceSystem(DOI + PREFIX + "/" + i, SOURCE_SYSTEM_ID_1));
      givenSpecimens.add(specimen);
    }
    for (int i = pageSize; i < pageSize * 2; i++) {
      var specimen = givenDigitalSpecimenWrapper(DOI + PREFIX + "/" + i);
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
    List<DigitalSpecimen> givenSpecimens = new ArrayList<>();
    List<DigitalSpecimen> responseExpected = new ArrayList<>();

    for (int i = 0; i < pageSize; i++) {
      var specimen = givenDigitalSpecimenWrapper(DOI + PREFIX + "/" + i);
      givenSpecimens.add(specimen);
    }

    for (int i = pageSize; i < pageSize * 2; i++) {
      var specimen = givenOlderSpecimen(DOI + PREFIX + "/" + i);
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

  private List<JsonNode> parseToElasticFormat(List<DigitalSpecimen> givenSpecimens) {
    return givenSpecimens.stream().map(this::toElasticFormat).toList();
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
      String id = HANDLE + PREFIX + "/" + i;
      if (i <= pageSize) {
        expected.add(givenAnnotationResponse(id, USER_ID_TOKEN));
      }
      givenAnnotations.add(givenAnnotationResponse(id, USER_ID_TOKEN));
      givenAnnotations.add(
          givenAnnotationResponse(id + "1", "https://orcid.org/0000-1112-5669-2769"));
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

  @Test
  void getCountForBatchAnnotations() throws Exception {
    // Given
    var batchMetadata = new BatchMetadata(List.of(givenSearchParam("Scotland")));
    var givenSpecimens = List.of(
        givenDigitalSpecimenWrapper(ID),
        givenDigitalSpecimenAltCountry(ID_ALT),
        givenDigitalSpecimenWrapper("A-third-specimen")
    );
    postDigitalSpecimens(parseToElasticFormat(givenSpecimens));

    // When
    var result = repository.getCountForBatchAnnotations(batchMetadata,
        AnnotationTargetType.DIGITAL_SPECIMEN);

    // Then
    assertThat(result).isEqualTo(2L);
  }

  @Test
  void getCountForBatchAnnotationsBlankParam() throws Exception {
    // Given
    var batchMetadata = new BatchMetadata(List.of(new SearchParam(
        "ods:hasEvent[*].dwc:FieldNumber",
        ""
    )));
    postDigitalSpecimens(parseToElasticFormat(List.of(givenDigitalSpecimenWrapper(ID))));

    // When
    var result = repository.getCountForBatchAnnotations(batchMetadata,
        AnnotationTargetType.DIGITAL_SPECIMEN);

    // Then
    assertThat(result).isEqualTo(1L);
  }

  private List<JsonNode> parseAnnotationToElasticFormat(List<Annotation> annotations) {
    return annotations.stream().map(this::annotationToElasticFormat).toList();
  }

  private JsonNode toElasticFormat(DigitalSpecimen specimen) {
    var node = (ObjectNode) MAPPER.valueToTree(specimen);
    // topicDiscipline default mapping to text will fail the test, removing it before we run the test
    node.remove("ods:topicDiscipline");
    return node;
  }

  private DigitalSpecimen givenOlderSpecimen(String id) {
    return givenOlderSpecimen(id, SOURCE_SYSTEM_ID_1);
  }

  private DigitalSpecimen givenOlderSpecimen(String id, String sourceSystem) {
    var spec = givenDigitalSpecimenSourceSystem(id, sourceSystem);
    return spec.withDctermsCreated(Date.from(Instant.parse(CREATED_ALT)));
  }

  public BulkResponse postDigitalSpecimens(List<JsonNode> digitalSpecimens)
      throws IOException {
    var bulkRequest = new BulkRequest.Builder();
    for (var digitalSpecimen : digitalSpecimens) {
      bulkRequest.operations(op -> op.index(
          idx -> idx.index(DIGITAL_SPECIMEN_INDEX).id(digitalSpecimen.get("@id").asText())
              .document(digitalSpecimen)));
    }
    var response = client.bulk(bulkRequest.build());
    client.indices().refresh(b -> b.index(DIGITAL_SPECIMEN_INDEX));
    return response;
  }

  private JsonNode annotationToElasticFormat(Annotation annotation) {
    return MAPPER.valueToTree(annotation);
  }

  private BulkResponse postAnnotations(List<JsonNode> annotations) throws IOException {
    var bulkRequest = new BulkRequest.Builder();
    for (var annotation : annotations) {
      bulkRequest.operations(
          op -> op.index(
              idx -> idx.index(ANNOTATION_INDEX).id(annotation.get("dcterms:identifier").asText())
                  .document(annotation)));
    }
    var response = client.bulk(bulkRequest.build());
    client.indices().refresh(b -> b.index(ANNOTATION_INDEX));
    return response;
  }
}

