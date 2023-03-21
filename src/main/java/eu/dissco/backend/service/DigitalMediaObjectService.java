package eu.dissco.backend.service;

import static eu.dissco.backend.service.ServiceUtils.createVersionNode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.domain.DigitalMediaObjectFull;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.domain.jsonapi.JsonApiLinks;
import eu.dissco.backend.domain.jsonapi.JsonApiLinksFull;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.repository.DigitalMediaObjectRepository;
import eu.dissco.backend.repository.MongoRepository;
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
  private final ObjectMapper mapper;

  // Controller Functions
  public JsonApiListResponseWrapper getDigitalMediaObjects(int pageNumber, int pageSize,
      String path) {
    var mediaPlusOne = repository.getDigitalMediaObjects(pageNumber, pageSize+1);
    List<JsonApiData> dataNodePlusOne = new ArrayList<>();
    mediaPlusOne.forEach(media -> dataNodePlusOne.add(new JsonApiData(media.id(), media.type(), mapper.valueToTree(media))));
    return wrapResponse(dataNodePlusOne, pageNumber, pageSize, path);
  }

  public JsonApiWrapper getDigitalMediaById(String id, String path) {
    var mediaObject = repository.getLatestDigitalMediaObjectById(id);
    var dataNode = new JsonApiData(mediaObject.id(), mediaObject.type(), mediaObject, mapper);
    var linksNode = new JsonApiLinks(path);
    return new JsonApiWrapper(dataNode, linksNode);
  }

  public JsonApiListResponseWrapper getAnnotationsOnDigitalMedia(String mediaId, String path) {
    return annotationService.getAnnotationForTarget(mediaId, path);
  }

  public JsonApiWrapper getDigitalMediaVersions(String id, String path) throws NotFoundException {
    var versions = mongoRepository.getVersions(id, "digital_media_provenance");
    var versionNode = createVersionNode(versions, mapper);
    var dataNode = new JsonApiData(id, "digitalMediaVersions", versionNode);
    return new JsonApiWrapper(dataNode, new JsonApiLinks(path));
  }

  public JsonApiWrapper getDigitalMediaObjectByVersion(String id, int version, String path)
      throws JsonProcessingException, NotFoundException {
    var dataNode = mongoRepository.getByVersion(id, version, "digital_media_provenance");
    String type = dataNode.get("digitalMediaObject").get("type").asText();
    return new JsonApiWrapper(new JsonApiData(id, type, dataNode),
        new JsonApiLinks(path));
  }

  // Used By Other Services
  public List<DigitalMediaObjectFull> getDigitalMediaObjectFull(String id) {
    var digitalMediaFull = new ArrayList<DigitalMediaObjectFull>();
    var digitalMedia = repository.getDigitalMediaForSpecimen(id);
    for (var digitalMediaObject : digitalMedia) {
      var annotation = annotationService.getAnnotationForTargetObject(digitalMediaObject.id());
      digitalMediaFull.add(new DigitalMediaObjectFull(digitalMediaObject, annotation));
    }
    return digitalMediaFull;
  }

  public List<String> getDigitalMediaIdsForSpecimen(String id) {
    return repository.getDigitalMediaIdsForSpecimen(id);
  }

  public List<JsonApiData> getDigitalMediaForSpecimen(String id) {
    var mediaList = repository.getDigitalMediaForSpecimen(id);
    List<JsonApiData> dataNode = new ArrayList<>();
    mediaList.forEach(media -> dataNode.add(new JsonApiData(media.id(), media.type(), mapper.valueToTree(media))));
    return dataNode;
  }

  // Response Wrapper
  private JsonApiListResponseWrapper wrapResponse(List<JsonApiData> dataNodePlusOne, int pageNumber, int pageSize, String path){
    boolean hasNextPage = dataNodePlusOne.size() > pageSize;
    var dataNode = hasNextPage ? dataNodePlusOne.subList(0, pageSize) : dataNodePlusOne;
    var linksNode = new JsonApiLinksFull(pageNumber, pageSize, hasNextPage, path);
    return new JsonApiListResponseWrapper(dataNode, linksNode);
  }
}
