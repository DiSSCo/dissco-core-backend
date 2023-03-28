package eu.dissco.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.domain.jsonapi.JsonApiLinksFull;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.repository.OrganisationRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganisationService {
  private final OrganisationRepository repository;
  private final ObjectMapper mapper;

  public List<String> getOrganisationNames(){
    return repository.getOrganisationNames();
  }

  public JsonApiListResponseWrapper getOrganisations(String path) {
    var organisations = repository.getOrganisations();
    List<JsonApiData> dataNode = new ArrayList<>();
    organisations.forEach(org -> dataNode.add(new JsonApiData(org.ror(), "organisation", org, mapper)));
    return new JsonApiListResponseWrapper(dataNode, new JsonApiLinksFull(path));
  }

  public JsonApiListResponseWrapper getCountries(String path) {
    var countries = repository.getCountries();
    List<JsonApiData> dataNode = new ArrayList<>();
    countries.forEach(country -> dataNode.add(new JsonApiData(country.countryCode(), "country", country, mapper)));
    var linksNode = new JsonApiLinksFull(path);
    return new JsonApiListResponseWrapper(dataNode, linksNode);
  }
}
