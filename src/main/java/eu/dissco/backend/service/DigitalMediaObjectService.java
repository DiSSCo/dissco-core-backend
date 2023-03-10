package eu.dissco.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import eu.dissco.backend.domain.AnnotationResponse;
import eu.dissco.backend.domain.DigitalMediaObject;
import eu.dissco.backend.domain.DigitalMediaObjectFull;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.domain.jsonapi.JsonApiLinks;
import eu.dissco.backend.domain.jsonapi.JsonApiLinksFull;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.repository.DigitalMediaObjectRepository;
import eu.dissco.backend.repository.MongoRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DigitalMediaObjectService {

  private final DigitalMediaObjectRepository repository;
  private final AnnotationService annotationService;
  private final MongoRepository mongoRepository;

  public DigitalMediaObject getDigitalMediaById(String id) {
    return repository.getLatestDigitalMediaById(id);
  }

  public JsonApiWrapper getDigitalMediaByIdJsonResponse(String id, String path) {
    var dataNode = repository.getLatestDigitalMediaObjectByIdJsonResponse(id);
    var linksNode = new JsonApiLinks(path);
    return new JsonApiWrapper(dataNode, linksNode);
  }

  public List<AnnotationResponse> getAnnotationsOnDigitalMediaObject(String id) {
      return annotationService.getAnnotationForTarget(id);
  }

  public List<DigitalMediaObjectFull> getDigitalMediaObjectFull(String id) {
    var digitalMediaFull = new ArrayList<DigitalMediaObjectFull>();
    var digitalMedia = repository.getDigitalMediaForSpecimen(id);
    for (var digitalMediaObject : digitalMedia) {
      var annotation = annotationService.getAnnotationForTarget(digitalMediaObject.id());
      digitalMediaFull.add(new DigitalMediaObjectFull(digitalMediaObject, annotation));
    }
    return digitalMediaFull;
  }

  public List<Integer> getDigitalMediaVersions(String id) throws NotFoundException {
    return mongoRepository.getVersions(id, "digital_media_provenance");
  }

  public DigitalMediaObject getDigitalMediaVersionByVersion(String id, int version)
      throws JsonProcessingException, NotFoundException {
    var result = mongoRepository.getByVersion(id, version, "digital_media_provenance");
    return mapToDigitalMediaObject(result);
  }

  private DigitalMediaObject mapToDigitalMediaObject(JsonNode result) {
    var digitalMediaObject = result.get("digitalMediaObject");
    return new DigitalMediaObject(
        result.get("id").asText(),
        result.get("version").asInt(),
        Instant.ofEpochSecond(result.get("created").asInt()),
        digitalMediaObject.get("type").asText(),
        digitalMediaObject.get("digitalSpecimenId").asText(),
        digitalMediaObject.get("mediaUrl").asText(),
        digitalMediaObject.get("format").asText(),
        digitalMediaObject.get("sourceSystemId").asText(),
        digitalMediaObject.get("data"),
        digitalMediaObject.get("originalData")
    );
  }

  public List<DigitalMediaObject> getDigitalMediaForSpecimen(String id) {
    return repository.getDigitalMediaForSpecimen(id);
  }

  public List<DigitalMediaObject> getDigitalMediaObjects(int pageNumber, int pageSize) {
    return repository.getDigitalMediaObject(pageNumber, pageSize);
  }

  private JsonApiListResponseWrapper wrapResponse(List<JsonApiData> dataNodePlusOne, int pageNumber, int pageSize, String path){
    boolean hasNextPage = dataNodePlusOne.size() > pageSize;
    var dataNode = hasNextPage ? dataNodePlusOne.subList(0, pageSize) : dataNodePlusOne;
    var linksNode = new JsonApiLinksFull(pageNumber, pageSize, hasNextPage, path);
    return new JsonApiListResponseWrapper(dataNode, linksNode);
  }

  public JsonApiListResponseWrapper getDigitalMediaObjectsJsonResponse(int pageNumber, int pageSize,
      String path) {
    var dataNodePlusOne = repository.getDigitalMediaObjectJsonResponse(pageNumber, pageSize+1);
    return wrapResponse(dataNodePlusOne, pageNumber, pageSize, path);
  }

  public List<String> getDigitalMediaIdsForSpecimen(String id) {
    return repository.getDigitalMediaIdsForSpecimen(id);
  }
}
