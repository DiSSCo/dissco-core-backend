package eu.dissco.backend.service;

import eu.dissco.backend.domain.SourceSystemRecord;
import eu.dissco.backend.repository.SourceSystemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SourceSystemService {

  private final SourceSystemRepository repository;

  public SourceSystemRecord getSourceSystemById(String id) {
    return repository.getSourceSystemById(id);
  }
}
