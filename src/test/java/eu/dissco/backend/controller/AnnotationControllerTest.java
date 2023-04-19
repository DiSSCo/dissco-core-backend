package eu.dissco.backend.controller;

import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.PREFIX;
import static eu.dissco.backend.TestUtils.SUFFIX;
import static eu.dissco.backend.TestUtils.USER_ID_TOKEN;
import static eu.dissco.backend.utils.AnnotationUtils.ANNOTATION_PATH;
import static eu.dissco.backend.utils.AnnotationUtils.ANNOTATION_URI;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationJsonResponse;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationRequest;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationResponseSingleDataNode;
import static eu.dissco.backend.utils.AnnotationUtils.givenJsonApiAnnotationRequest;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;

import eu.dissco.backend.domain.AnnotationRequest;
import eu.dissco.backend.exceptions.NoAnnotationFoundException;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.service.AnnotationService;
import java.io.IOException;
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
  private Authentication authentication;
  private AnnotationController controller;

  private MockHttpServletRequest mockRequest;

  @BeforeEach
  void setup() {
    controller = new AnnotationController(service, MAPPER);
    mockRequest = new MockHttpServletRequest();
    mockRequest.setRequestURI(ANNOTATION_URI);
  }

  @Test
  void testGetAnnotation(){
    var expectedResponse = givenAnnotationResponseSingleDataNode(ANNOTATION_PATH);
    given(service.getAnnotation(ID, ANNOTATION_PATH)).willReturn(expectedResponse);

    // When
    var receivedResponse = controller.getAnnotation(PREFIX, SUFFIX, mockRequest);

    // Then
    assertThat(receivedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(receivedResponse.getBody()).isEqualTo(expectedResponse);
  }

  @Test
  void testGetLatestAnnotations() throws IOException {
    // Given
    int pageNumber = 1;
    int pageSize = 11;
    String annotationId = "123";
    var expectedJson = givenAnnotationJsonResponse(ANNOTATION_PATH, pageNumber, pageSize,
        USER_ID_TOKEN, annotationId, true);
    var expectedResponse = ResponseEntity.ok(expectedJson);
    given(service.getLatestAnnotations(pageNumber, pageSize, ANNOTATION_PATH)).willReturn(
        expectedJson);

    // When
    var receivedResponse = controller.getLatestAnnotations(pageNumber, pageSize,
        mockRequest);

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
    var receivedResponse = controller.getAnnotationByVersion(PREFIX, SUFFIX, version, mockRequest);

    // Then
    assertThat(receivedResponse).isEqualTo(expectedResponse);
  }

  @Test
  void testGetAnnotations() {
    // Given
    int pageNumber = 1;
    int pageSize = 11;

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
  void testCreateAnnotation() throws Exception{
    // Given
    givenAuthentication(USER_ID_TOKEN);
    AnnotationRequest annotation = givenAnnotationRequest();
    var request = givenJsonApiAnnotationRequest(annotation);
    var expectedResponse = givenAnnotationResponseSingleDataNode(ANNOTATION_PATH);
    given(service.persistAnnotation(annotation, USER_ID_TOKEN, ANNOTATION_PATH))
        .willReturn(expectedResponse);

    // When
    var receivedResponse = controller.createAnnotation(authentication, request, mockRequest);

    // Then
    assertThat(receivedResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(receivedResponse.getBody()).isEqualTo(expectedResponse);
  }

  @Test
  void testUpdateAnnotation() throws Exception{
    // Given
    givenAuthentication(USER_ID_TOKEN);
    AnnotationRequest annotation = givenAnnotationRequest();
    var requestBody = givenJsonApiAnnotationRequest(annotation);
    var expected = givenAnnotationResponseSingleDataNode(ANNOTATION_PATH);
    given(service.updateAnnotation(ID, annotation, USER_ID_TOKEN, ANNOTATION_PATH)).willReturn(expected);

    // When
    var result = controller.updateAnnotation(authentication, requestBody, PREFIX, SUFFIX, mockRequest);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isEqualTo(expected);
  }

  @Test
  void testGetAnnotationsForUserJsonResponse() {
    // Given
    givenAuthentication(USER_ID_TOKEN);

    // When
    var receivedResponse = controller.getAnnotationsForUser(1, 1, mockRequest,
        authentication);

    // Then
    assertThat(receivedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testGetAnnotationsByVersion() throws NotFoundException {
    // Given

    // When
    var receivedResponse = controller.getAnnotationVersions(PREFIX, SUFFIX, mockRequest);

    // Then
    assertThat(receivedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testDeleteAnnotationSuccess() throws NoAnnotationFoundException {
    // Given
    givenAuthentication(USER_ID_TOKEN);
    given(service.deleteAnnotation(PREFIX, SUFFIX, USER_ID_TOKEN)).willReturn(true);

    // When
    var receivedResponse = controller.deleteAnnotation(authentication, PREFIX, SUFFIX);

    // Then
    assertThat(receivedResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
  }

  @Test
  void testDeleteAnnotationFailure() throws NoAnnotationFoundException {
    // Given
    givenAuthentication(USER_ID_TOKEN);
    given(service.deleteAnnotation(PREFIX, SUFFIX, USER_ID_TOKEN)).willReturn(false);

    // When
    var receivedResponse = controller.deleteAnnotation(authentication, PREFIX, SUFFIX);

    // Then
    assertThat(receivedResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  private void givenAuthentication(String userId) {
    given(authentication.getName()).willReturn(userId);
  }
}
