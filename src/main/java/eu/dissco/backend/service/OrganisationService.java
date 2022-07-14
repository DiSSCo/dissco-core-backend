package eu.dissco.backend.service;

import eu.dissco.backend.domain.OrganisationTuple;
import eu.dissco.backend.repository.OrganisationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganisationService {

  private final OrganisationRepository repository;

  public List<String> getNames() {
    return repository.getOrganisationNames();
  }

  public List<OrganisationTuple> getTuples() {
    return repository.getOrganisationTuple();
  }

}
