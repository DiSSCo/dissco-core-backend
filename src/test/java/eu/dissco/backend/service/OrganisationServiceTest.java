package eu.dissco.backend.service;

import static eu.dissco.backend.util.TestUtils.ORGANISATION_NAME;
import static eu.dissco.backend.util.TestUtils.givenOrganisationDocument;
import static eu.dissco.backend.util.TestUtils.givenOrganisationTuple;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import eu.dissco.backend.domain.OrganisationDocument;
import eu.dissco.backend.repository.OrganisationRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrganisationServiceTest {

  @Mock
  private OrganisationRepository repository;

  private OrganisationService service;

  @BeforeEach
  void setup() {
    this.service = new OrganisationService(repository);
  }

  @Test
  void testGetNames() {
    // Given
    given(repository.getOrganisationNames()).willReturn(List.of(ORGANISATION_NAME));

    // When
    var result = service.getNames();

    // Then
    assertThat(result).isEqualTo(List.of(ORGANISATION_NAME));
  }

  @Test
  void testGetTuples() {
    // Given
    given(repository.getOrganisationTuple()).willReturn(List.of(givenOrganisationTuple()));

    // When
    var result = service.getTuples();

    // Then
    assertThat(result).isEqualTo(List.of(givenOrganisationTuple()));
  }

  @Test
  void testCreateNewDocument() {
    // Given

    // When
    var result = service.createNewDocument(givenOrganisationDocument());

    // Then
    then(repository).should().saveNewDocument(eq(givenOrganisationDocument()));
  }

}
