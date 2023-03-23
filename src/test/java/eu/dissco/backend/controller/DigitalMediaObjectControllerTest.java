package eu.dissco.backend.controller;

import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.POSTFIX;
import static eu.dissco.backend.TestUtils.PREFIX;
import static eu.dissco.backend.TestUtils.USER_ID_TOKEN;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationJsonResponseNoPagination;
import static eu.dissco.backend.utils.DigitalMediaObjectUtils.DIGITAL_MEDIA_PATH;
import static eu.dissco.backend.utils.DigitalMediaObjectUtils.DIGITAL_MEDIA_URI;
import static eu.dissco.backend.utils.DigitalMediaObjectUtils.givenDigitalMediaJsonResponse;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.service.DigitalMediaObjectService;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

@ExtendWith(MockitoExtension.class)
class DigitalMediaObjectControllerTest {

  @Mock
  private DigitalMediaObjectService service;
  private DigitalMediaObjectController controller;
  private MockHttpServletRequest mockRequest;

  @BeforeEach
  void setup() {
    controller = new DigitalMediaObjectController(service);
    mockRequest = new MockHttpServletRequest();
    mockRequest.setRequestURI(DIGITAL_MEDIA_URI);
  }

  @Test
  void testGetDigitalMediaObjects() {
    // Given
    int pageNumber = 1;
    int pageSize = 10;
    List<String> mediaIds = Collections.nCopies(pageSize, ID);
    given(service.getDigitalMediaObjects(pageNumber, pageSize, DIGITAL_MEDIA_PATH)).willReturn(
        givenDigitalMediaJsonResponse(DIGITAL_MEDIA_PATH, pageNumber, pageSize,
            mediaIds));

    // When
    var responseReceived = controller.getDigitalMediaObjects(pageNumber, pageSize,
        mockRequest);

    assertThat(responseReceived.getStatusCode()).isEqualTo(HttpStatus.OK);
  }
  @Test
  void testGetLatestDigitalMediaObjectById() {
    // Given
    given(service.getDigitalMediaById(ID, DIGITAL_MEDIA_PATH)).willReturn(
        givenDigitalMediaJsonResponse(DIGITAL_MEDIA_PATH, ID));

    // When
    var responseReceived = controller.getDigitalMediaObjectById(PREFIX, POSTFIX, mockRequest);

    // Then
    assertThat(responseReceived.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testGetDigitalMediaVersions() throws NotFoundException {
    // When
    var responseReceived = controller.getDigitalMediaVersions(PREFIX, POSTFIX, mockRequest);

    // Then
    assertThat(responseReceived.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testGetDigitalMediaObjectVersion() throws NotFoundException, JsonProcessingException {
    // Given
    int version = 1;
    given(service.getDigitalMediaObjectByVersion(ID, version, DIGITAL_MEDIA_PATH)).willReturn(givenDigitalMediaJsonResponse(DIGITAL_MEDIA_PATH, ID));

    // When
    var responseReceived = controller.getDigitalMediaObjectByVersion(PREFIX, POSTFIX, version, mockRequest);

    // Then
    assertThat(responseReceived.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testGetAnnotationsById() {
    // Given
    var responseExpected = givenAnnotationJsonResponseNoPagination(USER_ID_TOKEN, List.of("1", "2"));

    given(service.getAnnotationsOnDigitalMedia(ID, DIGITAL_MEDIA_PATH)).willReturn(responseExpected);

    // When
    var responseReceived = controller.getMediaAnnotationsById(PREFIX, POSTFIX, mockRequest);

    // Then
    assertThat(responseReceived.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(responseReceived.getBody()).isEqualTo(responseExpected);
  }

}
