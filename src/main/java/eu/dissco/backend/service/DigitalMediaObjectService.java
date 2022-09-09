package eu.dissco.backend.service;

import eu.dissco.backend.domain.MultiMediaObject;
import eu.dissco.backend.repository.DigitalMediaObjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DigitalMediaObjectService {

  private final DigitalMediaObjectRepository repository;

  public MultiMediaObject getMultiMediaById(String id) {
    return repository.getLatestMultiMediaById(id);
  }
}
