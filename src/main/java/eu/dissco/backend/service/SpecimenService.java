package eu.dissco.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.dissco.backend.domain.AnnotationResponse;
import eu.dissco.backend.domain.DigitalMediaObject;
import eu.dissco.backend.domain.DigitalSpecimen;
import eu.dissco.backend.domain.DigitalSpecimenFull;
import eu.dissco.backend.domain.DigitalSpecimenJsonLD;
import eu.dissco.backend.repository.ElasticSearchRepository;
import eu.dissco.backend.repository.SpecimenRepository;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpecimenService {

  private final ObjectMapper mapper;
  private final SpecimenRepository repository;
  private final ElasticSearchRepository elasticRepository;
  private final DigitalMediaObjectService digitalMediaObjectService;
  private final AnnotationService annotationService;

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

  public DigitalSpecimen getSpecimenByVersion(String id, int version) {
    return repository.getSpecimenByVersion(id, version);
  }

  public List<Integer> getSpecimenVersions(String id) {
    return repository.getSpecimenVersions(id);
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

  public List<DigitalSpecimen> getLatestSpecimen() throws IOException {
    return elasticRepository.getLatestSpecimen();
  }

  public DigitalSpecimenJsonLD getSpecimenByIdJsonLD(String id) {
    var digitalSpecimen = getSpecimenById(id);
    var digitalMediaObjects = digitalMediaObjectService.getDigitalMediaIdsForSpecimen(
        digitalSpecimen.id()).stream().map(value -> "hdl:" + value).toList();
    return new DigitalSpecimenJsonLD(
        "hdl:" + digitalSpecimen.id(),
        digitalSpecimen.type(),
        generateContext(),
        generatePrimaryData(digitalSpecimen),
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

  private JsonNode generateContext() {
    var node = mapper.createObjectNode();
    node.put("ods", "http://github.com/DiSSCo/openDS/ods-ontology/terms/");
    node.put("hls", "https://hdl.handle.net/");
    node.put("doi", "https://doi.org/");
    node.put("dct", "http://purl.org/dc/terms/");
    node.put("dwc", "http://rs.tdwg.org/dwc/terms/");
    node.set("ods:organizationId", generateIdNode());
    node.set("ods:sourceSystemId", generateIdNode());
    node.set("ods:hasSpecimenMedia", generateMediaNode());
    return node;
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