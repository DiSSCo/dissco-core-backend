package eu.dissco.backend.controller;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;

import eu.dissco.backend.domain.OrganisationDocument;
import eu.dissco.backend.service.OrganisationDocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class OrganisationDocumentServiceTest {

  @Mock
  private OrganisationDocumentService service;
  private OrganisationDocumentController controller;

  @BeforeEach
  void setup(){
    controller = new OrganisationDocumentController(service);
  }

  @Test
  void testCreateDocument(){
    // Given
    var requestBody = new OrganisationDocument();
    given(service.createNewDocument(requestBody)).willReturn(requestBody);

    // When
    var result = controller.createDocument(requestBody);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
  }

}
