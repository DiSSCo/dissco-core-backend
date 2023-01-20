package eu.dissco.backend.controller;
import static eu.dissco.backend.TestUtils.USER_ID_TOKEN;
import static eu.dissco.backend.TestUtils.givenAnnotationJsonResponse;
import static eu.dissco.backend.TestUtils.givenAnnotationResponse;
import static org.mockito.BDDMockito.given;


import eu.dissco.backend.domain.AnnotationRequest;
import eu.dissco.backend.domain.AnnotationResponse;
import eu.dissco.backend.exceptions.NoAnnotationFoundException;
import eu.dissco.backend.service.AnnotationService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
@Slf4j
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

  @BeforeEach
  void setup() { controller = new AnnotationController(service); }

  @Test
  void testGetAnnotation(){
    // Given
    String prefix = "20.5000.1025";
    String postfix = "ABC-123-DEF";
    String id = prefix + "/" + postfix;
    var expectedResponse = ResponseEntity.ok(givenAnnotationResponse());
    given(service.getAnnotation(id)).willReturn(givenAnnotationResponse());

    // When
    var receivedResponse = controller.getAnnotation(prefix, postfix);

    // Then
    assertThat(receivedResponse).isEqualTo(expectedResponse);
  }

  @Test
  void testGetLatestAnnotations() throws IOException {
    int pageNumber = 1;
    int pageSize = 30;
    List<AnnotationResponse> expectedResponseList = new ArrayList<>();
    int annotationCount = pageNumber * pageSize;
    for (int i = 0; i < annotationCount; i++){
      expectedResponseList.add(givenAnnotationResponse());
    }
    given(service.getLatestAnnotations(pageNumber, pageSize)).willReturn(expectedResponseList);
    var expectedResponse = ResponseEntity.ok(expectedResponseList);

    // When
    var receivedResponse = controller.getLatestAnnotations(pageNumber, pageSize);

    // Then
    assertThat(receivedResponse).isEqualTo(expectedResponse);
  }

  @Test
  void testGetLatestAnnotationsJsonResponse() throws IOException {
    // Given
    String path = "sandbox.dissco.tech/api/v1/annotations/latest/json";
    int pageNumber = 1;
    int pageSize = 11;
    int totalPageCount = 100;
    String userId = USER_ID_TOKEN;
    String annotationId = "123";
    var expectedJson = givenAnnotationJsonResponse(path, pageNumber, pageSize, totalPageCount, userId, annotationId);
    var expectedResponse = ResponseEntity.ok(expectedJson);
    given(service.getLatestAnnotationsJsonResponse(pageNumber, pageSize, path)).willReturn(expectedJson);

    // When
    var receivedResponse = controller.getLatestAnnotationsJsonResponse(pageNumber, pageSize);

    // Then
    assertThat(receivedResponse).isEqualTo(expectedResponse);
  }

  @Test
  void testGetAnnotationVersion() {
    String prefix = "20.5000.1025";
    String postfix = "ABC-123-DEF";
    int version = 1;
    String id = prefix + "/" + postfix;
    var expectedResponse = ResponseEntity.ok(givenAnnotationResponse());
    given(service.getAnnotationVersion(id, version)).willReturn(givenAnnotationResponse());

    // When
    var receivedResponse = controller.getAnnotation(prefix, postfix, version);

    // Then
    assertThat(receivedResponse).isEqualTo(expectedResponse);
  }

  @Test
  void testGetAnnotationsJsonResponse(){
    int pageNumber = 1;
    int pageSize = 11;
    int totalPageCount = 100;
    String path = "sandbox.dissco.tech/api/v1/annotations/all/json";
    String userId = USER_ID_TOKEN;
    String annotationId = "123";
    var expectedJson = givenAnnotationJsonResponse(path, pageNumber, pageSize, totalPageCount, userId,
        annotationId);
    var expectedResponse = ResponseEntity.ok(expectedJson);
    given(service.getAnnotationsJsonResponse(pageNumber, pageSize, path)).willReturn(expectedJson);

    // When
    var receivedResponse = controller.getAnnotationsJsonResponse(pageNumber, pageSize);

    // Then
    assertThat(receivedResponse).isEqualTo(expectedResponse);
  }

  @Test
  void testGetAnnotations(){
    // Given
    int pageNumber = 1;
    int pageSize = 30;
    List<AnnotationResponse> expectedResponseList = new ArrayList<>();
    int annotationCount = pageNumber * pageSize;
    for (int i = 0; i < annotationCount; i++){
      expectedResponseList.add(givenAnnotationResponse());
    }

    given(service.getAnnotations(pageNumber, pageSize)).willReturn(expectedResponseList);
    var expectedResponse = ResponseEntity.ok(expectedResponseList);

    // When
    var receivedResponse = controller.getAnnotations(pageNumber, pageSize);

    assertThat(receivedResponse).isEqualTo(expectedResponse);
  }

  @Test
  void testCreateAnnotation(){
    // Given
    givenAuthentication(USER_ID_TOKEN);
    AnnotationRequest request = new AnnotationRequest("type", "motivation", null, null);
    given(service.persistAnnotation(request, USER_ID_TOKEN)).willReturn(givenAnnotationResponse());

    // When
    var receivedResponse = controller.createAnnotation(authentication, request);

    // Then
    assertThat(receivedResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
  }

  @Test
  void testUpdateAnnotation() throws NoAnnotationFoundException {
    // Given
    givenAuthentication(USER_ID_TOKEN);
    String prefix = "20.5000.1025";
    String postfix = "ABC-123-DEF";
    AnnotationRequest request = new AnnotationRequest("type", "motivation", null, null);
    // When
    var receivedResponse = controller.updateAnnotation(authentication, request, prefix, postfix);

    // Then
    assertThat(receivedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testGetAnnotationsForUser(){
    // Given
    givenAuthentication(USER_ID_TOKEN);
    int pageNumber = 0;
    int pageSize = 10;
    List<AnnotationResponse> annotations = Collections.nCopies(pageSize, givenAnnotationResponse());
    given(service.getAnnotationsForUser(USER_ID_TOKEN, pageNumber, pageSize)).willReturn(annotations);

    // When
    var receivedResponse  = controller.getAnnotationsForUser(pageNumber, pageSize, authentication);

    // Then
    assertThat(receivedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(receivedResponse.getBody()).isEqualTo(annotations);
  }

  @Test
  void testGetAnnotationsForUserJsonResponse(){
    // Given
    givenAuthentication(USER_ID_TOKEN);

    // When
    var receivedResponse = controller.getAnnotationsForUserJsonResponse(1, 1, authentication);

    // Then
    assertThat(receivedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testGetAnnotationsByVersion(){
    // When
    var receivedResponse = controller.getAnnotationByVersion("1", "1");

    // Then
    assertThat(receivedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testDeleteAnnotationSuccess() throws NoAnnotationFoundException {
    // Given
    givenAuthentication(USER_ID_TOKEN);
    String prefix = "20.5000.1025";
    String postfix = "ABC-123-DEF";
    given(service.deleteAnnotation(prefix, postfix, USER_ID_TOKEN)).willReturn(true);

    // When
    var receivedResponse = controller.deleteAnnotation(authentication, prefix, postfix);

    // Then
    assertThat(receivedResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
  }

  @Test
  void testDeleteAnnotationFailure() throws NoAnnotationFoundException {
    // Given
    givenAuthentication(USER_ID_TOKEN);
    String prefix = "20.5000.1025";
    String postfix = "ABC-123-DEF";
    given(service.deleteAnnotation(prefix, postfix, USER_ID_TOKEN)).willReturn(false);

    // When
    var receivedResponse = controller.deleteAnnotation(authentication, prefix, postfix);

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
