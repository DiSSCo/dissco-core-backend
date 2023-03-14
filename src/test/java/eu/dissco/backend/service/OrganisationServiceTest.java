package eu.dissco.backend.service;

import static eu.dissco.backend.utils.OrganisationUtils.ORGANISATION_PATH;
import static eu.dissco.backend.utils.OrganisationUtils.givenCountryData;
import static eu.dissco.backend.utils.OrganisationUtils.givenCountryJsonApiWrapper;
import static eu.dissco.backend.utils.OrganisationUtils.givenOrganisationData;
import static eu.dissco.backend.utils.OrganisationUtils.givenOrganisationJsonApiWrapper;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;

import eu.dissco.backend.repository.OrganisationRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrganisationServiceTest {

  private OrganisationService service;
  @Mock
  private OrganisationRepository repository;

  @BeforeEach
  void setup(){
    service = new OrganisationService(repository);
  }

  @Test
  void testGetOrganisationNames(){
    // Given
    List<String> expected = List.of("a", "b");
    given(repository.getOrganisationNames()).willReturn(expected);

    // When
    var result = service.getOrganisationNames();

    // Then
    assertThat(result).isEqualTo(expected);

  }

  @Test
  void testGetOrganisations(){
    // Given
    given(repository.getOrganisations()).willReturn(List.of(givenOrganisationData()));
    var expected = givenOrganisationJsonApiWrapper();

    // When
    var received = service.getOrganisations(ORGANISATION_PATH);

    // Then
    assertThat(received).isEqualTo(expected);
  }

  @Test
  void testGetCountries(){
    // Given
    given(repository.getCountries()).willReturn(List.of(givenCountryData()));
    var expected = givenCountryJsonApiWrapper();

    // When
    var received = service.getCountries(ORGANISATION_PATH);

    // Then
    assertThat(received).isEqualTo(expected);
  }

}
