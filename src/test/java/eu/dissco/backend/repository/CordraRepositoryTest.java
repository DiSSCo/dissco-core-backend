package eu.dissco.backend.repository;

import static eu.dissco.backend.util.TestUtils.ID;
import static eu.dissco.backend.util.TestUtils.ORGANISATION_NAME;
import static eu.dissco.backend.util.TestUtils.givenCordraOrganisationObject;
import static eu.dissco.backend.util.TestUtils.givenCordraSpecimenObject;
import static eu.dissco.backend.util.TestUtils.givenOrganisationTuple;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import eu.dissco.backend.domain.OrganisationTuple;
import eu.dissco.backend.properties.CordraProperties;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import net.cnri.cordra.api.CordraClient;
import net.cnri.cordra.api.CordraException;
import net.cnri.cordra.api.CordraObject;
import net.cnri.cordra.api.QueryParams;
import net.cnri.cordra.api.SearchResults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CordraRepositoryTest {

  @Mock
  private CordraClient client;
  @Mock
  private CordraProperties properties;
  @Mock
  private SearchResults<CordraObject> searchResults;

  private CordraRepository repository;

  @BeforeEach
  void setup() {
    this.repository = new CordraRepository(client, properties);
  }

  @Test
  void testGetspecimen() throws CordraException {
    // Given
    given(client.search(anyString(), any(QueryParams.class))).willReturn(searchResults);

    // When
    var result = repository.getSpecimen(1, 10);

    // Then
    assertThat(result).isEqualTo(searchResults);
  }

  @Test
  void testGetSpecimenById() throws CordraException, IOException {
    // Given
    var cordraObject = givenCordraSpecimenObject();
    given(client.get(anyString())).willReturn(cordraObject);

    // When
    var result = repository.getSpecimenById(ID);

    // Then
    assertThat(result).isEqualTo(cordraObject);
  }

  @Test
  void testSearch() throws CordraException {
    // Given
    given(client.search(anyString(), any(QueryParams.class))).willReturn(searchResults);

    // When
    var result = repository.search("*", 1, 10);

    // Then
    assertThat(result).isEqualTo(searchResults);
  }

  @Test
  void testGetOrganisationNames() throws CordraException, IOException {
    // Given
    given(searchResults.stream()).willReturn(
        Stream.of(givenCordraOrganisationObject("test-organisation.json")));
    given(client.search(anyString())).willReturn(searchResults);

    // When
    var result = repository.getOrganisationNames();

    // Then
    assertThat(result).isEqualTo(List.of(ORGANISATION_NAME));
  }

  @Test
  void testGetOrganisationTuples() throws CordraException, IOException {
    // Given
    given(searchResults.stream()).willReturn(
        Stream.of(givenCordraOrganisationObject("test-organisation.json")));
    given(client.search(anyString())).willReturn(searchResults);

    // When
    var result = repository.getOrganisationTuple();

    // Then
    assertThat(result).isEqualTo(List.of(givenOrganisationTuple()));
  }

  @Test
  void testGetOrganisationTuplesNoROR() throws CordraException, IOException {
    // Given
    given(searchResults.stream()).willReturn(
        Stream.of(givenCordraOrganisationObject("test-organisation-no-ror.json")));
    given(client.search(anyString())).willReturn(searchResults);

    // When
    var result = repository.getOrganisationTuple();

    // Then
    assertThat(result).isEqualTo(List.of(new OrganisationTuple(ORGANISATION_NAME, null)));
  }

}
