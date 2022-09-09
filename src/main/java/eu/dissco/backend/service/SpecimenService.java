package eu.dissco.backend.service;

import eu.dissco.backend.domain.AnnotationResponse;
import eu.dissco.backend.domain.DigitalSpecimen;
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
    return annotationRepository.getAnnotationsForSpecimen(id);
  }

  public DigitalSpecimen getSpecimenByVersion(String id, int version) {
    return repository.getSpecimenByVersion(id, version);
  }

  public List<Integer> getSpecimenVersions(String id) {
    return repository.getSpecimenVersions(id);
  }
}
