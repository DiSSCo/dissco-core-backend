package eu.dissco.backend.service;

import eu.dissco.backend.domain.AnnotationResponse;
import eu.dissco.backend.domain.DigitalMediaObject;
import eu.dissco.backend.domain.DigitalSpecimen;
import eu.dissco.backend.domain.DigitalSpecimenFull;
import eu.dissco.backend.repository.AnnotationRepository;
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

  private final SpecimenRepository repository;
  private final AnnotationRepository annotationRepository;
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
    return annotationRepository.getForTarget(id);
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
}
