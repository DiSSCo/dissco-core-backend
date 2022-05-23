package eu.dissco.backend.service;

import static eu.dissco.backend.util.TestUtils.ORGANISATION_NAME;
import static eu.dissco.backend.util.TestUtils.givenOrganisationTuple;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import eu.dissco.backend.repository.CordraRepository;
import eu.dissco.backend.util.TestUtils;
import java.util.List;
import net.cnri.cordra.api.CordraException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrganisationServiceTest {

  @Mock
  private CordraRepository repository;

  private OrganisationService service;

  @BeforeEach
  void setup() {
    this.service = new OrganisationService(repository);
  }

  @Test
  void testGetNames() throws CordraException {
    // Given
    given(repository.getOrganisationNames()).willReturn(List.of(ORGANISATION_NAME));

    // When
    var result = service.getNames();

    // Then
    assertThat(result).isEqualTo(List.of(ORGANISATION_NAME));
  }

  @Test
  void testGetTuples() throws CordraException {
    // Given
    given(repository.getOrganisationTuple()).willReturn(List.of(givenOrganisationTuple()));

    // When
    var result = service.getTuples();

    // Then
    assertThat(result).isEqualTo(List.of(givenOrganisationTuple()));
  }


}
