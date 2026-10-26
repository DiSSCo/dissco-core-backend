package eu.dissco.backend.service;

import eu.dissco.backend.database.jooq.enums.MjrTargetType;
import eu.dissco.backend.domain.DigitalMediaFull;
import eu.dissco.backend.domain.DigitalSpecimenFull;
import eu.dissco.backend.domain.FdoType;
import eu.dissco.backend.domain.MongoCollection;
import eu.dissco.backend.domain.elastic.DefaultMappingTerms;
import eu.dissco.backend.domain.elastic.TaxonMappingTerms;
import eu.dissco.backend.domain.jsonapi.*;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.exceptions.UnknownParameterException;
import eu.dissco.backend.repository.DigitalSpecimenRepository;
import eu.dissco.backend.repository.ElasticSearchRepository;
import eu.dissco.backend.repository.MongoRepository;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.MultiValueMapAdapter;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.*;

import static eu.dissco.backend.TestUtils.*;
import static eu.dissco.backend.domain.elastic.DefaultMappingTerms.TOPIC_DISCIPLINE;
import static eu.dissco.backend.utils.AnnotationUtils.*;
import static eu.dissco.backend.utils.DigitalMediaObjectUtils.givenDigitalMediaObject;
import static eu.dissco.backend.utils.MachineAnnotationServiceUtils.givenMasJobRequest;
import static eu.dissco.backend.utils.MachineAnnotationServiceUtils.givenMasResponse;
import static eu.dissco.backend.utils.SpecimenUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class DigitalSpecimenServiceTest {

	@Mock
	private DigitalSpecimenRepository repository;

	@Mock
	private ElasticSearchRepository elasticRepository;

	@Mock
	private DigitalMediaService digitalMediaService;

	@Mock
	private AnnotationService annotationService;

	@Mock
	private MachineAnnotationServiceService masService;

	@Mock
	private MongoRepository mongoRepository;

	@Mock
	private MasJobRecordService masJobRecordService;

	private DigitalSpecimenService service;

	@BeforeEach
	void setup() {
		service = new DigitalSpecimenService(MAPPER, repository, elasticRepository, digitalMediaService, masService,
				annotationService, mongoRepository, masJobRecordService);
	}

	@Test
	void testGetSpecimen() throws Exception {
		// Given
		int pageNum = 1;
		int pageSize = 10;
		var digitalSpecimens = givenDigitalSpecimenList(pageSize + 1);
		var totalResults = 20L;
		var dataNode = givenDigitalSpecimenJsonApiData(digitalSpecimens);
		var linksNode = new JsonApiLinksFull(pageNum, pageSize, true, SPECIMEN_PATH);
		var expected = new JsonApiListResponseWrapper(dataNode.subList(0, pageSize), linksNode,
				new JsonApiMeta(totalResults));
		given(elasticRepository.getSpecimens(pageNum, pageSize)).willReturn(Pair.of(totalResults, digitalSpecimens));

		// When
		var result = service.getSpecimen(pageNum, pageSize, SPECIMEN_PATH);

		// Then
		assertThat(result).isEqualTo(expected);
	}

	@Test
	void testGetSpecimenLastPage() throws Exception {
		// Given
		int pageNum = 1;
		int pageSize = 10;
		var digitalSpecimens = List.of(givenDigitalSpecimenWrapper(ID));
		var dataNode = givenDigitalSpecimenJsonApiData(digitalSpecimens);
		var linksNode = new JsonApiLinksFull(pageNum, pageSize, false, SPECIMEN_PATH);
		var totalResults = 10L;
		var expected = new JsonApiListResponseWrapper(dataNode, linksNode, new JsonApiMeta(totalResults));
		given(elasticRepository.getSpecimens(pageNum, pageSize)).willReturn(Pair.of(totalResults, digitalSpecimens));

		// When
		var result = service.getSpecimen(pageNum, pageSize, SPECIMEN_PATH);

		// Then
		assertThat(result).isEqualTo(expected);
	}

	@Test
	void testGetLatestSpecimen() throws IOException {
		// Given
		int pageSize = 10;
		int pageNum = 1;
		var totalResults = 20L;
		var specimens = Collections.nCopies(pageSize + 1, givenDigitalSpecimenWrapper(ID));
		var elasticSearchResults = Pair.of(totalResults, specimens);
		var dataNode = givenDigitalSpecimenJsonApiData(specimens);
		given(elasticRepository.getLatestSpecimen(pageNum, pageSize)).willReturn(elasticSearchResults);
		var expected = new JsonApiListResponseWrapper(dataNode.subList(0, pageSize),
				new JsonApiLinksFull(pageNum, pageSize, true, SPECIMEN_PATH), new JsonApiMeta(totalResults));

		// When
		var result = service.getLatestSpecimen(pageNum, pageSize, SPECIMEN_PATH);

		// Then
		assertThat(result).isEqualTo(expected);
	}

	@Test
	void testGetLatestSpecimenPage2() throws IOException {
		// Given
		int pageSize = 10;
		int pageNum = 2;
		var totalResults = 20L;
		var specimens = Collections.nCopies(pageSize + 1, givenDigitalSpecimenWrapper(ID));
		var elasticSearchResults = Pair.of(totalResults, specimens);
		var dataNode = givenDigitalSpecimenJsonApiData(specimens);
		var path = SPECIMEN_PATH + "?pageNumber=" + pageNum;
		given(elasticRepository.getLatestSpecimen(pageNum, pageSize)).willReturn(elasticSearchResults);
		var expected = new JsonApiListResponseWrapper(dataNode.subList(0, pageSize),
				new JsonApiLinksFull(pageNum, pageSize, true, path), new JsonApiMeta(totalResults));

		// When
		var result = service.getLatestSpecimen(pageNum, pageSize, path);

		// Then
		assertThat(result).isEqualTo(expected);
	}

	@Test
	void testGetLatestSpecimenLastPage() throws IOException {
		// Given
		int pageSize = 10;
		int pageNum = 2;
		long totalResults = 20L;
		var specimens = Collections.nCopies(pageSize, givenDigitalSpecimenWrapper(ID));
		var elasticSearchResults = Pair.of(totalResults, specimens);
		var dataNode = givenDigitalSpecimenJsonApiData(specimens);
		given(elasticRepository.getLatestSpecimen(pageNum, pageSize)).willReturn(elasticSearchResults);
		var expected = new JsonApiListResponseWrapper(dataNode,
				new JsonApiLinksFull(pageNum, pageSize, false, SPECIMEN_PATH), new JsonApiMeta(totalResults));

		// When
		var result = service.getLatestSpecimen(pageNum, pageSize, SPECIMEN_PATH);

		// Then
		assertThat(result).isEqualTo(expected);
	}

	@Test
	void testGetSpecimenById() throws NotFoundException {
		// Given
		var dataNode = givenDigitalSpecimenJsonApiData(givenDigitalSpecimenWrapper(ID));
		given(repository.getLatestSpecimenById(ID)).willReturn(givenDigitalSpecimenWrapper(ID));
		var expected = new JsonApiWrapper(dataNode, new JsonApiLinks(ANNOTATION_PATH));

		// When
		var result = service.getSpecimenById(ID, ANNOTATION_PATH);

		// Then
		assertThat(result).isEqualTo(expected);
	}

	@Test
	void testGetSpecimenByIdNotFound() {
		// Given
		given(repository.getLatestSpecimenById(ID)).willReturn(null);

		// When / Then
		assertThrows(NotFoundException.class, () -> service.getSpecimenById(ID, ANNOTATION_PATH));

	}

	@Test
	void testGetSpecimenByIdFull() throws NotFoundException {
		// Given
		var digitalSpecimen = givenDigitalSpecimenWrapper(ID);
		var digitalMedia = List.of(new DigitalMediaFull(givenDigitalMediaObject(ID), List.of()));
		var annotations = List.of(givenAnnotationResponse(ORCID_ALT, ID));
		given(repository.getLatestSpecimenById(ID)).willReturn(digitalSpecimen);
		given(digitalMediaService.getFullDigitalMediaFromSpecimen(digitalSpecimen)).willReturn(digitalMedia);
		given(annotationService.getAnnotationForTargetObject(ID)).willReturn(annotations);
		var attributeNode = MAPPER.valueToTree(new DigitalSpecimenFull(digitalSpecimen, digitalMedia, annotations));
		var expected = new JsonApiWrapper(new JsonApiData(ID, FdoType.DIGITAL_SPECIMEN.getName(), attributeNode),
				new JsonApiLinks(ANNOTATION_PATH));

		// When
		var result = service.getSpecimenByIdFull(ID, ANNOTATION_PATH);

		// Then
		assertThat(result).isEqualTo(expected);
	}

	@Test
	void testGetSpecimenByVersionFull() throws Exception {
		// Given
		int version = 4;
		var specimen = givenMongoDBResponse();
		var digitalMedia = List.of(new DigitalMediaFull(givenDigitalMediaObject(DOI + ID), List.of()));
		var annotations = List.of(givenAnnotationResponse(ORCID_ALT, ID));
		given(mongoRepository.getByVersion(ID, version, MongoCollection.DIGITAL_SPECIMEN)).willReturn(specimen);
		given(digitalMediaService.getFullDigitalMediaFromSpecimen(any())).willReturn(digitalMedia);
		given(annotationService.getAnnotationForTargetObject(ID)).willReturn(annotations);
		var digitalSpecimenWrapper = givenDigitalSpecimenWrapper(DOI + ID, "123", version, SOURCE_SYSTEM_ID_1,
				SPECIMEN_NAME);
		var attributeNode = MAPPER
			.valueToTree(new DigitalSpecimenFull(digitalSpecimenWrapper, digitalMedia, annotations));
		var expected = new JsonApiWrapper(new JsonApiData(DOI + ID, FdoType.DIGITAL_SPECIMEN.getName(), attributeNode),
				new JsonApiLinks(ANNOTATION_PATH));

		// When
		var result = service.getSpecimenByVersionFull(ID, 4, ANNOTATION_PATH);

		// Then
		assertThat(result).isEqualTo(expected);
	}

	@Test
	void testSpecimenByVersion() throws Exception {
		// Given
		int version = 4;
		var givenSpecimenWrapper = givenDigitalSpecimenWrapper(DOI + ID, "123", version, SOURCE_SYSTEM_ID_1,
				SPECIMEN_NAME);
		var specimen = givenMongoDBResponse();
		given(mongoRepository.getByVersion(ID, version, MongoCollection.DIGITAL_SPECIMEN)).willReturn(specimen);
		var responseExpected = new JsonApiWrapper(
				new JsonApiData(DOI + ID, FdoType.DIGITAL_SPECIMEN.getName(), givenSpecimenWrapper, MAPPER),
				new JsonApiLinks(SPECIMEN_PATH));

		// When
		var responseReceived = service.getSpecimenByVersion(ID, version, SPECIMEN_PATH);

		// Then
		assertThat(responseReceived).isEqualTo(responseExpected);
	}

	@Test
	void testGetDigitalMediaFromSpecimen() throws NotFoundException {
		// Given
		given(repository.getLatestSpecimenById(ID)).willReturn(givenDigitalSpecimenWrapper(ID));
		given(digitalMediaService.getDigitalMediaFromSpecimen(givenDigitalSpecimenWrapper(ID)))
			.willReturn(List.of(givenDigitalMediaObject(ID_ALT)));
		var expected = new JsonApiListResponseWrapper(List
			.of(new JsonApiData(ID_ALT, FdoType.DIGITAL_MEDIA.getName(), givenDigitalMediaObject(ID_ALT), MAPPER)),
				new JsonApiLinksFull(SPECIMEN_PATH));

		// When
		var result = service.getDigitalMedia(ID, SPECIMEN_PATH);

		// Then
		assertThat(result).isEqualTo(expected);
	}

	@Test
	void testGetDigitalMediaFromSpecimenNotFound() {
		// Given

		// When / then
		assertThrows(NotFoundException.class, () -> service.getDigitalMedia(ID, SPECIMEN_PATH));
	}

	@Test
	void testGetSpecimenVersions() throws NotFoundException {
		// Given
		List<Integer> versionsList = List.of(1, 2);
		given(mongoRepository.getVersions(ID, MongoCollection.DIGITAL_SPECIMEN)).willReturn(versionsList);
		var versionsNode = MAPPER.createObjectNode();
		var arrayNode = versionsNode.putArray("versions");
		arrayNode.add(1).add(2);
		var dataNode = new JsonApiData(ID, "digitalSpecimenVersions", versionsNode);
		var responseExpected = new JsonApiWrapper(dataNode, new JsonApiLinks(ANNOTATION_PATH));
		try (var mockedStatic = mockStatic(DigitalServiceUtils.class)) {
			mockedStatic.when(() -> DigitalServiceUtils.createVersionNode(versionsList, MAPPER))
				.thenReturn(versionsNode);

			// When
			var responseReceived = service.getSpecimenVersions(ID, ANNOTATION_PATH);

			// Then
			assertThat(responseReceived).isEqualTo(responseExpected);
		}
	}

	@Test
	void testGetAnnotations() {
		// Given
		var expected = givenAnnotationJsonResponseNoPagination(ANNOTATION_PATH, List.of(ID));
		given(annotationService.getAnnotationForTarget(ID, ANNOTATION_PATH)).willReturn(expected);

		// When
		var result = service.getAnnotations(ID, ANNOTATION_PATH);

		// Then
		assertThat(result).isEqualTo(expected);
	}

	@Test
	void testSearch() throws IOException, UnknownParameterException {
		// Given
		int pageNum = 1;
		int pageSize = 10;

		var digitalSpecimens = givenDigitalSpecimenJsonApiDataList(pageSize + 1);
		var params = new HashMap<String, List<String>>();
		params.put("q", List.of("Leucanthemum ircutianum"));
		params.put("hasCountry", List.of("true"));
		var map = new MultiValueMapAdapter<>(params);
		given(elasticRepository.search(anyMap(), eq(pageNum), eq(pageSize)))
			.willReturn(Pair.of(11L, givenDigitalSpecimenList(pageSize + 1)));
		var linksNode = new JsonApiLinksFull(pageNum, pageSize, true, SPECIMEN_PATH);
		var expected = new JsonApiListResponseWrapper(digitalSpecimens.subList(0, pageSize), linksNode,
				new JsonApiMeta(11L));

		// When
		var result = service.search(map, SPECIMEN_PATH);

		// Then
		assertThat(result).isEqualTo(expected);
	}

	@Test
	void testSearchTermValueInvalidExistsValue() {
		var params = new HashMap<String, List<String>>();
		params.put("hasCountry", List.of("Leucanthemum ircutianum"));
		var map = new MultiValueMapAdapter<>(params);

		// When / then
		assertThrows(UnknownParameterException.class, () -> service.search(map, SPECIMEN_PATH));
	}

	@Test
	void testSearchTermValueTooManyExistsValue() {
		var params = new HashMap<String, List<String>>();
		params.put("hasCountry", List.of("true", "false"));
		var map = new MultiValueMapAdapter<>(params);

		// When / then
		assertThrows(UnknownParameterException.class, () -> service.search(map, SPECIMEN_PATH));
	}

	@Test
	void testSearchLastPage() throws IOException, UnknownParameterException {
		// Given
		int pageNum = 2;
		int pageSize = 10;
		var digitalSpecimens = givenDigitalSpecimenJsonApiDataList(pageSize);
		var params = new HashMap<String, List<String>>();
		params.put("q", List.of("Leucanthemum ircutianum"));
		params.put("pageNumber", List.of("2"));
		var path = SPECIMEN_PATH + "?q=Leucanthemum ircutianum";
		var map = new MultiValueMapAdapter<>(params);
		var mappedParam = Map.of(DefaultMappingTerms.QUERY, List.of("Leucanthemum ircutianum"));
		given(elasticRepository.search(mappedParam, pageNum, pageSize))
			.willReturn(Pair.of(10L, givenDigitalSpecimenList(pageSize)));
		var linksNode = new JsonApiLinksFull(pageNum, pageSize, false, path);
		var expected = new JsonApiListResponseWrapper(digitalSpecimens, linksNode, new JsonApiMeta(10L));

		// When
		var result = service.search(map, path);

		// Then
		assertThat(result).isEqualTo(expected);
	}

	@Test
	void testInvalidPageNumber() throws IOException, UnknownParameterException {
		// Given
		int pageNum = 1;
		int pageSize = 10;
		var digitalSpecimens = givenDigitalSpecimenJsonApiDataList(pageSize);
		var params = new HashMap<String, List<String>>();
		params.put("q", List.of("Leucanthemum ircutianum"));
		params.put("pageNumber", List.of("randomString", "anotherRandomString"));
		var map = new MultiValueMapAdapter<>(params);
		var mappedParam = Map.of(DefaultMappingTerms.QUERY, List.of("Leucanthemum ircutianum"));
		given(elasticRepository.search(mappedParam, pageNum, pageSize))
			.willReturn(Pair.of(10L, givenDigitalSpecimenList(pageSize)));
		var linksNode = new JsonApiLinksFull(pageNum, pageSize, false, SPECIMEN_PATH);
		var expected = new JsonApiListResponseWrapper(digitalSpecimens, linksNode, new JsonApiMeta(10L));

		// When
		var result = service.search(map, SPECIMEN_PATH);

		// Then
		assertThat(result).isEqualTo(expected);
	}

	@Test
	void testSearchWithParameters() throws IOException, UnknownParameterException {
		// Given
		int pageNum = 1;
		int pageSize = 10;
		var digitalSpecimens = givenDigitalSpecimenJsonApiDataList(pageSize);
		var params = new LinkedHashMap<String, List<String>>();
		params.put("country", List.of("France", "Albania"));
		params.put("typeStatus", List.of("holotype"));
		var path = SPECIMEN_PATH + "?country=France&country=Albania&typeStatus=holotype";
		var map = new MultiValueMapAdapter<>(params);
		var mappedParam = Map.of(DefaultMappingTerms.COUNTRY, List.of("France", "Albania"),
				DefaultMappingTerms.TYPE_STATUS, List.of("holotype"));
		given(elasticRepository.search(mappedParam, pageNum, pageSize))
			.willReturn(Pair.of(10L, givenDigitalSpecimenList(pageSize)));
		var linksNode = new JsonApiLinksFull(pageNum, pageSize, false, path);
		var expected = new JsonApiListResponseWrapper(digitalSpecimens, linksNode, new JsonApiMeta(10L));

		// When
		var result = service.search(map, path);

		// Then
		assertThat(result).isEqualTo(expected);
	}

	@Test
	void testSearchException() {
		// Given
		var params = new HashMap<String, List<String>>();
		params.put("exception", List.of("Leucanthemum ircutianum"));
		var map = new MultiValueMapAdapter<>(params);

		// When
		assertThatThrownBy(() -> service.search(map, SPECIMEN_PATH))
			.isExactlyInstanceOf(UnknownParameterException.class)
			.hasMessageContaining("exception");

		// Then
		then(elasticRepository).shouldHaveNoInteractions();
	}

	@Test
	void testAggregation() throws IOException, UnknownParameterException {
		// Given
		var params = new HashMap<String, List<String>>();
		params.put("sourceSystemID", List.of(SOURCE_SYSTEM_ID_1));
		var map = new MultiValueMapAdapter<>(params);
		var aggregationMap = givenAggregationMap();
		given(elasticRepository.getAggregations(anyMap(), anySet(), eq(false))).willReturn(aggregationMap);
		var dataNode = new JsonApiData(String.valueOf(params.hashCode()), "aggregations",
				MAPPER.valueToTree(aggregationMap));
		var linksNode = new JsonApiLinks(SPECIMEN_PATH);
		var expected = new JsonApiWrapper(dataNode, linksNode);

		// When
		var result = service.aggregations(map, SPECIMEN_PATH);

		// Then
		assertThat(result).isEqualTo(expected);
	}

	@Test
	void testTaxonAggregation() throws IOException, UnknownParameterException {
		// Given
		var params = new HashMap<String, List<String>>();
		params.put("kingdom", List.of("animalia"));
		var map = new MultiValueMapAdapter<>(params);
		var aggregationMap = givenTaxonAggregationMap();
		given(elasticRepository.getAggregations(Map.of(TaxonMappingTerms.KINGDOM, List.of("animalia")),
				Set.of(TaxonMappingTerms.KINGDOM, TaxonMappingTerms.PHYLUM), true))
			.willReturn(aggregationMap);
		var dataNode = new JsonApiData(String.valueOf(params.hashCode()), "aggregations",
				MAPPER.valueToTree(aggregationMap));
		var linksNode = new JsonApiLinks(SPECIMEN_PATH);
		var expected = new JsonApiWrapper(dataNode, linksNode);

		// When
		var result = service.taxonAggregations(map, SPECIMEN_PATH);

		// Then
		assertThat(result).isEqualTo(expected);
	}

	@Test
	void testTermAggregation() throws IOException, UnknownParameterException {
		// Given
		var params = new HashMap<String, List<String>>();
		params.put("country", List.of("Netherlands"));
		params.put("midsLevel", List.of("1"));
		params.put("prefix", List.of("neth"));
		var map = new MultiValueMapAdapter<>(params);
		var aggregationMap = Map.of("country", Map.of("Netherlands", 1L));
		given(elasticRepository.getTermAggregation(Map.of(DefaultMappingTerms.MIDS_LEVEL, List.of("1")),
				DefaultMappingTerms.COUNTRY, "neth", false))
			.willReturn(aggregationMap);
		var dataNode = new JsonApiData(String.valueOf(("country" + map).hashCode()), "aggregations",
				MAPPER.valueToTree(aggregationMap));
		var expected = new JsonApiWrapper(dataNode, new JsonApiLinks(SPECIMEN_PATH));

		// When
		var result = service.termAggregations("country", "neth", map, SPECIMEN_PATH);

		// Then
		assertThat(result).isEqualTo(expected);
	}

	@ParameterizedTest
	@ValueSource(strings = { "unknownTerm", "q", "latitude", "longitude", "hasCountry" })
	void testTermAggregationNotAggregatable(String term) {
		// Given
		var map = new MultiValueMapAdapter<>(new HashMap<String, List<String>>());

		// When / Then
		assertThatThrownBy(() -> service.termAggregations(term, null, map, SPECIMEN_PATH))
			.isInstanceOf(UnknownParameterException.class);
		then(elasticRepository).shouldHaveNoInteractions();
	}

	@ParameterizedTest
	@ValueSource(strings = { "midsLevel", "hasMedia" })
	void testTermAggregationPrefixOnNonKeywordTerm(String term) {
		// Given
		var map = new MultiValueMapAdapter<>(new HashMap<String, List<String>>());

		// When / Then
		assertThatThrownBy(() -> service.termAggregations(term, "1", map, SPECIMEN_PATH))
			.isInstanceOf(UnknownParameterException.class);
		then(elasticRepository).shouldHaveNoInteractions();
	}

	@Test
	void testTermAggregationMultiplePrefixes() {
		// Given
		var params = new HashMap<String, List<String>>();
		params.put("prefix", List.of("neth", "fin"));
		var map = new MultiValueMapAdapter<>(params);

		// When / Then
		assertThatThrownBy(() -> service.termAggregations("country", "neth,fin", map, SPECIMEN_PATH))
			.isInstanceOf(UnknownParameterException.class);
		then(elasticRepository).shouldHaveNoInteractions();
	}

	@Test
	void testTermAggregationNonKeywordTerm() throws IOException, UnknownParameterException {
		// Given
		var map = new MultiValueMapAdapter<>(new HashMap<String, List<String>>());
		var aggregationMap = Map.of("hasMedia", Map.of("true", 2L, "false", 1L));
		given(elasticRepository.getTermAggregation(Map.of(), DefaultMappingTerms.HAS_MEDIA, null, false))
			.willReturn(aggregationMap);
		var dataNode = new JsonApiData(String.valueOf(("hasMedia" + map).hashCode()), "aggregations",
				MAPPER.valueToTree(aggregationMap));
		var expected = new JsonApiWrapper(dataNode, new JsonApiLinks(SPECIMEN_PATH));

		// When
		var result = service.termAggregations("hasMedia", null, map, SPECIMEN_PATH);

		// Then
		assertThat(result).isEqualTo(expected);
	}

	@Test
	void testDiscipline() throws IOException {
		// Given
		var aggregation = Pair.of(352009L,
				Map.of("topicDiscipline", Map.of("Zoology", 349555L, "Astrogeology", 2454L)));
		given(elasticRepository.getAggregation(TOPIC_DISCIPLINE)).willReturn(aggregation);
		var dataNode = new JsonApiData(String.valueOf(SPECIMEN_PATH.hashCode()), "aggregations",
				MAPPER.valueToTree(aggregation.getRight()));
		var linksNode = new JsonApiLinks(SPECIMEN_PATH);
		var expected = new JsonApiWrapper(dataNode, linksNode, new JsonApiMeta(352009L));

		// When
		var result = service.discipline(SPECIMEN_PATH);

		// Then
		assertThat(result).isEqualTo(expected);
	}

	@Test
	void testSearchTermValue() throws IOException, UnknownParameterException {
		// Given
		var aggregation = Map.of("topicDiscipline", Map.of("Zoology", 349555L));
		given(elasticRepository.getTermAggregation(Map.of(), TOPIC_DISCIPLINE, "Z", false)).willReturn(aggregation);
		var dataNode = new JsonApiData(String.valueOf(("topicDiscipline" + "Z").hashCode()), "aggregations",
				MAPPER.valueToTree(aggregation));
		var linksNode = new JsonApiLinks(SPECIMEN_PATH);
		var expected = new JsonApiWrapper(dataNode, linksNode);

		// When
		var result = service.searchTermValue("topicDiscipline", "Z", SPECIMEN_PATH, false);

		// Then
		assertThat(result).isEqualTo(expected);
	}

	@ParameterizedTest
	@ValueSource(strings = { "unknownTerm", "q", "latitude", "hasCountry", "midsLevel", "hasMedia" })
	void testSearchTermNotSupported(String term) {
		// When / Then
		assertThatThrownBy(() -> service.searchTermValue(term, "Z", SPECIMEN_PATH, false))
			.isInstanceOf(UnknownParameterException.class);
		then(elasticRepository).shouldHaveNoInteractions();
	}

	@Test
	void testGetMas() throws NotFoundException {
		// Given
		var digitalSpecimen = givenDigitalSpecimenWrapper(ID);
		var response = givenMasResponse(SPECIMEN_PATH);
		given(repository.getLatestSpecimenById(ID)).willReturn(digitalSpecimen);
		given(masService.getMassForObject(any(JsonNode.class), eq(SPECIMEN_PATH))).willReturn(response);

		// When
		var result = service.getMass(ID, SPECIMEN_PATH);

		// Then
		assertThat(result).isEqualTo(response);
	}

	@Test
	void testGetMassNotFound() {
		// Given

		// When / then
		assertThrows(NotFoundException.class, () -> service.getMass(ID, SPECIMEN_PATH));
	}

	@Test
	void testScheduleMas() throws Exception {
		// Given

		// When
		service.scheduleMass(ID, List.of(givenMasJobRequest()), ORCID, SANDBOX_URI);

		// Then
		then(masService).should()
			.scheduleMas(ID, List.of(givenMasJobRequest()), ORCID, MjrTargetType.DIGITAL_SPECIMEN, SANDBOX_URI);
	}

	@Test
	void testGetOriginalDataForSpecimen() throws NotFoundException {
		// Given
		var expectedJson = givenMongoDBResponse();
		var expected = new JsonApiWrapper(new JsonApiData(ID, FdoType.DIGITAL_SPECIMEN.getName(), expectedJson),
				new JsonApiLinks(SANDBOX_URI));
		given(repository.getSpecimenOriginalData(ID)).willReturn(expectedJson);

		// When
		var result = service.getOriginalDataForSpecimen(ID, SANDBOX_URI);

		// Then
		assertThat(result).isEqualTo(expected);
	}

	@Test
	void testGetOriginalDataForSpecimenNotFound() {
		// Given

		// When / then
		assertThrows(NotFoundException.class, () -> service.getOriginalDataForSpecimen(ID, SPECIMEN_PATH));
	}

	private JsonNode givenMongoDBResponse() {
		return MAPPER.readValue("""
				{
				       "@id": "https://doi.org/20.5000.1025/ABC-123-XYZ",
				       "@type": "ods:DigitalSpecimen",
				       "dcterms:identifier": "https://doi.org/20.5000.1025/ABC-123-XYZ",
				       "ods:fdoType": "https://doi.org/21.T11148/894b1e6cad57e921764e",
				       "ods:midsLevel": 0,
				       "ods:version": 4,
				       "dcterms:created": "2022-11-01T09:59:24.000Z",
				       "ods:physicalSpecimenID": "123",
				       "ods:physicalSpecimenIDType": "Resolvable",
				       "ods:isMarkedAsType": true,
				       "ods:isKnownToContainMedia": true,
				       "ods:specimenName": "Abyssothyris Thomson, 1927",
				       "ods:sourceSystemID": "https://hdl.handle.net/20.5000.1025/3XA-8PT-SAY",
				       "dcterms:license": "http://creativecommons.org/licenses/by/4.0/legalcode",
				       "dcterms:modified": "03/12/2012",
				       "ods:organisationID": "https://ror.org/0349vqz63",
				       "ods:organisationName": "Royal Botanic Garden Edinburgh Herbarium",
				       "dwc:datasetName": "Royal Botanic Garden Edinburgh Herbarium",
				       "ods:hasEvents": [
				               {
				                  "ods:hasAssertions": [],
				                  "ods:hasLocation": {
				                     "dwc:country": "Scotland"
				                       }
				                   }
				               ]
				   }
				""", JsonNode.class);
	}

}
