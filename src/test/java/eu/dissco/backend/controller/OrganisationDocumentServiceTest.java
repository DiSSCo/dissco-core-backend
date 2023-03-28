package eu.dissco.backend.controller;

import eu.dissco.backend.domain.OrganisationDocument;
import eu.dissco.backend.service.OrganisationDocumentService;
import eu.dissco.backend.service.OrganisationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    var requestBody = new OrganisationDocument();
  }

}
