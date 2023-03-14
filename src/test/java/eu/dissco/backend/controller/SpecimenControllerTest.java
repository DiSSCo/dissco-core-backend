package eu.dissco.backend.controller;

import static eu.dissco.backend.TestUtils.POSTFIX;
import static eu.dissco.backend.TestUtils.PREFIX;
import static eu.dissco.backend.utils.SpecimenUtils.SPECIMEN_URI;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import eu.dissco.backend.service.SpecimenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

@ExtendWith(MockitoExtension.class)
class SpecimenControllerTest {
  @Mock
  private SpecimenService service;
  MockHttpServletRequest mockRequest;
  private SpecimenController controller;

  @BeforeEach
  void setup(){
    controller = new SpecimenController(service);
    mockRequest = new MockHttpServletRequest();
    mockRequest.setRequestURI(SPECIMEN_URI);
  }

  @Test
  void testGetSpecimen(){
    // When
    var result = controller.getSpecimen(1, 1, mockRequest);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testGetLatestSpecimen() throws Exception {
    var result = controller.getLatestSpecimen(1, 1, mockRequest);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testGetSpecimenById(){
    // When
    var result = controller.getSpecimenById(PREFIX, POSTFIX, mockRequest);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testGetSpecimenByIdJsonLd(){
    // When
    var result = controller.getSpecimenByIdJsonLD(PREFIX, POSTFIX, mockRequest);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testGetSpecimenByIdFull(){
    // When
    var result = controller.getSpecimenByIdFull(PREFIX, POSTFIX, mockRequest);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testGetSpecimenByVersion() throws Exception {
    // When
    var result = controller.getSpecimenByVersion(PREFIX, POSTFIX, 1, mockRequest);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testGetSpecimenVersions() throws Exception {
    // When
    var result = controller.getSpecimenVersions(PREFIX, POSTFIX, mockRequest);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testGetSpecimenAnnotations() throws Exception {
    // When
    var result = controller.getSpecimenAnnotations(PREFIX, POSTFIX, mockRequest);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testGetSpecimenDigitalMedia(){
    // When
    var result = controller.getSpecimenDigitalMedia(PREFIX, POSTFIX, mockRequest);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testSearch() throws Exception {
    // When
    var result = controller.searchSpecimen("", 1, 1, mockRequest);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
  }






}
