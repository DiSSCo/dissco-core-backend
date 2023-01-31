package eu.dissco.backend.controller;

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

import static eu.dissco.backend.TestUtils.SANDBOX_URI;
import static eu.dissco.backend.TestUtils.givenDigitalMediaObject;
import static eu.dissco.backend.TestUtils.givenMediaJsonResponse;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DigitalMediaObjectControllerTest {
  @Mock
  private DigitalMediaObjectService service;
  private DigitalMediaObjectController controller;

  @BeforeEach
  void setup() { controller = new DigitalMediaObjectController(service); }

  @Test
  void testGetDigitalMediaObject(){
    // Given
    int pageNumber = 1;
    int pageSize = 10;
    var mediaObjects = Collections.nCopies(pageSize, givenDigitalMediaObject("id"));
    given(service.getDigitalMediaObjects(pageNumber, pageSize)).willReturn(mediaObjects);

    // When
    var responseReceived = controller.getDigitalMediaObjects(pageNumber, pageSize);

    // Then
    assertThat(responseReceived.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testGetDigitalMediaObjectsNameJsonResponse(){
    // Given
    String requestUri = "api/v1/digitalMedia/json";
    String path = SANDBOX_URI +  requestUri;
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI(requestUri);
    int pageNumber = 1;
    int pageSize = 10;
    int totalPageCount = 10;
    List<String> mediaIds = Collections.nCopies(pageSize, "id");
    given(service.getDigitalMediaObjectsJsonResponse(pageNumber, pageSize, path)).willReturn(givenMediaJsonResponse(
        path, pageNumber, pageSize, totalPageCount, mediaIds));

    // When
    var responseReceived = controller.getDigitalMediaObjectsNameJsonResponse(pageNumber, pageSize, request);

    assertThat(responseReceived.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testGetMultiMediaById(){
    // Given
    String prefix = "20.5000.1025";
    String postfix = "abc";
    String id = prefix + "/" + postfix;
    given(service.getDigitalMediaById(id)).willReturn(givenDigitalMediaObject(id));

    // When
    var responseReceived = controller.getMultiMediaById(prefix, postfix);

    // Then
    assertThat(responseReceived.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testGetLatestDigitalMediaObjectByIdJsonResponse(){
    // Given
    String prefix = "20.5000.1025";
    String postfix = "abc";
    String id = prefix + "/" + postfix;
    String requestUri = "api/v1/digitalMedia/json/" + id;
    String path = SANDBOX_URI +  requestUri;
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI(requestUri);
    given(service.getDigitalMediaByIdJsonResponse(id, path)).willReturn(givenMediaJsonResponse(path, id));

    // When
    var responseReceived = controller.getMultiMediaByIdJsonResponse(prefix, postfix, request);

    // Then
    assertThat(responseReceived.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testGetDigitalMediaVersions(){
    // Given
    String prefix = "20.5000.1025";
    String postfix = "abc";
    String id = prefix + "/" + postfix;
    List<Integer> versions = List.of(1, 2, 3);
    given(service.getDigitalMediaVersions(id)).willReturn(versions);

    // When
    var responseReceived = controller.getDigitalMediaVersions(prefix, postfix);

    // Then
    assertThat(responseReceived.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testGetDigitalMediaObjectVersion(){
    // Given
    String prefix = "20.5000.1025";
    String postfix = "abc";
    String id = prefix + "/" + postfix;
    int version = 1;
    given(service.getDigitalMediaVersion(id, version)).willReturn(givenDigitalMediaObject(id));

    // When
    var responseReceived = controller.getDigitalMediaObject(prefix, postfix, version);

    // Then
    assertThat(responseReceived.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testGetDigitalMediaObjectJsonResponse(){
    // Given
    String prefix = "20.5000.1025";
    String postfix = "abc";
    String id = prefix + "/" + postfix;
    int version = 1;
    String requestUri = "api/v1/digitalMedia/json/" + id;
    String path = SANDBOX_URI +  requestUri;
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI(requestUri);
    given(service.getDigitalMediaVersionJsonResponse(id, version, path)).willReturn(givenMediaJsonResponse(path, id));

    // When
    var responseReceived = controller.getDigitalMediaObjectJsonResponse(prefix, postfix, version, request);

    // Then
    assertThat(responseReceived.getStatusCode()).isEqualTo(HttpStatus.OK);
  }
}
