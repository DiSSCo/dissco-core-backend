package eu.dissco.backend.utils;

import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.SANDBOX_URI;

import eu.dissco.backend.domain.Country;
import eu.dissco.backend.domain.OrganisationTuple;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.domain.jsonapi.JsonApiLinksFull;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import java.util.List;

public class OrganisationUtils {

  public static final String ORGANISATION_URI = "/api/v1/organisation";
  public static final String ORGANISATION_PATH = SANDBOX_URI + ORGANISATION_URI;
  public final static OrganisationTuple ORGANISATION = new OrganisationTuple("Naturalis", "0x123");
  public final static Country COUNTRY = new Country("Netherlands", "NL");

  private OrganisationUtils() {
  }

  public static JsonApiData givenOrganisationData(OrganisationTuple organisation) {
    return new JsonApiData(organisation.ror(),"organisation", organisation, MAPPER);
  }

  public static JsonApiData givenCountryData(Country country) {
    return new JsonApiData(country.countryCode(),"country", country, MAPPER);
  }

  public static JsonApiData givenCountryData() {
    return givenCountryData(COUNTRY);
  }

  public static JsonApiData givenOrganisationData() {
    return givenOrganisationData(ORGANISATION);
  }

  public static JsonApiListResponseWrapper givenOrganisationJsonApiWrapper() {
    return new JsonApiListResponseWrapper(
        List.of(givenOrganisationData()),
        new JsonApiLinksFull(ORGANISATION_PATH)
    );
  }

  public static JsonApiListResponseWrapper givenCountryJsonApiWrapper() {
    return new JsonApiListResponseWrapper(
        List.of(givenCountryData()),
        new JsonApiLinksFull(ORGANISATION_PATH)
    );
  }

}