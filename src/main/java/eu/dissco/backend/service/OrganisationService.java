package eu.dissco.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.dissco.backend.domain.OrganisationDocument;
import eu.dissco.backend.domain.OrganisationTuple;
import eu.dissco.backend.repository.CordraRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cnri.cordra.api.CordraException;
import net.cnri.cordra.api.CordraObject;
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

  public CordraObject createNewDocument(OrganisationDocument document)
      throws CordraException, JsonProcessingException {
    return repository.createOrganisationDocument(document);
  }
}
