package eu.dissco.backend.service;

import eu.dissco.backend.domain.DigitalMediaObject;
import eu.dissco.backend.domain.DigitalMediaObjectFull;
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
}
