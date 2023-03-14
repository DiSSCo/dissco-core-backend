package eu.dissco.backend.service;

import eu.dissco.backend.domain.jsonapi.JsonApiLinksFull;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.repository.OrganisationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganisationService {

  private final OrganisationRepository repository;

  public JsonApiListResponseWrapper getOrganisations(String path) {
    return new JsonApiListResponseWrapper(repository.getOrganisations(),
    new JsonApiLinksFull(path));
  }

  public JsonApiListResponseWrapper getCountries(String path) {
    var dataNode = repository.getCountries();
    var linksNode = new JsonApiLinksFull(path);
    return new JsonApiListResponseWrapper(dataNode, linksNode);
  }
}
