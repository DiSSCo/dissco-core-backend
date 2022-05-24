package eu.dissco.backend.service;

import eu.dissco.backend.domain.OrganisationTuple;
import eu.dissco.backend.repository.CordraRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cnri.cordra.api.CordraException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganisationService {

  private final CordraRepository repository;


  public List<String> getNames() throws CordraException {
    return repository.getOrganisationNames();
  }

  public List<OrganisationTuple> getTuples() throws CordraException {
    return repository.getOrganisationTuple();
  }

}
