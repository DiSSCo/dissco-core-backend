package eu.dissco.backend.service;

import static eu.dissco.backend.service.ServiceUtils.createVersionNode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.dissco.backend.domain.DigitalSpecimen;
import eu.dissco.backend.domain.DigitalSpecimenFull;
import eu.dissco.backend.domain.DigitalSpecimenJsonLD;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.domain.jsonapi.JsonApiLinks;
import eu.dissco.backend.domain.jsonapi.JsonApiLinksFull;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.repository.ElasticSearchRepository;
import eu.dissco.backend.repository.MongoRepository;
import eu.dissco.backend.repository.SpecimenRepository;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpecimenService {

  private static final String ORGANISATION_ID = "ods:organisationId";
  private static final String MIDS_LEVEL = "midsLevel";
  private final Map<String, String> prefixMap = Map.of(
      "dct", "http://purl.org/dc/terms/",
      "dwc", "http://rs.tdwg.org/dwc/terms/",
      "dwca", "http://rs.tdwg.org/dwc/text/",
      "abcd", "https://abcd.tdwg.org/terms/",
      "abcd-efg", "https://terms.tdwg.org/wiki/ABCD_EFG/",
      "ods", "http://github.com/DiSSCo/openDS/ods-ontology/terms/",
      "hdl", "https://hdl.handle.net/",
      "dcterms", "http://purl.org/dc/terms/");
  private final Map<String, String> paramMapping = createParamMapping();
  private final ObjectMapper mapper;
  private final SpecimenRepository repository;
  private final ElasticSearchRepository elasticRepository;
  private final DigitalMediaObjectService digitalMediaObjectService;
  private final AnnotationService annotationService;
  private final MongoRepository mongoRepository;

  private static Map<String, String> createParamMapping() {
    var map = new HashMap<String, String>();
    map.put("country", "digitalSpecimen.ods:attributes.dwc:country.keyword");
    map.put("countryCode", "digitalSpecimen.ods:attributes.dwc:countryCode.keyword");
    map.put("physicalSpecimenId", "digitalSpecimen.ods:physicalSpecimenId.keyword");
    map.put(MIDS_LEVEL, MIDS_LEVEL);
    map.put("typeStatus", "digitalSpecimen.ods:attributes.dwc:typeStatus.keyword");
    map.put("license", "digitalSpecimen.ods:attributes.dcterms:license.keyword");
    map.put("hasMedia", "digitalSpecimen.ods:attributes.ods:hasMedia.keyword");
    map.put("organisationId", "digitalSpecimen.ods:attributes.ods:organisationId.keyword");
    map.put("organisationName", "digitalSpecimen.ods:attributes.ods:organisationName.keyword");
    map.put("sourceSystemId", "digitalSpecimen.ods:attributes.ods:sourceSystemId.keyword");
    map.put("type", "digitalSpecimen.ods:type.keyword");
    map.put("specimenName", "digitalSpecimen.ods:attributes.ods:specimenName");
    map.put("q", "q");
    return map;
  }

  public JsonApiListResponseWrapper getSpecimen(int pageNumber, int pageSize, String path) {
    var digitalSpecimenList = repository.getSpecimensLatest(pageNumber, pageSize + 1);
    return wrapListResponse(digitalSpecimenList, pageSize, pageNumber, path);
  }

  public JsonApiListResponseWrapper getLatestSpecimen(int pageNumber, int pageSize, String path)
      throws IOException {
    var digitalSpecimenList = elasticRepository.getLatestSpecimen(pageNumber, pageSize + 1);
    return wrapListResponse(digitalSpecimenList, pageSize, pageNumber, path);
  }

  public JsonApiWrapper getSpecimenById(String id, String path) {
    var digitalSpecimen = repository.getLatestSpecimenById(id);
    var dataNode = new JsonApiData(digitalSpecimen.id(), digitalSpecimen.type(), digitalSpecimen,
        mapper);
    return new JsonApiWrapper(dataNode, new JsonApiLinks(path));
  }

  public JsonApiWrapper getSpecimenByIdFull(String id, String path) {
    var digitalSpecimen = repository.getLatestSpecimenById(id);
    var digitalMedia = digitalMediaObjectService.getDigitalMediaObjectFull(id);
    var annotation = annotationService.getAnnotationForTargetObject(id);
    var attributeNode = mapper.valueToTree(
        new DigitalSpecimenFull(digitalSpecimen, digitalMedia, annotation));
    return new JsonApiWrapper(new JsonApiData(id, digitalSpecimen.type(), attributeNode),
        new JsonApiLinks(path));
  }

  public JsonApiWrapper getSpecimenByVersion(String id, int version, String path)
      throws JsonProcessingException, NotFoundException {
    var specimenNode = mongoRepository.getByVersion(id, version, "digital_specimen_provenance");
    JsonApiData dataNode;
    var specimen = mapResultToSpecimen(specimenNode);
    dataNode = new JsonApiData(specimen.id(), specimen.type(), specimen, mapper);
    return new JsonApiWrapper(dataNode, new JsonApiLinks(path));
  }

  public JsonApiWrapper getSpecimenVersions(String id, String path) throws NotFoundException {
    var versionsList = mongoRepository.getVersions(id, "digital_specimen_provenance");
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
    var digitalSpecimen = repository.getLatestSpecimenById(id);
    var digitalMediaObjects = digitalMediaObjectService.getDigitalMediaIdsForSpecimen(
        digitalSpecimen.id()).stream().map(value -> "hdl:" + value).toList();
    var primarySpecimenData = generatePrimaryData(digitalSpecimen);
    return new DigitalSpecimenJsonLD(
        "hdl:" + digitalSpecimen.id(),
        digitalSpecimen.type(),
        generateContext(primarySpecimenData),
        primarySpecimenData,
        "hdl:" + digitalSpecimen.sourceSystemId(),
        digitalMediaObjects
    );
  }

  private JsonNode generatePrimaryData(DigitalSpecimen digitalSpecimen) {
    var primarySpecimenData = mapper.createObjectNode();
    primarySpecimenData.put("ods:midsLevel", digitalSpecimen.midsLevel());
    primarySpecimenData.put("ods:version", digitalSpecimen.version());
    primarySpecimenData.put("ods:physicalSpecimenId", digitalSpecimen.physicalSpecimenId());
    primarySpecimenData.put("ods:physicalSpecimenIdType", digitalSpecimen.physicalSpecimenIdType());
    primarySpecimenData.put("ods:specimenName", digitalSpecimen.specimenName());
    primarySpecimenData.put(ORGANISATION_ID, digitalSpecimen.organisationId());
    primarySpecimenData.put("ods:datasetId", digitalSpecimen.datasetId());
    primarySpecimenData.put("ods:physicalSpecimenCollection",
        digitalSpecimen.physicalSpecimenCollection());
    primarySpecimenData.setAll((ObjectNode) digitalSpecimen.data().deepCopy());
    return primarySpecimenData;
  }

  private JsonNode generateContext(JsonNode data) {
    var prefixes = determinePrefixes(data);
    var node = mapper.createObjectNode();
    node.set(ORGANISATION_ID, generateIdNode());
    node.set("ods:sourceSystemId", generateIdNode());
    node.set("ods:hasSpecimenMedia", generateMediaNode());
    prefixes.forEach(prefix -> node.put(prefix, prefixMap.get(prefix)));
    return node;
  }

  private List<String> determinePrefixes(JsonNode data) {
    var prefixes = new ArrayList<String>();
    prefixes.add("hdl");
    data.fields().forEachRemaining(field -> {
      var prefix = field.getKey().substring(0, field.getKey().indexOf(':'));
      prefixes.add(prefix);
    });
    return prefixes;
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

  private DigitalSpecimen mapResultToSpecimen(JsonNode result) {
    var digitalSpecimen = result.get("digitalSpecimen");
    var attributes = digitalSpecimen.get("ods:attributes");
    return new DigitalSpecimen(
        result.get("id").asText(),
        result.get(MIDS_LEVEL).asInt(),
        result.get("version").asInt(),
        Instant.ofEpochSecond(result.get("created").asInt()),
        digitalSpecimen.get("ods:type").asText(),
        digitalSpecimen.get("ods:physicalSpecimenId").asText(),
        attributes.get("ods:physicalSpecimenIdType").asText(),
        attributes.get("ods:specimenName").asText(),
        attributes.get(ORGANISATION_ID).asText(),
        attributes.get("ods:datasetId").asText(),
        attributes.get("ods:physicalSpecimenCollection").asText(),
        attributes.get("ods:sourceSystemId").asText(),
        attributes,
        digitalSpecimen.get("ods:originalAttributes"),
        digitalSpecimen.get("ods:attributes").get("dwca:id").asText()
    );
  }

  private JsonApiListResponseWrapper wrapListResponse(List<DigitalSpecimen> digitalSpecimenList,
      int pageSize, int pageNumber, String path) {
    var dataNodePlusOne = digitalSpecimenList.stream().map(specimen -> new JsonApiData(specimen.id(), specimen.type(), specimen, mapper)).toList();
    boolean hasNext = dataNodePlusOne.size() > pageSize;
    var linksNode = new JsonApiLinksFull(pageNumber, pageSize, hasNext, path);
    var dataNode = hasNext ? dataNodePlusOne.subList(0, pageSize) : dataNodePlusOne;
    return new JsonApiListResponseWrapper(dataNode, linksNode);
  }
  private JsonApiListResponseWrapper wrapListResponse(List<DigitalSpecimen> digitalSpecimenList,
      int pageNumber, int pageSize, MultiValueMap<String, String> params, String path) {
    var dataNodePlusOne = digitalSpecimenList.stream().map(specimen -> new JsonApiData(specimen.id(), specimen.type(), specimen, mapper)).toList();
    boolean hasNext = dataNodePlusOne.size() > pageSize;
    var linksNode = new JsonApiLinksFull(params, pageNumber, pageSize, hasNext, path);
    var dataNode = hasNext ? dataNodePlusOne.subList(0, pageSize) : dataNodePlusOne;
    return new JsonApiListResponseWrapper(dataNode, linksNode);
  }

  public JsonApiListResponseWrapper search(MultiValueMap<String, String> params, String path)
      throws IOException {
    var pageNumber = getIntParam("pageNumber", params, "1");
    var pageSize = getIntParam("pageSize", params, "10");
    var specimenPlusOne = elasticRepository.search(mapParams(params), pageNumber, pageSize + 1);
    return wrapListResponse(specimenPlusOne, pageNumber, pageSize, params, path);
  }

  private static int getIntParam(String paramName, MultiValueMap<String, String> params, String defaultValue) {
    var paramValue = params.getOrDefault(paramName, List.of(defaultValue));
    if (paramValue.size() > 1){
      log.warn("Taking first value for param: {} values: {}", paramName, paramValue);
    }
    try{
      return Integer.parseInt(paramValue.get(0));
    } catch(NumberFormatException ex){
     log.error("Param: {} cannot be parsed to a number, falling back to default", paramName, ex);
     return Integer.parseInt(defaultValue);
    }
  }

  private Map<String, List<String>> mapParams(MultiValueMap<String, String> params) {
    var mappedParams = new HashMap<String, List<String>>();
    for (var entry : params.entrySet()) {
      var mappedParam = paramMapping.get(entry.getKey());
      if (mappedParam != null) {
        mappedParams.put(mappedParam, entry.getValue());
      } else {
        log.warn("Did not find mapping for key: {}", entry.getKey());
      }
    }
    return mappedParams;
  }
}