package eu.dissco.backend.service;

import eu.dissco.backend.domain.OrganisationDocument;
import eu.dissco.backend.repository.OrganisationDocumentRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganisationDocumentService {

  private final OrganisationDocumentRepository repository;

  public OrganisationDocument createNewDocument(OrganisationDocument document) {
    repository.saveNewDocument(document);
    return document;
  }

  public OrganisationDocument getDocument(String id) {
    return repository.getDocument(id);
  }

  public List<OrganisationDocument> getDocumentsForOrganisation(String ror) {
    return repository.getDocumentsForOrganisation(ror);
  }
}
