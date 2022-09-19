package eu.dissco.backend.service;

import eu.dissco.backend.domain.MappingRecord;
import eu.dissco.backend.repository.MappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MappingService {

  private final MappingRepository repository;

  public MappingRecord getMappingById(String id) {
      return repository.getMappingById(id);
  }
}
