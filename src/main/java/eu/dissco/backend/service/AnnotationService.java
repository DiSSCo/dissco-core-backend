package eu.dissco.backend.service;

import eu.dissco.backend.domain.AnnotationRequest;
import eu.dissco.backend.domain.AnnotationResponse;
import eu.dissco.backend.repository.AnnotationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnnotationService {

  private final AnnotationRepository repository;

  public void persistAnnotation(AnnotationRequest annotation, String userId) {
    repository.saveAnnotation(annotation, userId);
  }

  public List<AnnotationResponse> getAnnotationsForUser(String userId) {
    return repository.getAnnotationsForUser(userId);
  }

  public void updateAnnotation(AnnotationRequest annotation, String userId) {
    repository.updateAnnotation(annotation, userId);
  }

  public AnnotationResponse getAnnotation(String id) {
    return repository.getAnnotation(id);
  }

  public void deleteAnnotation(String id) {
    repository.deleteAnnotation(id);
  }
}
