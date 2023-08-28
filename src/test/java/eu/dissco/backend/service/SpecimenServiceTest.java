package eu.dissco.backend.service;

import static eu.dissco.backend.TestUtils.HANDLE;
import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.PREFIX;
import static eu.dissco.backend.TestUtils.SOURCE_SYSTEM_ID_1;
import static eu.dissco.backend.TestUtils.USER_ID_TOKEN;
import static eu.dissco.backend.TestUtils.givenAggregationMap;
import static eu.dissco.backend.TestUtils.givenDigitalSpecimen;
import static eu.dissco.backend.domain.MappingTerms.TOPIC_DISCIPLINE;
import static eu.dissco.backend.utils.AnnotationUtils.ANNOTATION_PATH;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationJsonResponseNoPagination;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationResponse;
import static eu.dissco.backend.utils.DigitalMediaObjectUtils.givenDigitalMediaJsonApiData;
import static eu.dissco.backend.utils.DigitalMediaObjectUtils.givenDigitalMediaObject;
import static eu.dissco.backend.utils.MachineAnnotationServiceUtils.*;
import static eu.dissco.backend.utils.MachineAnnotationServiceUtils.givenFlattenedDigitalSpecimen;
import static eu.dissco.backend.utils.MachineAnnotationServiceUtils.givenMasResponse;
import static eu.dissco.backend.utils.SpecimenUtils.SPECIMEN_PATH;
import static eu.dissco.backend.utils.SpecimenUtils.givenDigitalSpecimenJsonApiData;
import static eu.dissco.backend.utils.SpecimenUtils.givenDigitalSpecimenJsonApiDataList;
import static eu.dissco.backend.utils.SpecimenUtils.givenDigitalSpecimenList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mockStatic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import eu.dissco.backend.domain.DigitalMediaObjectFull;
import eu.dissco.backend.domain.DigitalSpecimen;
import eu.dissco.backend.domain.DigitalSpecimenFull;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.domain.jsonapi.JsonApiLinks;
import eu.dissco.backend.domain.jsonapi.JsonApiLinksFull;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiMeta;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.exceptions.UnknownParameterException;
import eu.dissco.backend.repository.ElasticSearchRepository;
import eu.dissco.backend.repository.MongoRepository;
import eu.dissco.backend.repository.SpecimenRepository;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import eu.dissco.backend.utils.MachineAnnotationServiceUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.MultiValueMapAdapter;

@ExtendWith(MockitoExtension.class)
class SpecimenServiceTest {

  @Mock
  private SpecimenRepository repository;
  @Mock
  private ElasticSearchRepository elasticRepository;
  @Mock
  private DigitalMediaObjectService digitalMediaObjectService;
  @Mock
  private AnnotationService annotationService;
  @Mock
  private MachineAnnotationServiceService masService;
  @Mock
  private MongoRepository mongoRepository;

  private SpecimenService service;

  @BeforeEach
  void setup() {
    service = new SpecimenService(MAPPER, repository, elasticRepository, digitalMediaObjectService,
        masService, annotationService, mongoRepository);
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
    var expected = new JsonApiListResponseWrapper(dataNode.subList(0, pageSize), linksNode, new JsonApiMeta(totalResults));
    given(elasticRepository.getSpecimens(pageNum, pageSize)).willReturn(Pair.of(totalResults, digitalSpecimens));

    // When
    var result = service.getSpecimen(pageNum, pageSize, SPECIMEN_PATH);

    // Then
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void testGetSpecimenLastPage() throws Exception{
    // Given
    int pageNum = 1;
    int pageSize = 10;
    var digitalSpecimens = List.of(givenDigitalSpecimen(ID));
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
    var specimens = Collections.nCopies(pageSize + 1, givenDigitalSpecimen(ID));
    var elasticSearchResults = Pair.of( totalResults, specimens);
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
  void testGetLatestSpecimenLastPage() throws IOException {
    // Given
    int pageSize = 10;
    int pageNum = 2;
    long totalResults = 20L;
    var specimens = Collections.nCopies(pageSize, givenDigitalSpecimen(ID));
    var elasticSearchResults = Pair.of( totalResults, specimens);
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
  void testGetSpecimenById() throws JsonProcessingException {
    // Given
    var dataNode = givenDigitalSpecimenJsonApiData(givenDigitalSpecimen(ID));
    given(repository.getLatestSpecimenById(ID)).willReturn(givenDigitalSpecimen(ID));
    var expected = new JsonApiWrapper(dataNode, new JsonApiLinks(ANNOTATION_PATH));

    // When
    var result = service.getSpecimenById(ID, ANNOTATION_PATH);

    // Then
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void testGetSpecimenByIdFull() throws JsonProcessingException {
    // Given
    var specimen = givenDigitalSpecimen(ID);
    var digitalMedia = List.of(new DigitalMediaObjectFull(givenDigitalMediaObject(ID), List.of()));
    var annotations = List.of(givenAnnotationResponse(USER_ID_TOKEN, ID));
    given(repository.getLatestSpecimenById(ID)).willReturn(specimen);
    given(digitalMediaObjectService.getDigitalMediaObjectFull(ID)).willReturn(digitalMedia);
    given(annotationService.getAnnotationForTargetObject(ID)).willReturn(annotations);
    var attributeNode = MAPPER.valueToTree(
        new DigitalSpecimenFull(specimen, digitalMedia, annotations));
    var expected = new JsonApiWrapper(new JsonApiData(ID, specimen.type(), attributeNode),
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
    var digitalMedia = List.of(new DigitalMediaObjectFull(givenDigitalMediaObject(ID), List.of()));
    var annotations = List.of(givenAnnotationResponse(USER_ID_TOKEN, ID));
    given(mongoRepository.getByVersion(ID, version, "digital_specimen_provenance")).willReturn(
        specimen);
    given(digitalMediaObjectService.getDigitalMediaObjectFull(ID)).willReturn(digitalMedia);
    given(annotationService.getAnnotationForTargetObject(ID)).willReturn(annotations);
    var attributeNode = MAPPER.valueToTree(
        new DigitalSpecimenFull(
            givenDigitalSpecimen(HANDLE + ID, "123", version, HANDLE + SOURCE_SYSTEM_ID_1),
            digitalMedia, annotations));
    var expected = new JsonApiWrapper(new JsonApiData(ID, "BotanySpecimen", attributeNode),
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
    var specimen = givenMongoDBResponse();
    given(mongoRepository.getByVersion(ID, version, "digital_specimen_provenance")).willReturn(
        specimen);
    var responseExpected = new JsonApiWrapper(
        new JsonApiData(HANDLE + ID, "BotanySpecimen",
            givenDigitalSpecimen(HANDLE + ID, "123", version, HANDLE + SOURCE_SYSTEM_ID_1),
            MAPPER),
        new JsonApiLinks(SPECIMEN_PATH)
    );

    // When
    var responseReceived = service.getSpecimenByVersion(ID, version, SPECIMEN_PATH);

    // Then
    assertThat(responseReceived).isEqualTo(responseExpected);
  }

  @Test
  void testGetSpecimenVersions() throws NotFoundException {
    // Given
    List<Integer> versionsList = List.of(1, 2);
    given(mongoRepository.getVersions(ID, "digital_specimen_provenance")).willReturn(versionsList);
    var versionsNode = MAPPER.createObjectNode();
    var arrayNode = versionsNode.putArray("versions");
    arrayNode.add(1).add(2);
    var dataNode = new JsonApiData(ID, "digitalSpecimenVersions", versionsNode);
    var responseExpected = new JsonApiWrapper(dataNode, new JsonApiLinks(ANNOTATION_PATH));
    try (var mockedStatic = mockStatic(ServiceUtils.class)) {
      mockedStatic.when(() -> ServiceUtils.createVersionNode(versionsList, MAPPER))
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
  void testGetDigitalMedia() {
    // Given
    var digitalMedia = givenDigitalMediaJsonApiData(ID);
    var digitalMediaList = List.of(digitalMedia);
    var expected = new JsonApiListResponseWrapper(List.of(digitalMedia),
        new JsonApiLinksFull(SPECIMEN_PATH));
    given(digitalMediaObjectService.getDigitalMediaForSpecimen(ID)).willReturn(digitalMediaList);

    // When
    var result = service.getDigitalMedia(ID, SPECIMEN_PATH);

    // Then
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void testSpecimenByIdJsonLD() throws IOException {
    // Given
    var specimens = givenDigitalSpecimen(ID);
    given(repository.getLatestSpecimenById(ID)).willReturn(specimens);
    given(digitalMediaObjectService.getDigitalMediaIdsForSpecimen(ID)).willReturn(
        List.of(PREFIX + "XXX-XXX-YYY"));

    // When
    var result = service.getSpecimenByIdJsonLD(ID);

    // Then
    assertThat(MAPPER.valueToTree(result).toPrettyString()).isEqualTo(givenJsonLDString());
  }

  @Test
  void testSearch() throws IOException, UnknownParameterException {
    // Given
    int pageNum = 1;
    int pageSize = 10;

    var digitalSpecimens = givenDigitalSpecimenJsonApiDataList(pageSize + 1);
    var params = new HashMap<String, List<String>>();
    params.put("q", List.of("Leucanthemum ircutianum"));
    var map = new MultiValueMapAdapter<>(params);
    given(elasticRepository.search(anyMap(), eq(pageNum), eq(pageSize))).willReturn(
        Pair.of(11L, givenDigitalSpecimenList(pageSize + 1)));
    var linksNode = new JsonApiLinksFull(map, pageNum, pageSize, true, SPECIMEN_PATH);
    var expected = new JsonApiListResponseWrapper(digitalSpecimens.subList(0, pageSize), linksNode,
        new JsonApiMeta(11L));

    // When
    var result = service.search(map, SPECIMEN_PATH);

    // Then
    assertThat(result).isEqualTo(expected);
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
    var map = new MultiValueMapAdapter<>(params);
    var mappedParam = Map.of("q", List.of("Leucanthemum ircutianum"));
    given(elasticRepository.search(mappedParam, pageNum, pageSize)).willReturn(
        Pair.of(10L, givenDigitalSpecimenList(pageSize)));
    var linksNode = new JsonApiLinksFull(new MultiValueMapAdapter<>(mappedParam), pageNum, pageSize,
        false, SPECIMEN_PATH);
    var expected = new JsonApiListResponseWrapper(digitalSpecimens, linksNode,
        new JsonApiMeta(10L));

    // When
    var result = service.search(map, SPECIMEN_PATH);

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
    var mappedParam = Map.of("q", List.of("Leucanthemum ircutianum"));
    given(elasticRepository.search(mappedParam, pageNum, pageSize)).willReturn(
        Pair.of(10L, givenDigitalSpecimenList(pageSize)));
    var linksNode = new JsonApiLinksFull(new MultiValueMapAdapter<>(mappedParam), pageNum, pageSize,
        false, SPECIMEN_PATH);
    var expected = new JsonApiListResponseWrapper(digitalSpecimens, linksNode,
        new JsonApiMeta(10L));

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
    var map = new MultiValueMapAdapter<>(params);
    var mappedParam = Map.of("digitalSpecimen.ods:attributes.dwc:country.keyword",
        List.of("France", "Albania"), "digitalSpecimen.ods:attributes.dwc:typeStatus.keyword",
        List.of("holotype"));
    given(elasticRepository.search(mappedParam, pageNum, pageSize)).willReturn(
        Pair.of(10L, givenDigitalSpecimenList(pageSize)));
    var linksNode = new JsonApiLinksFull(new MultiValueMapAdapter<>(params), pageNum, pageSize,
        false, SPECIMEN_PATH);
    var expected = new JsonApiListResponseWrapper(digitalSpecimens, linksNode,
        new JsonApiMeta(10L));

    // When
    var result = service.search(map, SPECIMEN_PATH);

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
    assertThatThrownBy(() -> service.search(map, SPECIMEN_PATH)).isExactlyInstanceOf(
        UnknownParameterException.class).hasMessageContaining("exception");

    // Then
    then(elasticRepository).shouldHaveNoInteractions();
  }

  @Test
  void testAggregation() throws IOException, UnknownParameterException {
    // Given
    var params = new HashMap<String, List<String>>();
    params.put("sourceSystemId", List.of(SOURCE_SYSTEM_ID_1));
    var map = new MultiValueMapAdapter<>(params);
    var aggregationMap = givenAggregationMap();
    given(elasticRepository.getAggregations(anyMap())).willReturn(aggregationMap);
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
    given(elasticRepository.searchTermValue(TOPIC_DISCIPLINE.getName(),
        TOPIC_DISCIPLINE.getFullName(), "Z")).willReturn(aggregation);
    var dataNode = new JsonApiData(String.valueOf(("topicDiscipline" + "Z").hashCode()),
        "aggregations", MAPPER.valueToTree(aggregation));
    var linksNode = new JsonApiLinks(SPECIMEN_PATH);
    var expected = new JsonApiWrapper(dataNode, linksNode);

    // When
    var result = service.searchTermValue("topicDiscipline", "Z", SPECIMEN_PATH);

    // Then
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void testSearchTermUnknown() {
    // When / Then
    assertThatThrownBy(
        () -> service.searchTermValue("unknownTerm", "Z", SPECIMEN_PATH)).isInstanceOf(
        UnknownParameterException.class);
  }

  @Test
  void testGetMas() throws JsonProcessingException {
    // Given
    var digitalSpecimen = givenDigitalSpecimen(ID);
    var response = givenMasResponse(SPECIMEN_PATH);
    given(repository.getLatestSpecimenById(ID)).willReturn(digitalSpecimen);
    given(masService.getMassForObject(givenFlattenedDigitalSpecimen(), SPECIMEN_PATH)).willReturn(
        response);

    // When
    var result = service.getMass(ID, SPECIMEN_PATH);

    // Then
    assertThat(result).isEqualTo(response);
  }

  @Test
  void testScheduleMas() throws JsonProcessingException {
    // Given
    var digitalSpecimen = givenDigitalSpecimen(ID);
    var response = givenMasResponse(SPECIMEN_PATH);
    given(repository.getLatestSpecimenById(ID)).willReturn(digitalSpecimen);
    given(masService.scheduleMass(givenFlattenedDigitalSpecimen(), List.of(ID), SPECIMEN_PATH,
        digitalSpecimen)).willReturn(
        response);

    // When
    var result = service.scheduleMass(ID, List.of(ID), SPECIMEN_PATH);

    // Then
    assertThat(result).isEqualTo(response);
  }

  private String givenJsonLDString() {
    return """
        {
          "@id" : "hdl:20.5000.1025/ABC-123-XYZ",
          "@type" : "BotanySpecimen",
          "@context" : {
            "ods:organisationId" : {
              "@type" : "@id"
            },
            "ods:sourceSystemId" : {
              "@type" : "@id"
            },
            "ods:hasSpecimenMedia" : {
              "@container" : "@list",
              "@type" : "@id"
            },
            "hdl" : "https://hdl.handle.net/",
            "ods" : "http://github.com/DiSSCo/openDS/ods-ontology/terms/",
            "dwca" : "http://rs.tdwg.org/dwc/text/",
            "dcterms" : "http://purl.org/dc/terms/",
            "dwc" : "http://rs.tdwg.org/dwc/terms/"
          },
          "ods:primarySpecimenData" : {
            "ods:midsLevel" : 1,
            "ods:version" : 1,
            "ods:physicalSpecimenId" : "global_id_123123",
            "ods:physicalSpecimenIdType" : "cetaf",
            "ods:specimenName" : "Leucanthemum ircutianum (Turcz.) Turcz.ex DC.",
            "ods:organisationId" : "https://ror.org/0349vqz63",
            "ods:datasetId" : "Royal Botanic Garden Edinburgh Herbarium",
            "ods:physicalSpecimenCollection" : "http://biocol.org/urn:lsid:biocol.org:col:15670",
            "dwca:id" : "http://data.rbge.org.uk/herb/E00586417",
            "ods:modified" : "03/12/2012",
            "ods:objectType" : "",
            "dcterms:license" : "http://creativecommons.org/licenses/by/4.0/legalcode",
            "ods:sourceSystemId" : "20.5000.1025/3XA-8PT-SAY",
            "dwc:typeStatus" : "holotype",
            "dwc:country" : "Scotland",
            "ods:hasMedia" : "true"
          },
          "ods:sourceSystemId" : "hdl:20.5000.1025/3XA-8PT-SAY",
          "ods:hasSpecimenMedia" : [ "hdl:20.5000.1025XXX-XXX-YYY" ]
        }""";
  }


  private JsonNode givenMongoDBResponse() throws JsonProcessingException {
    return MAPPER.readValue("""
           {
          "id": "20.5000.1025/ABC-123-XYZ",
          "midsLevel": 1,
          "version": 4,
          "created": 1667296764,
          "digitalSpecimen": {
            "ods:physicalSpecimenId": "123",
            "ods:type": "BotanySpecimen",
            "ods:attributes": {
              "dwca:id": "http://data.rbge.org.uk/herb/E00586417",
              "ods:modified": "03/12/2012",
              "ods:datasetId": "Royal Botanic Garden Edinburgh Herbarium",
              "ods:objectType": "",
              "dcterms:license": "http://creativecommons.org/licenses/by/4.0/legalcode",
              "ods:specimenName": "Leucanthemum ircutianum (Turcz.) Turcz.ex DC.",
              "ods:organisationId": "https://ror.org/0349vqz63",
              "ods:sourceSystemId": "20.5000.1025/3XA-8PT-SAY",
              "ods:physicalSpecimenIdType": "cetaf",
              "ods:physicalSpecimenCollection": "http://biocol.org/urn:lsid:biocol.org:col:15670",
              "dwc:typeStatus": "holotype",
              "dwc:country": "Scotland",
              "ods:hasMedia": "true"
            },
            "ods:originalAttributes": {
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
            }
          }
        }
                  """, JsonNode.class);
  }


}
