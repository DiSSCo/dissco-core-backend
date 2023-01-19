package eu.dissco.backend.service;

import eu.dissco.backend.domain.DigitalMediaObject;
import eu.dissco.backend.domain.DigitalMediaObjectFull;
import eu.dissco.backend.domain.JsonApiData;
import eu.dissco.backend.domain.JsonApiLinksFull;
import eu.dissco.backend.domain.JsonApiMeta;
import eu.dissco.backend.domain.JsonApiMetaWrapper;
import eu.dissco.backend.repository.DigitalMediaObjectRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DigitalMediaObjectService {

  private final DigitalMediaObjectRepository repository;
  private final AnnotationService annotationService;

  public DigitalMediaObject getDigitalMediaById(String id) {
    return repository.getLatestDigitalMediaById(id);
  }

  public List<JsonApiData> getDigitalMediaByIdJsonResponse(String id){
    return repository.getLatestDigitalMediaObjectByIdJsonResponse(id);
  }

  public List<DigitalMediaObjectFull> getDigitalMediaObjectFull(String id) {
    var digitalMediaFull = new ArrayList<DigitalMediaObjectFull>();
    var digitalMedia = repository.getForDigitalSpecimen(id);
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

  public List<DigitalMediaObject> getDigitalMediaForSpecimen(String id) {
    return repository.getDigitalMediaForSpecimen(id);
  }

  public List<DigitalMediaObject> getDigitalMediaObjects(int pageNumber, int pageSize) {
    return repository.getDigitalMediaObject(pageNumber, pageSize);
  }

  public JsonApiMetaWrapper getDigitalMediaObjectsJsonResponse(int pageNumber, int pageSize, String path){
    var dataNode = repository.getDigitalMediaObjectJsonResponse(pageNumber, pageSize);
    int totalPageCount = repository.getMediaObjectCount(pageSize);
    var linksNode = buildLinksNode(path, pageNumber, pageSize, totalPageCount);
    var metaNode = new JsonApiMeta(totalPageCount);

    return new JsonApiMetaWrapper(dataNode, linksNode, metaNode);
  }

  private JsonApiLinksFull buildLinksNode(String path, int pageNumber, int pageSize,
      int totalPageCount) {
    String pn = "?pageNumber=";
    String ps = "&pageSize=";
    String self = path + pn + pageNumber + ps + pageSize;
    String first = path + pn + "0" + ps + pageSize;
    String last = path + pn + totalPageCount + ps + pageSize;
    String prev = (pageNumber == 0) ? null
        : path + pn + (pageNumber - 1) + ps + pageSize;
    String next = (pageNumber >= totalPageCount) ? null
        : path + pn + (pageNumber + 1) + ps + pageSize;
    return new JsonApiLinksFull(self, first, last, prev, next);
  }

  public List<String> getDigitalMediaIdsForSpecimen(String id) {
    return repository.getDigitalMediaIdsForSpecimen(id);
  }
}
