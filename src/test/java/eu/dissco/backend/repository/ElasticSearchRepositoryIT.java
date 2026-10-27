package eu.dissco.backend.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.json.jackson.Jackson3JsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest5_client.Rest5ClientTransport;
import co.elastic.clients.transport.rest5_client.low_level.Rest5Client;
import eu.dissco.backend.TestUtils;
import eu.dissco.backend.domain.annotation.AnnotationTargetType;
import eu.dissco.backend.domain.annotation.batch.BatchMetadata;
import eu.dissco.backend.domain.annotation.batch.SearchParam;
import eu.dissco.backend.domain.elastic.DefaultMappingTerms;
import eu.dissco.backend.domain.elastic.MappingTerm;
import eu.dissco.backend.domain.elastic.MissingMappingTerms;
import eu.dissco.backend.domain.elastic.TaxonMappingTerms;
import eu.dissco.backend.properties.ElasticSearchProperties;
import eu.dissco.backend.schema.*;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.message.BasicHeader;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

import static eu.dissco.backend.TestUtils.*;
import static eu.dissco.backend.domain.elastic.DefaultMappingTerms.*;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationResponse;
import static eu.dissco.backend.utils.AnnotationUtils.givenSearchParam;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class ElasticSearchRepositoryIT {

	private static final DockerImageName ELASTIC_IMAGE = DockerImageName
		.parse("docker.elastic.co/elasticsearch/elasticsearch")
		.withTag("9.2.0");

	private static final String DIGITAL_SPECIMEN_INDEX = "digital-specimen";

	private static final String ANNOTATION_INDEX = "annotation";

	private static final String ELASTICSEARCH_USERNAME = "elastic";

	private static final String ELASTICSEARCH_PASSWORD = "s3cret";

	private static final String CREATED_ALT = "2022-09-02T09:59:24Z";

	private static final ElasticsearchContainer container = new ElasticsearchContainer(ELASTIC_IMAGE)
		.withPassword(ELASTICSEARCH_PASSWORD)
		.withStartupTimeout(Duration.ofMinutes(2));

	private static ElasticsearchClient client;

	private static Rest5Client restClient;

	private final ElasticSearchProperties properties = new ElasticSearchProperties();

	private ElasticSearchRepository repository;

	@BeforeAll
	static void initContainer() {
		// Create the elasticsearch container.
		container.start();

		var creds = Base64.getEncoder()
			.encodeToString((ELASTICSEARCH_USERNAME + ":" + ELASTICSEARCH_PASSWORD).getBytes());

		restClient = Rest5Client.builder(new HttpHost("https", "localhost", container.getMappedPort(9200)))
			.setDefaultHeaders(new Header[] { new BasicHeader("Authorization", "Basic " + creds) })
			.setSSLContext(container.createSslContextFromCa())
			.build();

		ElasticsearchTransport transport = new Rest5ClientTransport(restClient, new Jackson3JsonpMapper(MAPPER));

		client = new ElasticsearchClient(transport);
	}

	@AfterAll
	static void closeResources() throws Exception {
		restClient.close();
	}

	private static Stream<Arguments> provideKeyValue() {
		return Stream.of(Arguments.of(DefaultMappingTerms.PHYSICAL_SPECIMEN_ID, "global_id_45634", 1L),
				Arguments.of(DefaultMappingTerms.PHYSICAL_SPECIMEN_ID, "global_id_45*", 1L),
				Arguments.of(MissingMappingTerms.HAS_COUNTRY, "true", 10L),
				Arguments.of(MissingMappingTerms.HAS_PHYLUM, "false", 10L),
				Arguments.of(DefaultMappingTerms.QUERY, PREFIX + "/0", 10L));
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
	void testSearch(MappingTerm field, String value, Long totalHits) throws IOException {
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
		assertThat(responseReceived.getRight())
			.contains(givenDigitalSpecimenWrapper(targetId, physicalId, SOURCE_SYSTEM_ID_1));
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
		var map = Map.of(DefaultMappingTerms.PHYSICAL_SPECIMEN_ID, List.of("*" + searchId + "*"),
				DefaultMappingTerms.IDENTIFIER, List.of(DOI + "*" + searchId + "*"));

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
		var map = Map.of(DefaultMappingTerms.PHYSICAL_SPECIMEN_ID, List.of("*" + searchId + "*"),
				DefaultMappingTerms.IDENTIFIER, List.of(DOI + "*" + searchId + "*"));

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
				Map.of(DefaultMappingTerms.ORGANISATION_ID, List.of("https://ror.org/0349vqz63")), pageNumber,
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
				specimen = givenDigitalSpecimenSourceSystem(DOI + PREFIX + "/" + i, SOURCE_SYSTEM_ID_1);
			}
			else {
				specimen = givenDigitalSpecimenSourceSystem(DOI + PREFIX + "/" + i, SOURCE_SYSTEM_ID_2);
				specimen.setOdsSourceSystemName(anotherSourceSystemName);
			}
			specimenTestRecords.add(specimen);
		}

		postDigitalSpecimens(parseToElasticFormat(specimenTestRecords));

		// When
		var responseReceived = repository.getAggregations(Map.of(SOURCE_SYSTEM_NAME, List.of(anotherSourceSystemName)),
				getAggregationSet(), DEFAULT_SIZE);

		// Then
		assertThat(responseReceived.get("country")).containsEntry("Scotland", 5L);
		assertThat(responseReceived.get("sourceSystemName")).containsEntry(anotherSourceSystemName, 5L);
		assertThat(responseReceived.get("sourceSystemName").get(SOURCE_SYSTEM_NAME)).isNull();
	}

	@Test
	void testGetAggregations() throws IOException {
		// Given
		List<DigitalSpecimen> specimenTestRecords = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			var specimen = givenDigitalSpecimenWrapper(DOI + PREFIX + "/" + i);
			specimenTestRecords.add(specimen);
		}
		postDigitalSpecimens(parseToElasticFormat(specimenTestRecords));

		// When
		var responseReceived = repository.getAggregations(Map.of(), getAggregationSet(), DEFAULT_SIZE);

		// Then
		assertThat(responseReceived.get("country")).containsEntry("Scotland", 10L);
	}

	@Test
	void testGetAggregationsTaxonomy() throws IOException {
		// Given
		List<DigitalSpecimen> specimenTestRecords = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			var kingdom = i < 5 ? "Fungi" : "Plantae";
			var specimen = givenDigitalSpecimenWrapper(DOI + PREFIX + "/" + i)
				.withOdsHasIdentifications(List.of(new Identification()
					.withOdsHasTaxonIdentifications(List.of(new TaxonIdentification().withDwcKingdom(kingdom)))));
			specimenTestRecords.add(specimen);
		}
		specimenTestRecords.add(givenDigitalSpecimenWrapper(DOI + PREFIX + "/" + 10));
		postDigitalSpecimens(parseToElasticFormat(specimenTestRecords));

		// When
		var responseReceived = repository.getAggregations(Map.of(), Set.of(TaxonMappingTerms.KINGDOM),
				Integer.MAX_VALUE);

		// Then
		assertThat(responseReceived.get("kingdom")).containsEntry("Plantae", 5L);
		assertThat(responseReceived.get("kingdom")).containsEntry("Plantae", 5L);
		assertThat(responseReceived).doesNotContainKey("missingData");
	}

	@Test
	void testAggregation() throws IOException {
		// Given
		List<DigitalSpecimen> specimenTestRecords = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			DigitalSpecimen specimen;
			if (i < 5) {
				specimen = givenDigitalSpecimenSourceSystem(DOI + PREFIX + "/" + i, SOURCE_SYSTEM_ID_1);
			}
			else {
				specimen = givenDigitalSpecimenSourceSystem(DOI + PREFIX + "/" + i, SOURCE_SYSTEM_ID_2);
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
	void testSearchOrWithinTermAndAcrossTerms() throws IOException {
		// Given
		var specimenNetherlands = givenDigitalSpecimenCountries(DOI + PREFIX + "/0", 1, "Netherlands");
		var specimenFinland = givenDigitalSpecimenCountries(DOI + PREFIX + "/1", 1, "Finland");
		var specimenFinlandMidsZero = givenDigitalSpecimenCountries(DOI + PREFIX + "/2", 0, "Finland");
		var specimenBothMidsZero = givenDigitalSpecimenCountries(DOI + PREFIX + "/3", 0, "Netherlands", "Finland");
		postDigitalSpecimens(parseToElasticFormat(
				List.of(specimenNetherlands, specimenFinland, specimenFinlandMidsZero, specimenBothMidsZero)));
		var params = Map.of(DefaultMappingTerms.COUNTRY, List.of("Netherlands", "Finland"),
				DefaultMappingTerms.MIDS_LEVEL, List.of("1"));

		// When
		var responseReceived = repository.search(params, 1, 10);

		// Then
		assertThat(responseReceived.getLeft()).isEqualTo(2L);
		assertThat(responseReceived.getRight()).containsExactlyInAnyOrder(specimenNetherlands, specimenFinland);
	}

	@Test
	void testGetAggregationsOrWithinTermAndAcrossTerms() throws IOException {
		// Given
		var specimenNetherlands = givenDigitalSpecimenCountries(DOI + PREFIX + "/0", 1, "Netherlands");
		var specimenFinland = givenDigitalSpecimenCountries(DOI + PREFIX + "/1", 1, "Finland");
		var specimenFinlandMidsZero = givenDigitalSpecimenCountries(DOI + PREFIX + "/2", 0, "Finland");
		var specimenBothMidsZero = givenDigitalSpecimenCountries(DOI + PREFIX + "/3", 0, "Netherlands", "Finland");
		postDigitalSpecimens(parseToElasticFormat(
				List.of(specimenNetherlands, specimenFinland, specimenFinlandMidsZero, specimenBothMidsZero)));
		var params = Map.of(DefaultMappingTerms.COUNTRY, List.of("Netherlands", "Finland"),
				DefaultMappingTerms.MIDS_LEVEL, List.of("1"));

		// When
		var responseReceived = repository.getAggregations(params, Set.of(DefaultMappingTerms.COUNTRY), DEFAULT_SIZE);

		// Then
		assertThat(responseReceived).containsValue(Map.of("Netherlands", 1L, "Finland", 1L))
			.containsOnlyKeys("country");
	}

	private static Stream<Arguments> provideTermAggregationPrefix() {
		var midsLevelOne = Map.of(DefaultMappingTerms.MIDS_LEVEL, List.of("1"));
		return Stream.of(Arguments.of(Map.of(), "neth", Map.of("Netherlands", 3L)),
				Arguments.of(midsLevelOne, "NETH", Map.of("Netherlands", 2L)),
				Arguments.of(Map.of(), "st.", Map.of("St. Helena", 2L)), Arguments.of(midsLevelOne, null,
						Map.of("Netherlands", 2L, "Finland", 2L, "St. Helena", 2L, "Stateless", 1L)));
	}

	@ParameterizedTest
	@MethodSource("provideTermAggregationPrefix")
	void testGetTermAggregation(Map<MappingTerm, List<String>> params, String prefix, Map<String, Long> expected)
			throws IOException {
		// Given
		postDigitalSpecimens(
				parseToElasticFormat(List.of(givenDigitalSpecimenCountries(DOI + PREFIX + "/0", 1, "Netherlands"),
						givenDigitalSpecimenCountries(DOI + PREFIX + "/1", 1, "Finland"),
						givenDigitalSpecimenCountries(DOI + PREFIX + "/2", 1, "Netherlands", "Finland"),
						givenDigitalSpecimenCountries(DOI + PREFIX + "/3", 0, "Netherlands"),
						givenDigitalSpecimenCountries(DOI + PREFIX + "/4", 1, "St. Helena"),
						givenDigitalSpecimenCountries(DOI + PREFIX + "/5", 1, "St. Helena", "Stateless"))));

		// When
		var responseReceived = repository.getTermAggregation(params, DefaultMappingTerms.COUNTRY, prefix, DEFAULT_SIZE,
				false);

		// Then
		assertThat(responseReceived).containsValue(expected).containsOnlyKeys("country");
	}

	private static Stream<Arguments> provideNonKeywordTermAggregation() {
		return Stream.of(Arguments.of(DefaultMappingTerms.HAS_MEDIA, Map.of("true", 2L, "false", 1L)),
				Arguments.of(DefaultMappingTerms.MIDS_LEVEL, Map.of("1", 2L, "0", 1L)));
	}

	@ParameterizedTest
	@MethodSource("provideNonKeywordTermAggregation")
	void testGetTermAggregationNonKeywordTerm(MappingTerm aggregationTerm, Map<String, Long> expected)
			throws IOException {
		// Given
		postDigitalSpecimens(
				parseToElasticFormat(List.of(givenDigitalSpecimenCountries(DOI + PREFIX + "/0", 1, "Netherlands"),
						givenDigitalSpecimenCountries(DOI + PREFIX + "/1", 1, "Netherlands"),
						givenDigitalSpecimenCountries(DOI + PREFIX + "/2", 0, "Netherlands")
							.withOdsIsKnownToContainMedia(false))));

		// When
		var responseReceived = repository.getTermAggregation(Map.of(), aggregationTerm, null, DEFAULT_SIZE, false);

		// Then
		assertThat(responseReceived).containsValue(expected).containsOnlyKeys(aggregationTerm.requestName());
	}

	private static DigitalSpecimen givenDigitalSpecimenCountries(String id, int midsLevel, String... countries) {
		var events = Stream.of(countries)
			.map(country -> new Event().withOdsHasLocation(new Location().withDwcCountry(country)))
			.toList();
		return givenDigitalSpecimenWrapper(id).withOdsMidsLevel(midsLevel).withOdsHasEvents(events);
	}

	@Test
	void testSearchTermValue() throws IOException {
		// Given
		List<DigitalSpecimen> specimenTestRecords = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			DigitalSpecimen specimen;
			if (i < 5) {
				specimen = givenDigitalSpecimenSourceSystem(DOI + PREFIX + "/" + i, SOURCE_SYSTEM_ID_1);
			}
			else {
				specimen = givenDigitalSpecimenSourceSystem(DOI + PREFIX + "/" + i, SOURCE_SYSTEM_ID_2);
			}
			specimenTestRecords.add(specimen);
		}

		postDigitalSpecimens(parseToElasticFormat(specimenTestRecords));

		// When
		var responseReceived = repository.getTermAggregation(Map.of(), SOURCE_SYSTEM_ID, SOURCE_SYSTEM_ID_2,
				DEFAULT_SIZE, false);

		// Then
		assertThat(responseReceived.get("sourceSystemID")).containsEntry(SOURCE_SYSTEM_ID_2, 5L);
		assertThat(responseReceived.get("sourceSystemID")).doesNotContainKey(SOURCE_SYSTEM_ID_1);
		assertThat(responseReceived).doesNotContainKey("missingData");
	}

	@Test
	void testSearchTermValueOrderAsc() throws IOException {
		// Given
		List<DigitalSpecimen> specimenTestRecords = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			DigitalSpecimen specimen;
			if (i < 9) {
				specimen = givenDigitalSpecimenSpecimenName(DOI + PREFIX + "/" + i, TestUtils.SPECIMEN_NAME);
			}
			else {
				specimen = givenDigitalSpecimenSpecimenName(DOI + PREFIX + "/" + i, SPECIMEN_NAME_2);
			}
			specimenTestRecords.add(specimen);
		}

		postDigitalSpecimens(parseToElasticFormat(specimenTestRecords));

		// When
		var responseReceived = repository.getTermAggregation(Map.of(), DefaultMappingTerms.SPECIMEN_NAME, "A",
				DEFAULT_SIZE, true);

		// Then
		var iterator = responseReceived.get("specimenName").keySet().iterator();
		assertThat(iterator.next()).isEqualTo(SPECIMEN_NAME_2);
		assertThat(iterator.next()).isEqualTo(TestUtils.SPECIMEN_NAME);
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
			responseExpected.add(givenDigitalSpecimenSourceSystem(DOI + PREFIX + "/" + i, SOURCE_SYSTEM_ID_1));
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
			responseExpected.add(givenOlderSpecimen(DOI + PREFIX + "/" + i, SOURCE_SYSTEM_ID_1));
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
				expected.add(givenAnnotationResponse(id, ORCID_ALT));
			}
			givenAnnotations.add(givenAnnotationResponse(id, ORCID_ALT));
			givenAnnotations.add(givenAnnotationResponse(id + "1", "https://orcid.org/0000-1112-5669-2769"));
		}
		postAnnotations(parseAnnotationToElasticFormat(givenAnnotations));

		// When
		var responseReceived = repository.getAnnotationsForCreator(ORCID_ALT, pageNumber, pageSize);

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
		var responseReceived = repository.getAnnotationsForCreator("a different user", pageNumber, pageSize);

		// Then
		assertThat(responseReceived.getLeft()).isZero();
		assertThat(responseReceived.getRight()).isEmpty();
	}

	@Test
	void getCountForBatchAnnotations() throws Exception {
		// Given
		var batchMetadata = new BatchMetadata(List.of(givenSearchParam("Scotland")));
		var givenSpecimens = List.of(givenDigitalSpecimenWrapper(ID), givenDigitalSpecimenAltCountry(ID_ALT),
				givenDigitalSpecimenWrapper("A-third-specimen"));
		postDigitalSpecimens(parseToElasticFormat(givenSpecimens));

		// When
		var result = repository.getCountForBatchAnnotations(batchMetadata, AnnotationTargetType.DIGITAL_SPECIMEN);

		// Then
		assertThat(result).isEqualTo(2L);
	}

	@Test
	void getCountForBatchAnnotationsBlankParam() throws Exception {
		// Given
		var batchMetadata = new BatchMetadata(List.of(new SearchParam("ods:hasEvent[*].dwc:FieldNumber", "")));
		postDigitalSpecimens(parseToElasticFormat(List.of(givenDigitalSpecimenWrapper(ID))));

		// When
		var result = repository.getCountForBatchAnnotations(batchMetadata, AnnotationTargetType.DIGITAL_SPECIMEN);

		// Then
		assertThat(result).isEqualTo(1L);
	}

	private List<JsonNode> parseAnnotationToElasticFormat(List<Annotation> annotations) {
		return annotations.stream().map(this::annotationToElasticFormat).toList();
	}

	private JsonNode toElasticFormat(DigitalSpecimen specimen) {
		var node = (ObjectNode) MAPPER.valueToTree(specimen);
		// topicDiscipline default mapping to text will fail the test, removing it before
		// we run the test
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

	public BulkResponse postDigitalSpecimens(List<JsonNode> digitalSpecimens) throws IOException {
		var bulkRequest = new BulkRequest.Builder();
		for (var digitalSpecimen : digitalSpecimens) {
			bulkRequest.operations(op -> op.index(idx -> idx.index(DIGITAL_SPECIMEN_INDEX)
				.id(digitalSpecimen.get("@id").asString())
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
			bulkRequest.operations(op -> op.index(idx -> idx.index(ANNOTATION_INDEX)
				.id(annotation.get("dcterms:identifier").asString())
				.document(annotation)));
		}
		var response = client.bulk(bulkRequest.build());
		client.indices().refresh(b -> b.index(ANNOTATION_INDEX));
		return response;
	}

}
