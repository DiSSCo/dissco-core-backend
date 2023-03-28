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
import eu.dissco.backend.exceptions.ConflictException;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.exceptions.UnprocessableEntityException;
import eu.dissco.backend.repository.ElasticSearchRepository;
import eu.dissco.backend.repository.MongoRepository;
import eu.dissco.backend.repository.SpecimenRepository;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException.UnprocessableEntity;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpecimenService {

  private static final String ORGANISATION_ID = "ods:organizationId";
  private final Map<String, String> prefixMap = Map.of(
      "dct", "http://purl.org/dc/terms/",
      "dwc", "http://rs.tdwg.org/dwc/terms/",
      "dwca", "http://rs.tdwg.org/dwc/text/",
      "abcd", "https://abcd.tdwg.org/terms/",
      "abcd-efg", "https://terms.tdwg.org/wiki/ABCD_EFG/",
      "ods", "http://github.com/DiSSCo/openDS/ods-ontology/terms/",
      "hdl", "https://hdl.handle.net/",
      "dcterms", "http://purl.org/dc/terms/");
  private final ObjectMapper mapper;
  private final SpecimenRepository repository;
  private final ElasticSearchRepository elasticRepository;
  private final DigitalMediaObjectService digitalMediaObjectService;
  private final AnnotationService annotationService;
  private final MongoRepository mongoRepository;

  public JsonApiListResponseWrapper getSpecimen(int pageNumber, int pageSize, String path) {
    var digitalSpecimenList = repository.getSpecimensLatest(pageNumber, pageSize + 1);
    return wrapListResponse(digitalSpecimenList, pageSize, pageNumber, path);
  }

  public JsonApiListResponseWrapper getLatestSpecimen(int pageNumber, int pageSize, String path) throws IOException {
    var digitalSpecimenList = elasticRepository.getLatestSpecimen(pageNumber, pageSize+1);
    return wrapListResponse(digitalSpecimenList, pageSize, pageNumber, path);
  }

  public JsonApiWrapper getSpecimenById(String id, String path) {
    var digitalSpecimen = repository.getLatestSpecimenById(id);
    var dataNode = new JsonApiData(digitalSpecimen.id(), digitalSpecimen.type(), digitalSpecimen, mapper);
    return new JsonApiWrapper(dataNode, new JsonApiLinks(path));
  }

  public JsonApiWrapper getSpecimenByIdFull(String id, String path) {
    var digitalSpecimen = repository.getLatestSpecimenById(id);
    var digitalMedia = digitalMediaObjectService.getDigitalMediaObjectFull(id);
    var annotation = annotationService.getAnnotationForTargetObject(id);
    var attributeNode = mapper.valueToTree(new DigitalSpecimenFull(digitalSpecimen, digitalMedia, annotation));
    return new JsonApiWrapper(new JsonApiData(id, digitalSpecimen.type(), attributeNode), new JsonApiLinks(path));
  }

  public JsonApiWrapper getSpecimenByVersion(String id, int version, String path)
      throws JsonProcessingException, NotFoundException {
    var specimenNode = mongoRepository.getByVersion(id, version, "digital_specimen_provenance");
    JsonApiData dataNode;
    try {
      var specimen = mapResultToSpecimen(specimenNode);
      dataNode = new JsonApiData(specimen.id(), specimen.type(), specimen, mapper);
    } catch (UnprocessableEntityException e){
      dataNode = new JsonApiData(id, "digitalSpecimen", specimenNode);
      log.warn("Unable to map digital specimen {} verision {} to DigitalSpecimen object. Returning raw json", id, version);
    }
    return new JsonApiWrapper(dataNode, new JsonApiLinks(path));
  }

  public JsonApiWrapper getSpecimenVersions(String id, String path) throws NotFoundException {
    var versionsList = mongoRepository.getVersions(id, "digital_specimen_provenance");
    var versionNode = createVersionNode(versionsList, mapper);
    return new JsonApiWrapper(new JsonApiData(id, "digitalSpecimenVersions", versionNode), new JsonApiLinks(path));
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

  public JsonApiListResponseWrapper search(String query, int pageNumber, int pageSize, String path)
      throws IOException {
    var specimensPlusOne = elasticRepository.search(query, pageNumber, pageSize+1);
    return wrapListResponse(specimensPlusOne, pageSize, pageNumber, path);
  }

  private JsonNode generatePrimaryData(DigitalSpecimen digitalSpecimen) {
    var primarySpecimenData = mapper.createObjectNode();
    primarySpecimenData.put("ods:midsLevel", digitalSpecimen.midsLevel());
    primarySpecimenData.put("ods:version", digitalSpecimen.version());
    primarySpecimenData.put("ods:physicalSpecimenId", digitalSpecimen.physicalSpecimenId());
    primarySpecimenData.put("ods:physicalSpecimenIdType", digitalSpecimen.physicalSpecimenIdType());
    primarySpecimenData.put("ods:specimenName", digitalSpecimen.specimenName());
    primarySpecimenData.put(ORGANISATION_ID, digitalSpecimen.organizationId());
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

  private DigitalSpecimen mapResultToSpecimen(JsonNode result) throws UnprocessableEntityException {
    var digitalSpecimen = result.get("digitalSpecimen");
    var attributes = digitalSpecimen.get("ods:attributes");
    DigitalSpecimen ds;

    try {
      ds =  new DigitalSpecimen(
          result.get("id").asText(),
          result.get("midsLevel").asInt(),
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
    } catch (NullPointerException npe){
      throw new UnprocessableEntityException();
    }
    return ds;
  }

  private JsonApiListResponseWrapper wrapListResponse(List<DigitalSpecimen> digitalSpecimenList, int pageSize, int pageNumber, String path){
    List<JsonApiData> dataNodePlusOne = new ArrayList<>();
    digitalSpecimenList.forEach(specimen -> dataNodePlusOne.add(new JsonApiData(specimen.id(), specimen.type(), specimen, mapper)));
    boolean hasNext = dataNodePlusOne.size() > pageSize;
    var linksNode = new JsonApiLinksFull(pageNumber, pageSize, hasNext, path);
    var dataNode = hasNext ? dataNodePlusOne.subList(0, pageSize) : dataNodePlusOne;
    return new JsonApiListResponseWrapper(dataNode, linksNode);
  }

}