package eu.dissco.backend.controller;

import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.POSTFIX;
import static eu.dissco.backend.TestUtils.PREFIX;
import static eu.dissco.backend.TestUtils.SANDBOX_URI;
import static eu.dissco.backend.TestUtils.USER_ID_TOKEN;
import static eu.dissco.backend.TestUtils.givenAnnotationJsonResponse;
import static eu.dissco.backend.TestUtils.givenDigitalMediaJsonResponse;
import static eu.dissco.backend.TestUtils.givenDigitalMediaObject;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;

import eu.dissco.backend.TestUtils;
import eu.dissco.backend.domain.JsonApiListResponseWrapper;
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

  @BeforeEach
  void setup() {
    controller = new DigitalMediaObjectController(service);
  }

  @Test
  void testGetDigitalMediaObject() {
    // Given
    int pageNumber = 1;
    int pageSize = 10;
    var mediaObjects = Collections.nCopies(pageSize, givenDigitalMediaObject(ID));
    given(service.getDigitalMediaObjects(pageNumber, pageSize)).willReturn(mediaObjects);

    // When
    var responseReceived = controller.getDigitalMediaObjects(pageNumber, pageSize);

    // Then
    assertThat(responseReceived.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testGetDigitalMediaObjectsNameJsonResponse() {
    // Given
    String requestUri = "api/v1/digitalMedia/json";
    String path = SANDBOX_URI + requestUri;
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI(requestUri);
    int pageNumber = 1;
    int pageSize = 10;
    List<String> mediaIds = Collections.nCopies(pageSize, ID);
    given(service.getDigitalMediaObjectsJsonResponse(pageNumber, pageSize, path)).willReturn(
        TestUtils.givenDigitalMediaJsonResponse(path, pageNumber, pageSize,
            mediaIds));

    // When
    var responseReceived = controller.getDigitalMediaObjectsNameJsonResponse(pageNumber, pageSize,
        request);

    assertThat(responseReceived.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testGetMultiMediaById() {
    // Given
    given(service.getDigitalMediaById(ID)).willReturn(givenDigitalMediaObject(ID));

    // When
    var responseReceived = controller.getMultiMediaById(PREFIX, POSTFIX);

    // Then
    assertThat(responseReceived.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testGetLatestDigitalMediaObjectByIdJsonResponse() {
    // Given
    String requestUri = "api/v1/digitalMedia/json/" + ID;
    String path = SANDBOX_URI + requestUri;
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI(requestUri);
    given(service.getDigitalMediaByIdJsonResponse(ID, path)).willReturn(
        givenDigitalMediaJsonResponse(path, ID));

    // When
    var responseReceived = controller.getMultiMediaByIdJsonResponse(PREFIX, POSTFIX, request);

    // Then
    assertThat(responseReceived.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testGetDigitalMediaVersions() {
    // Given
    List<Integer> versions = List.of(1, 2, 3);
    given(service.getDigitalMediaVersions(ID)).willReturn(versions);

    // When
    var responseReceived = controller.getDigitalMediaVersions(PREFIX, POSTFIX);

    // Then
    assertThat(responseReceived.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testGetDigitalMediaObjectVersion() {
    // Given
    int version = 1;
    given(service.getDigitalMediaVersion(ID, version)).willReturn(givenDigitalMediaObject(ID));

    // When
    var responseReceived = controller.getDigitalMediaObject(PREFIX, POSTFIX, version);

    // Then
    assertThat(responseReceived.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testGetDigitalMediaObjectJsonResponse() {
    // Given
    int version = 1;
    String requestUri = "api/v1/digitalMedia/json/" + ID;
    String path = SANDBOX_URI + requestUri;
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI(requestUri);
    given(service.getDigitalMediaVersionJsonResponse(ID, version, path)).willReturn(
        givenDigitalMediaJsonResponse(path, ID));

    // When
    var responseReceived = controller.getDigitalMediaObjectJsonResponse(PREFIX, POSTFIX, version,
        request);

    // Then
    assertThat(responseReceived.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testGetAnnotationsById() {
    // Given
    int pageNum = 1;
    int pageSize = 10;
    String requestUri = "api/v1/digitalMedia/" + ID + "/annotations/json";
    String path = SANDBOX_URI + requestUri;
    JsonApiListResponseWrapper responseExpected = givenAnnotationJsonResponse(path, pageNum, pageSize,
        USER_ID_TOKEN, "123", true);
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI(requestUri);
    given(service.getAnnotationsOnDigitalMediaObject(ID, path, pageNum, pageSize)).willReturn(
        responseExpected);

    // When
    var responseReceived = controller.getAnnotationsById(PREFIX, POSTFIX, pageNum, pageSize,
        request);

    // Then
    assertThat(responseReceived.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(responseReceived.getBody()).isEqualTo(responseExpected);
  }

}
