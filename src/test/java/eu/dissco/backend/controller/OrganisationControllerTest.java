package eu.dissco.backend.controller;

import static eu.dissco.backend.utils.OrganisationUtils.ORGANISATION_URI;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import eu.dissco.backend.service.OrganisationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

@ExtendWith(MockitoExtension.class)
class OrganisationControllerTest {

  @Mock
  private OrganisationService service;
  private OrganisationController controller;
  private final MockHttpServletRequest mockRequest = new MockHttpServletRequest();

  @BeforeEach
  void setup() {
    controller = new OrganisationController(service);
    mockRequest.setRequestURI(ORGANISATION_URI);
  }


  @Test
  void testGetOrganisationNames() {
    // When
    var result = controller.getOrganisationNames(1, 1);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testGetOrganisations() {
    // When
    var result = controller.getOrganisations(1, 1, mockRequest);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testGetCountries() {
    // When
    var result = controller.getOrganisationCountries(1, 1, mockRequest);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

}
