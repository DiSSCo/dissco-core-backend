package eu.dissco.backend.controller;

import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.POSTFIX;
import static eu.dissco.backend.TestUtils.PREFIX;
import static eu.dissco.backend.TestUtils.SANDBOX_URI;
import static eu.dissco.backend.TestUtils.USER_ID_TOKEN;
import static eu.dissco.backend.utils.AnnotationUtils.ANNOTATION_PATH;
import static eu.dissco.backend.utils.AnnotationUtils.ANNOTATION_URI;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationJsonResponse;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationResponse;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationResponseSingleDataNode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.dissco.backend.domain.AnnotationRequest;
import eu.dissco.backend.domain.AnnotationResponse;
import eu.dissco.backend.exceptions.NoAnnotationFoundException;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.service.AnnotationService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class AnnotationControllerTest {

  @Mock
  private AnnotationService service;
  @Mock
  private KeycloakPrincipal<KeycloakSecurityContext> principal;
  @Mock
  private KeycloakSecurityContext securityContext;
  @Mock
  private AccessToken accessToken;
  private Authentication authentication;
  private AnnotationController controller;

  private MockHttpServletRequest mockRequest;

  @BeforeEach
  void setup() {
    controller = new AnnotationController(service);
    mockRequest = new MockHttpServletRequest();
    mockRequest.setRequestURI(ANNOTATION_URI);
  }

  @Test
  void testGetAnnotation(){
    var expectedResponse = givenAnnotationResponseSingleDataNode(ANNOTATION_PATH);
    given(service.getAnnotation(ID, ANNOTATION_PATH)).willReturn(expectedResponse);

    // When
    var receivedResponse = controller.getAnnotation(PREFIX, POSTFIX, mockRequest);

    // Then
    assertThat(receivedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(receivedResponse.getBody()).isEqualTo(expectedResponse);
  }

  @Test
  void testGetLatestAnnotations() throws IOException {
    // Given
    String requestUri = "api/v1/annotations/latest/json";
    String path = SANDBOX_URI + requestUri;
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI(requestUri);

    int pageNumber = 1;
    int pageSize = 11;
    String annotationId = "123";
    var expectedJson = givenAnnotationJsonResponse(path, pageNumber, pageSize,
        USER_ID_TOKEN, annotationId, true);
    var expectedResponse = ResponseEntity.ok(expectedJson);
    given(service.getLatestAnnotations(pageNumber, pageSize, path)).willReturn(
        expectedJson);

    // When
    var receivedResponse = controller.getLatestAnnotations(pageNumber, pageSize,
        request);

    // Then
    assertThat(receivedResponse).isEqualTo(expectedResponse);
  }

  @Test
  void testGetAnnotationVersion() throws Exception {
    // Given
    int version = 1;
    var expectedResponse = ResponseEntity.ok(givenAnnotationResponseSingleDataNode(ANNOTATION_PATH));
    given(service.getAnnotationByVersion(ID, version, ANNOTATION_PATH)).willReturn(expectedResponse.getBody());

    // When
    var receivedResponse = controller.getAnnotationByVersion(PREFIX, POSTFIX, version, mockRequest);

    // Then
    assertThat(receivedResponse).isEqualTo(expectedResponse);
  }

  @Test
  void testGetAnnotations() {
    // Given
    int pageNumber = 1;
    int pageSize = 11;
    MockHttpServletRequest r = new MockHttpServletRequest();
    r.setRequestURI("");

    var expectedJson = givenAnnotationJsonResponse(ANNOTATION_PATH, pageNumber, pageSize,
        USER_ID_TOKEN, ID, true);
    var expectedResponse = ResponseEntity.ok(expectedJson);
    given(service.getAnnotations(pageNumber, pageSize, ANNOTATION_PATH)).willReturn(expectedJson);

    // When
    var receivedResponse = controller.getAnnotations(pageNumber, pageSize, mockRequest);

    // Then
    assertThat(receivedResponse).isEqualTo(expectedResponse);
  }


  @Test
  void testCreateAnnotation(){
    // Given
    givenAuthentication(USER_ID_TOKEN);
    AnnotationRequest request = new AnnotationRequest("type", "motivation", null, null);
    var expectedResponse = givenAnnotationResponseSingleDataNode(ANNOTATION_PATH);
    given(service.persistAnnotation(request, USER_ID_TOKEN, ANNOTATION_PATH))
        .willReturn(expectedResponse);

    // When
    var receivedResponse = controller.createAnnotation(authentication, request, mockRequest);

    // Then
    assertThat(receivedResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(receivedResponse.getBody()).isEqualTo(expectedResponse);
  }

  @Test
  void testGetAnnotationsForUserJsonResponse() {
    // Given
    givenAuthentication(USER_ID_TOKEN);

    String requestUri = "api/v1/annotations/latest/json";
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI(requestUri);

    // When
    var receivedResponse = controller.getAnnotationsForUser(1, 1, request,
        authentication);

    // Then
    assertThat(receivedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testGetAnnotationsByVersion() throws NotFoundException {
    // Given

    // When
    var receivedResponse = controller.getAnnotationVersions(PREFIX, POSTFIX, mockRequest);

    // Then
    assertThat(receivedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testDeleteAnnotationSuccess() throws NoAnnotationFoundException {
    // Given
    givenAuthentication(USER_ID_TOKEN);
    given(service.deleteAnnotation(PREFIX, POSTFIX, USER_ID_TOKEN)).willReturn(true);

    // When
    var receivedResponse = controller.deleteAnnotation(authentication, PREFIX, POSTFIX);

    // Then
    assertThat(receivedResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
  }

  @Test
  void testDeleteAnnotationFailure() throws NoAnnotationFoundException {
    // Given
    givenAuthentication(USER_ID_TOKEN);
    given(service.deleteAnnotation(PREFIX, POSTFIX, USER_ID_TOKEN)).willReturn(false);

    // When
    var receivedResponse = controller.deleteAnnotation(authentication, PREFIX, POSTFIX);

    // Then
    assertThat(receivedResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  private void givenAuthentication(String userId) {
    authentication = new TestingAuthenticationToken(principal, null);
    given(principal.getKeycloakSecurityContext()).willReturn(securityContext);
    given(securityContext.getToken()).willReturn(accessToken);
    given(accessToken.getSubject()).willReturn(userId);
  }
}
