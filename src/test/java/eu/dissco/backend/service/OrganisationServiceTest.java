package eu.dissco.backend.service;

import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.utils.OrganisationUtils.COUNTRY;
import static eu.dissco.backend.utils.OrganisationUtils.ORGANISATION;
import static eu.dissco.backend.utils.OrganisationUtils.ORGANISATION_PATH;
import static eu.dissco.backend.utils.OrganisationUtils.givenCountryJsonApiWrapper;
import static eu.dissco.backend.utils.OrganisationUtils.givenOrganisationJsonApiWrapper;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;

import eu.dissco.backend.repository.OrganisationRepository;
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
  void setup() {
    service = new OrganisationService(repository, MAPPER);
  }

  @Test
  void testGetOrganisationNames() {
    // Given
    List<String> expected = List.of("a", "b");
    int pageNum = 1;
    int pageSize = expected.size();
    given(repository.getOrganisationNames(pageNum, pageSize)).willReturn(expected);

    // When
    var result = service.getOrganisationNames(pageNum, pageSize);

    // Then
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void testGetOrganisations() {
    // Given
    int pageNum = 1;
    int pageSize = 1;
    given(repository.getOrganisations(pageNum, pageSize)).willReturn(List.of(ORGANISATION));
    var expected = givenOrganisationJsonApiWrapper();

    // When
    var received = service.getOrganisations(ORGANISATION_PATH, pageNum, pageSize);

    // Then
    assertThat(received).isEqualTo(expected);
  }

  @Test
  void testGetCountries() {
    // Given
    int pageNum = 1;
    int pageSize = 1;
    given(repository.getCountries(pageNum, pageSize)).willReturn(List.of(COUNTRY));
    var expected = givenCountryJsonApiWrapper();

    // When
    var received = service.getCountries(ORGANISATION_PATH, pageNum, pageSize);

    // Then
    assertThat(received).isEqualTo(expected);
  }

}
