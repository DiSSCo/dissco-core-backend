package eu.dissco.backend.service;

import static eu.dissco.backend.domain.MappingTerms.TOPIC_DISCIPLINE;
import static eu.dissco.backend.domain.MappingTerms.getMappedTerm;
import static eu.dissco.backend.repository.RepositoryUtils.DOI_STRING;
import static eu.dissco.backend.service.ServiceUtils.createVersionNode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.dissco.backend.domain.MasJobState;
import eu.dissco.backend.domain.DigitalSpecimenFull;
import eu.dissco.backend.domain.DigitalSpecimenJsonLD;
import eu.dissco.backend.domain.DigitalSpecimenWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.domain.jsonapi.JsonApiLinks;
import eu.dissco.backend.domain.jsonapi.JsonApiLinksFull;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiMeta;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import eu.dissco.backend.exceptions.ForbiddenException;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.exceptions.UnknownParameterException;
import eu.dissco.backend.repository.ElasticSearchRepository;
import eu.dissco.backend.repository.MongoRepository;
import eu.dissco.backend.repository.SpecimenRepository;
import eu.dissco.backend.schema.DigitalSpecimen;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpecimenService {

  private static final String DEFAULT_PAGE_NUM = "1";
  private static final String DEFAULT_PAGE_SIZE = "10";
  private static final String MONGODB_COLLECTION_NAME = "digital_specimen_provenance";
  private static final String AGGREGATIONS_TYPE = "aggregations";
  private final Map<String, String> prefixMap = Map.of(
      "dct", "https://purl.org/dc/terms/",
      "dwc", "https://rs.tdwg.org/dwc/terms/",
      "dwca", "https://rs.tdwg.org/dwc/text/",
      "abcd", "https://abcd.tdwg.org/terms/",
      "abcd-efg", "https://terms.tdwg.org/wiki/ABCD_EFG/",
      "ods", "https://github.com/DiSSCo/openDS/ods-ontology/terms/",
      "hdl", "https://hdl.handle.net/",
      "dcterms", "https://purl.org/dc/terms/",
      "ac", "https://rs.tdwg.org/ac/terms/");
  private final ObjectMapper mapper;
  private final SpecimenRepository repository;
  private final ElasticSearchRepository elasticRepository;
  private final DigitalMediaObjectService digitalMediaObjectService;
  private final MachineAnnotationServiceService masService;
  private final AnnotationService annotationService;
  private final MongoRepository mongoRepository;
  private final MasJobRecordService masJobRecordService;
  private final UserService userService;

  public JsonApiListResponseWrapper getSpecimen(int pageNumber, int pageSize, String path)
      throws IOException {
    var elasticSearchResults = elasticRepository.getSpecimens(pageNumber, pageSize);
    return wrapListResponse(elasticSearchResults, pageSize, pageNumber, path);
  }

  public JsonApiListResponseWrapper getLatestSpecimen(int pageNumber, int pageSize, String path)
      throws IOException {
    var elasticSearchResults = elasticRepository.getLatestSpecimen(pageNumber, pageSize);
    return wrapListResponse(elasticSearchResults, pageSize, pageNumber, path);
  }

  public JsonApiWrapper getSpecimenById(String id, String path) {
    var digitalSpecimen = repository.getLatestSpecimenById(id);
    var dataNode = new JsonApiData(digitalSpecimen.digitalSpecimen().getOdsId(),
        digitalSpecimen.digitalSpecimen().getOdsType(), digitalSpecimen,
        mapper);
    return new JsonApiWrapper(dataNode, new JsonApiLinks(path));
  }

  public JsonApiWrapper getSpecimenByIdFull(String id, String path) {
    var digitalSpecimen = repository.getLatestSpecimenById(id);
    return mapFullSpecimen(id, path, digitalSpecimen);
  }

  public JsonApiWrapper getSpecimenByVersionFull(String id, int version, String path)
      throws NotFoundException, JsonProcessingException {
    var specimenNode = mongoRepository.getByVersion(id, version, MONGODB_COLLECTION_NAME);
    var specimen = mapResultToSpecimen(specimenNode);
    return mapFullSpecimen(id, path, specimen);
  }

  public JsonApiListResponseWrapper getMasJobRecordsForSpecimen(String targetId,
      MasJobState state, String path, int pageNum, int pageSize) throws NotFoundException {
    return masJobRecordService.getMasJobRecordByTargetId(targetId, state, path, pageNum, pageSize);
  }

  private JsonApiWrapper mapFullSpecimen(String id, String path,
      DigitalSpecimenWrapper specimen) {
    var digitalMedia = digitalMediaObjectService.getDigitalMediaObjectFull(id);
    var annotation = annotationService.getAnnotationForTargetObject(id);
    var attributeNode = mapper.valueToTree(
        new DigitalSpecimenFull(specimen.digitalSpecimen(), specimen.originalData(),
            digitalMedia, annotation));
    return new JsonApiWrapper(
        new JsonApiData(specimen.digitalSpecimen().getOdsId(),
            specimen.digitalSpecimen().getOdsType(), attributeNode),
        new JsonApiLinks(path));
  }

  public JsonApiWrapper getSpecimenByVersion(String id, int version, String path)
      throws JsonProcessingException, NotFoundException {
    var specimenNode = mongoRepository.getByVersion(id, version, MONGODB_COLLECTION_NAME);
    var specimen = mapResultToSpecimen(specimenNode);
    var dataNode = new JsonApiData(specimen.digitalSpecimen().getOdsId(),
        specimen.digitalSpecimen().getOdsType(), specimen, mapper);
    return new JsonApiWrapper(dataNode, new JsonApiLinks(path));
  }

  public JsonApiWrapper getSpecimenVersions(String id, String path) throws NotFoundException {
    var versionsList = mongoRepository.getVersions(id, MONGODB_COLLECTION_NAME);
    var versionNode = createVersionNode(versionsList, mapper);
    return new JsonApiWrapper(new JsonApiData(id, "digitalSpecimenVersions", versionNode),
        new JsonApiLinks(path));
  }

  public JsonApiListResponseWrapper getAnnotations(String id, String path) {
    return annotationService.getAnnotationForTarget(id, path);
  }

  public JsonApiListResponseWrapper getDigitalMedia(String id, String path) {
    var dataNode = digitalMediaObjectService.getDigitalMediaForSpecimen(id);
    return new JsonApiListResponseWrapper(dataNode, new JsonApiLinksFull(path));
  }

  public DigitalSpecimenJsonLD getSpecimenByIdJsonLD(String id) {
    var digitalSpecimenWrapper = repository.getLatestSpecimenById(id);
    var digitalMediaObjects = digitalMediaObjectService.getDigitalMediaIdsForSpecimen(
            digitalSpecimenWrapper.digitalSpecimen().getOdsId()).stream().map(value -> "hdl:" + value)
        .toList();
    var primarySpecimenData = generatePrimaryData(digitalSpecimenWrapper);
    return new DigitalSpecimenJsonLD(
        "hdl:" + digitalSpecimenWrapper.digitalSpecimen().getOdsId(),
        digitalSpecimenWrapper.digitalSpecimen().getOdsType(),
        generateContext(primarySpecimenData),
        primarySpecimenData,
        digitalMediaObjects
    );
  }

  private JsonNode generatePrimaryData(DigitalSpecimenWrapper digitalSpecimenWrapper) {
    return mapper.convertValue(digitalSpecimenWrapper.digitalSpecimen(), ObjectNode.class);
  }

  private JsonNode generateContext(JsonNode data) {
    var prefixes = determinePrefixes(data);
    var node = mapper.createObjectNode();
    node.set("dwc:institutionId", generateIdNode());
    node.set("ods:sourceSystemId", generateIdNode());
    node.set("ods:hasSpecimenMedia", generateMediaNode());
    prefixes.forEach(prefix -> node.put(prefix, prefixMap.get(prefix)));
    return node;
  }

  private Set<String> determinePrefixes(JsonNode data) {
    var prefixes = new HashSet<String>();
    prefixes.add("hdl");
    data.fields().forEachRemaining(field -> extractPrefix(field, prefixes));
    return prefixes;
  }

  private void extractPrefix(Entry<String, JsonNode> field, Set<String> prefixes) {
    if (field.getValue().isValueNode()) {
      if (field.getKey().contains(":")) {
        var prefix = field.getKey().substring(0, field.getKey().indexOf(':'));
        prefixes.add(prefix);
      }
    } else if (field.getValue().isArray()) {
      field.getValue().elements().forEachRemaining(
          entry -> entry.fields().forEachRemaining(subField -> extractPrefix(subField, prefixes)));
    } else if (field.getValue().isObject()) {
      field.getValue().fields().forEachRemaining(subField -> extractPrefix(subField, prefixes));
    }
  }

  private JsonNode generateMediaNode() {
    var node = mapper.createObjectNode();
    node.put("@container", "@list");
    node.put("@type", "@id");
    return node;
  }

  private JsonNode generateIdNode() {
    var node = mapper.createObjectNode();
    node.put("@type", "@id");
    return node;
  }

  private DigitalSpecimenWrapper mapResultToSpecimen(JsonNode result)
      throws JsonProcessingException {
    var digitalSpecimenNode = result.get("digitalSpecimenWrapper");
    var ds = mapper.treeToValue(digitalSpecimenNode.get("ods:attributes"), DigitalSpecimen.class)
        .withOdsId(DOI_STRING + result.get("id").asText())
        .withOdsType(digitalSpecimenNode.get("ods:type").asText())
        .withOdsMidsLevel(result.get("midsLevel").asInt())
        .withOdsVersion(result.get("version").asInt())
        .withOdsCreated(result.get("created").asText());
    return new DigitalSpecimenWrapper(
        ds,
        digitalSpecimenNode.get("ods:originalAttributes")
    );
  }

  private JsonApiListResponseWrapper wrapListResponse(
      Pair<Long, List<DigitalSpecimenWrapper>> elasticSearchResults,
      int pageSize, int pageNumber, String path) {
    var digitalSpecimenList = elasticSearchResults.getRight();
    var dataNodePlusOne = digitalSpecimenList.stream()
        .map(specimen -> new JsonApiData(specimen.digitalSpecimen().getOdsId(),
            specimen.digitalSpecimen().getOdsType(), specimen, mapper))
        .toList();
    boolean hasNext = dataNodePlusOne.size() > pageSize;
    var linksNode = new JsonApiLinksFull(pageNumber, pageSize, hasNext, path);
    var dataNode = hasNext ? dataNodePlusOne.subList(0, pageSize) : dataNodePlusOne;
    var metaNode = new JsonApiMeta(elasticSearchResults.getLeft());
    return new JsonApiListResponseWrapper(dataNode, linksNode, metaNode);
  }

  private JsonApiListResponseWrapper wrapListResponseSearchResults(
      Pair<Long, List<DigitalSpecimenWrapper>> digitalSpecimenSearchResult,
      int pageNumber, int pageSize, MultiValueMap<String, String> params, String path) {
    var dataNodePlusOne = digitalSpecimenSearchResult.getRight().stream()
        .map(specimen -> new JsonApiData(specimen.digitalSpecimen().getOdsId(),
            specimen.digitalSpecimen().getOdsType(), specimen, mapper))
        .toList();
    boolean hasNext = dataNodePlusOne.size() > pageSize;
    var linksNode = new JsonApiLinksFull(params, pageNumber, pageSize, hasNext, path);
    var dataNode = hasNext ? dataNodePlusOne.subList(0, pageSize) : dataNodePlusOne;
    return new JsonApiListResponseWrapper(dataNode, linksNode,
        new JsonApiMeta(digitalSpecimenSearchResult.getLeft()));
  }

  public JsonApiListResponseWrapper search(MultiValueMap<String, String> params, String path)
      throws IOException, UnknownParameterException {
    var pageNumber = getIntParam("pageNumber", params, DEFAULT_PAGE_NUM);
    var pageSize = getIntParam("pageSize", params, DEFAULT_PAGE_SIZE);
    removePaginationParams(params);
    var specimenPlusOne = elasticRepository.search(mapParamsKeyword(params), pageNumber, pageSize);
    return wrapListResponseSearchResults(specimenPlusOne, pageNumber, pageSize, params, path);
  }

  private void removePaginationParams(MultiValueMap<String, String> params) {
    params.remove("pageSize");
    params.remove("pageNumber");
  }

  private int getIntParam(String paramName, MultiValueMap<String, String> params,
      String defaultValue) {
    var paramValue = params.getOrDefault(paramName, List.of(defaultValue));
    if (paramValue.size() > 1) {
      log.warn("Taking first value for param: {} values: {}", paramName, paramValue);
    }
    try {
      var intValue = Integer.parseInt(paramValue.get(0));
      if (intValue > 0) {
        return intValue;
      } else {
        log.warn("Provided value: {} is a negative value, falling back to default value", intValue);
        return Integer.parseInt(defaultValue);
      }
    } catch (NumberFormatException ex) {
      log.error("Param: {} cannot be parsed to a number, falling back to default", paramName, ex);
      return Integer.parseInt(defaultValue);
    }
  }

  private Map<String, List<String>> mapParamsKeyword(MultiValueMap<String, String> params)
      throws UnknownParameterException {
    var mappedParams = new HashMap<String, List<String>>();
    for (var entry : params.entrySet()) {
      var mappedParam = getMappedTerm(entry.getKey());
      if (mappedParam.isPresent()) {
        mappedParams.put(mappedParam.get(), entry.getValue());
      } else {
        throw new UnknownParameterException("Parameter: " + entry.getKey() + " is not recognised");
      }
    }
    return mappedParams;
  }

  public JsonApiWrapper aggregations(MultiValueMap<String, String> params, String path)
      throws IOException, UnknownParameterException {
    var aggregations = elasticRepository.getAggregations(mapParamsKeyword(params));
    var dataNode = new JsonApiData(String.valueOf(params.hashCode()), AGGREGATIONS_TYPE,
        mapper.valueToTree(aggregations));
    return new JsonApiWrapper(dataNode, new JsonApiLinks(path));
  }

  public JsonApiWrapper discipline(String path) throws IOException {
    var disciplineResult = elasticRepository.getAggregation(TOPIC_DISCIPLINE);
    var dataNode = new JsonApiData(String.valueOf(path.hashCode()), AGGREGATIONS_TYPE,
        mapper.valueToTree(disciplineResult.getRight()));
    return new JsonApiWrapper(dataNode,
        new JsonApiLinks(path), new JsonApiMeta(disciplineResult.getLeft()));
  }

  public JsonApiWrapper searchTermValue(String name, String value, String path)
      throws UnknownParameterException, IOException {
    var mappedTerm = getMappedTerm(name);
    if (mappedTerm.isPresent()) {
      var aggregations = elasticRepository.searchTermValue(name, mappedTerm.get(), value);
      var dataNode = new JsonApiData(String.valueOf((name + value).hashCode()), AGGREGATIONS_TYPE,
          mapper.valueToTree(aggregations));
      return new JsonApiWrapper(dataNode, new JsonApiLinks(path));
    } else {
      throw new UnknownParameterException("Parameter: " + name + " is not recognised");
    }
  }

  public JsonApiListResponseWrapper getMass(String id, String path) {
    var digitalSpecimen = repository.getLatestSpecimenById(id);
    var flattenAttributes = flattenAttributes(digitalSpecimen);
    return masService.getMassForObject(flattenAttributes, path);
  }

  private JsonNode flattenAttributes(DigitalSpecimenWrapper digitalSpecimen) {
    return mapper.convertValue(digitalSpecimen.digitalSpecimen(), ObjectNode.class);
  }

  public JsonApiListResponseWrapper scheduleMass(String id, List<String> masIds, String userId, String path)
      throws ForbiddenException {
    var orcid = userService.getOrcid(userId);
    var digitalSpecimen = repository.getLatestSpecimenById(id);
    var flattenAttributes = flattenAttributes(digitalSpecimen);
    return masService.scheduleMass(flattenAttributes, masIds, path, digitalSpecimen, id, orcid);
  }
}