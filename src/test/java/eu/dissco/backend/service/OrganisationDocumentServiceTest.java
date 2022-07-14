package eu.dissco.backend.service;

import static eu.dissco.backend.util.TestUtils.ORGANISATION_ROR;
import static eu.dissco.backend.util.TestUtils.givenOrganisationDocument;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import eu.dissco.backend.repository.OrganisationDocumentRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrganisationDocumentServiceTest {

  @Mock
  private OrganisationDocumentRepository repository;

  private OrganisationDocumentService service;

  @BeforeEach
  void setup() {
    service = new OrganisationDocumentService(repository);
  }

  @Test
  void testCreateNewDocument() {
    // Given

    // When
    service.createNewDocument(givenOrganisationDocument());

    // Then
    then(repository).should().saveNewDocument(eq(givenOrganisationDocument()));
  }

  @Test
  void testGetDocument() {
    // Given
    given(repository.getDocument(anyString())).willReturn(givenOrganisationDocument());

    // When
    var result = service.getDocument("test/123");

    // Then
    assertThat(result).isEqualTo(givenOrganisationDocument());
  }

  @Test
  void testGetDocumentForOrganisation() {
    // Given
    given(repository.getDocumentsForOrganisation(anyString())).willReturn(List.of(givenOrganisationDocument()));

    // When
    var result = service.getDocumentsForOrganisation(ORGANISATION_ROR);

    // Then
    assertThat(result).isEqualTo(List.of(givenOrganisationDocument()));
  }

}
