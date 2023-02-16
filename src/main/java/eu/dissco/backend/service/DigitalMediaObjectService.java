package eu.dissco.backend.service;

import eu.dissco.backend.domain.DigitalMediaObject;
import eu.dissco.backend.domain.DigitalMediaObjectFull;
import eu.dissco.backend.domain.JsonApiLinks;
import eu.dissco.backend.domain.JsonApiLinksFull;
import eu.dissco.backend.domain.JsonApiMetaWrapper;
import eu.dissco.backend.domain.JsonApiWrapper;
import eu.dissco.backend.repository.DigitalMediaObjectRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DigitalMediaObjectService {

  private final DigitalMediaObjectRepository repository;
  private final AnnotationService annotationService;

  public DigitalMediaObject getDigitalMediaById(String id) {
    return repository.getLatestDigitalMediaById(id);
  }

  public JsonApiWrapper getDigitalMediaByIdJsonResponse(String id, String path) {
    var dataNode = repository.getLatestDigitalMediaObjectByIdJsonResponse(id);
    var linksNode = new JsonApiLinks(path);
    return new JsonApiWrapper(dataNode, linksNode);
  }

  public JsonApiMetaWrapper getAnnotationsOnDigitalMediaObject(String id, String path,
      int pageNumber, int pageSize) {
    String mediaId = "https://hdl.handle.net/" + id;
    var annotations = repository.getAnnotationsOnDigitalMediaObject(mediaId);
    var linksNode = buildLinksNode(path, pageNumber, pageSize, true);
    return new JsonApiMetaWrapper(annotations, linksNode);
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

  public List<Integer> getDigitalMediaVersions(String id) {
    return repository.getDigitalMediaVersions(id);
  }

  public DigitalMediaObject getDigitalMediaVersion(String id, int version) {
    return repository.getDigitalMediaByVersion(id, version);
  }

  public JsonApiWrapper getDigitalMediaVersionJsonResponse(String id, int version, String path) {
    var dataNode = repository.getDigitalMediaByVersionJsonResponse(id, version);
    var linksNode = new JsonApiLinks(path);
    return new JsonApiWrapper(dataNode, linksNode);
  }

  public List<DigitalMediaObject> getDigitalMediaForSpecimen(String id) {
    return repository.getDigitalMediaForSpecimen(id);
  }

  public List<DigitalMediaObject> getDigitalMediaObjects(int pageNumber, int pageSize) {
    return repository.getDigitalMediaObject(pageNumber, pageSize);
  }

  public JsonApiMetaWrapper getDigitalMediaObjectsJsonResponse(int pageNumber, int pageSize,
      String path) {
    var dataNode = repository.getDigitalMediaObjectJsonResponse(pageNumber, pageSize);
    var linksNode = buildLinksNode(path, pageNumber, pageSize, true);
    return new JsonApiMetaWrapper(dataNode, linksNode);
  }

  private JsonApiLinksFull buildLinksNode(String path, int pageNumber, int pageSize,
      boolean hasNextPage) {
    String pn = "?pageNumber=";
    String ps = "&pageSize=";
    String self = path + pn + pageNumber + ps + pageSize;
    String first = path + pn + "1" + ps + pageSize;
    String prev = (pageNumber == 1) ? null : path + pn + (pageNumber - 1) + ps + pageSize;
    String next =
        (hasNextPage) ? null : path + pn + (pageNumber + 1) + ps + pageSize;
    return new JsonApiLinksFull(self, first, prev, next);
  }

  public List<String> getDigitalMediaIdsForSpecimen(String id) {
    return repository.getDigitalMediaIdsForSpecimen(id);
  }
}
