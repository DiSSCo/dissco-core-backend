package eu.dissco.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.dissco.backend.domain.AnnotationResponse;
import eu.dissco.backend.domain.DigitalMediaObject;
import eu.dissco.backend.domain.DigitalSpecimen;
import eu.dissco.backend.domain.DigitalSpecimenFull;
import eu.dissco.backend.domain.DigitalSpecimenJsonLD;
import eu.dissco.backend.exceptions.NotFoundException;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class SpecimenService {

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

  public List<DigitalSpecimen> getSpecimen(int pageNumber, int pageSize) {
    return repository.getSpecimensLatest(pageNumber, pageSize);
  }

  public DigitalSpecimen getSpecimenById(String id) {
    return repository.getLatestSpecimenById(id);
  }

  public List<DigitalSpecimen> search(String query, int pageNumber, int pageSize)
      throws IOException {
    return elasticRepository.search(query, pageNumber, pageSize);
  }

  public List<AnnotationResponse> getAnnotations(String id) {
    return annotationService.getAnnotationForTarget(id);
  }

  public DigitalSpecimen getSpecimenByVersion(String id, int version)
      throws JsonProcessingException, NotFoundException {
    var result = mongoRepository.getByVersion(id, version, "digital_specimen_provenance");
    return mapResultToSpecimen(result);
  }

  private DigitalSpecimen mapResultToSpecimen(JsonNode result) {
    return new DigitalSpecimen(
        result.get("id").asText(),
        result.get("midslevel").asInt(),
        result.get("version").asInt(),
        Instant.parse(result.get("created").get("$date").asText()),
        result.get("type").asText(),
        result.get("physical_specimen_id").asText(),
        result.get("physical_specimen_type").asText(),
        result.get("specimen_name").asText(),
        result.get("organization_id").asText(),
        result.get("dataset").asText(),
        result.get("physical_specimen_collection").asText(),
        result.get("source_system_id").asText(),
        result.get("data"),
        result.get("original_data"),
        result.get("dwca_id").asText()
    );
  }

  public List<Integer> getSpecimenVersions(String id) throws NotFoundException {
    return mongoRepository.getVersions(id, "digital_specimen_provenance");
  }

  public DigitalSpecimenFull getSpecimenByIdFull(String id) {
    var digitalSpecimen = repository.getLatestSpecimenById(id);
    var digitalMedia = digitalMediaObjectService.getDigitalMediaObjectFull(id);
    var annotation = annotationService.getAnnotationForTarget(id);
    return new DigitalSpecimenFull(digitalSpecimen, digitalMedia, annotation);
  }

  public List<DigitalMediaObject> getDigitalMedia(String id) {
    return digitalMediaObjectService.getDigitalMediaForSpecimen(id);
  }

  public List<DigitalSpecimen> getLatestSpecimen(int pageNumber, int pageSize) throws IOException {
    return elasticRepository.getLatestSpecimen(pageNumber, pageSize);
  }

  public DigitalSpecimenJsonLD getSpecimenByIdJsonLD(String id) {
    var digitalSpecimen = getSpecimenById(id);
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
    primarySpecimenData.put("ods:organizationId", digitalSpecimen.organizationId());
    primarySpecimenData.put("ods:datasetId", digitalSpecimen.datasetId());
    primarySpecimenData.put("ods:physicalSpecimenCollection",
        digitalSpecimen.physicalSpecimenCollection());
    primarySpecimenData.setAll((ObjectNode) digitalSpecimen.data().deepCopy());
    return primarySpecimenData;
  }

  private JsonNode generateContext(JsonNode data) {
    var prefixes = determinePrefixes(data);
    var node = mapper.createObjectNode();
    node.set("ods:organizationId", generateIdNode());
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
}