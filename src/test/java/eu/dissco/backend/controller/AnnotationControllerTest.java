package eu.dissco.backend.controller;

import static eu.dissco.backend.TestUtils.HANDLE;
import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.ORCID;
import static eu.dissco.backend.TestUtils.PREFIX;
import static eu.dissco.backend.TestUtils.SUFFIX;
import static eu.dissco.backend.TestUtils.USER_ID_TOKEN;
import static eu.dissco.backend.TestUtils.givenAdminClaims;
import static eu.dissco.backend.TestUtils.givenAgent;
import static eu.dissco.backend.TestUtils.givenClaims;
import static eu.dissco.backend.utils.AnnotationUtils.ANNOTATION_PATH;
import static eu.dissco.backend.utils.AnnotationUtils.ANNOTATION_URI;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationCountRequest;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationEventRequest;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationJsonResponse;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationRequest;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationResponseSingleDataNode;
import static eu.dissco.backend.utils.AnnotationUtils.givenJsonApiAnnotationRequest;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import eu.dissco.backend.component.SchemaValidatorComponent;
import eu.dissco.backend.exceptions.ForbiddenException;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.properties.ApplicationProperties;
import eu.dissco.backend.schema.AnnotationProcessingRequest;
import eu.dissco.backend.service.AnnotationService;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class AnnotationControllerTest {

  @Mock
  private AnnotationService service;
  @Mock
  private Authentication authentication;
  @Mock
  private ApplicationProperties applicationProperties;
  @Mock
  private SchemaValidatorComponent validatorComponent;
  private AnnotationController controller;

  private MockHttpServletRequest mockRequest;

  @BeforeEach
  void setup() {
    controller = new AnnotationController(applicationProperties, MAPPER, service,
        validatorComponent);
    mockRequest = new MockHttpServletRequest();
    mockRequest.setRequestURI(ANNOTATION_URI);
  }

  @Test
  void testGetAnnotation() {
    var expectedResponse = givenAnnotationResponseSingleDataNode(ANNOTATION_PATH);
    given(service.getAnnotation(ID, ANNOTATION_PATH)).willReturn(expectedResponse);
    given(applicationProperties.getBaseUrl()).willReturn("https://sandbox.dissco.tech");

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
    given(applicationProperties.getBaseUrl()).willReturn("https://sandbox.dissco.tech");

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
    var expectedResponse = ResponseEntity.ok(
        givenAnnotationResponseSingleDataNode(ANNOTATION_PATH));
    given(service.getAnnotationByVersion(HANDLE + ID, version, ANNOTATION_PATH)).willReturn(
        expectedResponse.getBody());
    given(applicationProperties.getBaseUrl()).willReturn("https://sandbox.dissco.tech");

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
    given(applicationProperties.getBaseUrl()).willReturn("https://sandbox.dissco.tech");

    // When
    var receivedResponse = controller.getAnnotations(pageNumber, pageSize, mockRequest);

    // Then
    assertThat(receivedResponse).isEqualTo(expectedResponse);
  }

  @Test
  void testCreateAnnotation() throws Exception {
    // Given
    givenAuthentication(givenClaims());
    var annotation = givenAnnotationRequest();

    var request = givenJsonApiAnnotationRequest(annotation);
    var expectedResponse = givenAnnotationResponseSingleDataNode(ANNOTATION_PATH);
    given(service.persistAnnotation(annotation, givenAgent(), ANNOTATION_PATH))
        .willReturn(expectedResponse);
    given(applicationProperties.getBaseUrl()).willReturn("https://sandbox.dissco.tech");

    // When
    var receivedResponse = controller.createAnnotation(authentication, request, mockRequest);

    // Then
    assertThat(receivedResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(receivedResponse.getBody()).isEqualTo(expectedResponse);
  }

  @Test
  void testPersistAnnotationMissingOrcid() {
    // given
    var principal = mock(Jwt.class);
    given(authentication.getPrincipal()).willReturn(principal);
    given(principal.getClaims()).willReturn(Map.of(
        "client-id", "demo-api-client"
    ));
    var request = givenJsonApiAnnotationRequest(givenAnnotationRequest());

    // When / Then
    assertThrowsExactly(ForbiddenException.class,
        () -> controller.createAnnotation(authentication, request, mockRequest));
  }

  @Test
  void testGetBatchConfirmation() throws Exception {
    // When
    var result = controller.getCountForBatchAnnotations(givenAnnotationCountRequest());

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testCreateAnnotationBatch() throws Exception {
    // Given
    givenAuthentication(givenClaims());
    var event = givenAnnotationEventRequest();
    var request = givenJsonApiAnnotationRequest(event);
    var expectedResponse = givenAnnotationResponseSingleDataNode(ANNOTATION_PATH);
    given(service.persistAnnotationBatch(event, givenAgent(), ANNOTATION_PATH))
        .willReturn(expectedResponse);
    given(applicationProperties.getBaseUrl()).willReturn("https://sandbox.dissco.tech");

    // When
    var receivedResponse = controller.createAnnotationBatch(authentication, request, mockRequest);

    // Then
    assertThat(receivedResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(receivedResponse.getBody()).isEqualTo(expectedResponse);
  }

  @Test
  void testCreateAnnotationNullResponse() throws Exception {
    // Given
    givenAuthentication(givenClaims());
    var annotation = givenAnnotationRequest();
    var request = givenJsonApiAnnotationRequest(annotation);
    given(
        service.persistAnnotation(any(AnnotationProcessingRequest.class), any(), any())).willReturn(
        null);

    // When
    var receivedResponse = controller.createAnnotation(authentication, request, mockRequest);

    // Then
    assertThat(receivedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testCreateAnnotationBatchNullResponse() throws Exception {
    // Given
    givenAuthentication(givenClaims());
    var event = givenAnnotationEventRequest();
    var request = givenJsonApiAnnotationRequest(event);
    given(service.persistAnnotationBatch(event, givenAgent(), ANNOTATION_PATH))
        .willReturn(null);
    given(applicationProperties.getBaseUrl()).willReturn("https://sandbox.dissco.tech");

    // When
    var receivedResponse = controller.createAnnotationBatch(authentication, request, mockRequest);

    // Then
    assertThat(receivedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testUpdateAnnotation() throws Exception {
    // Given
    givenAuthentication(givenClaims());
    var annotation = givenAnnotationRequest();
    var requestBody = givenJsonApiAnnotationRequest(annotation);
    var expected = givenAnnotationResponseSingleDataNode(ANNOTATION_PATH);
    given(service.updateAnnotation(ID, annotation, givenAgent(), ANNOTATION_PATH, PREFIX,
        SUFFIX)).willReturn(
        expected);
    given(applicationProperties.getBaseUrl()).willReturn("https://sandbox.dissco.tech");

    // When
    var result = controller.updateAnnotation(authentication, requestBody, PREFIX, SUFFIX,
        mockRequest);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isEqualTo(expected);
  }

  @Test
  void testGetAnnotationsForUserJsonResponse() throws Exception {
    // Given
    givenAuthentication(givenClaims());

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
    then(service).should().getAnnotationVersions(eq(HANDLE + ID), anyString());
  }

  @ParameterizedTest
  @MethodSource("nonAdminClaims")
  void testTombstoneAnnotationSuccessNonAdmin(Map<String, Object> claims) throws Exception {
    // Given
    givenAuthentication(claims);
    given(service.tombstoneAnnotation(PREFIX, SUFFIX, givenAgent(), false)).willReturn(true);

    // When
    var receivedResponse = controller.tombstoneAnnotation(authentication, PREFIX, SUFFIX);

    // Then
    assertThat(receivedResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
  }

  private static Stream<Arguments> nonAdminClaims(){
    return Stream.of(
        Arguments.of(givenClaims()),
        Arguments.of(Map.of(
            "orcid", ORCID,
            "given_name", "Sam",
            "family_name", "Leeflang",
            "realm_access", Map.of("roles", List.of("dissco-user")))),
        Arguments.of(Map.of(
            "orcid", ORCID,
            "given_name", "Sam",
            "family_name", "Leeflang",
            "realm_access", Map.of("roles", "dissco-admin"))));
  }

  @Test
  void testTombstoneAnnotationSuccessAdmin() throws Exception {
    // Given
    givenAuthentication(givenAdminClaims());
    given(service.tombstoneAnnotation(PREFIX, SUFFIX, givenAgent(), true)).willReturn(true);

    // When
    var receivedResponse = controller.tombstoneAnnotation(authentication, PREFIX, SUFFIX);

    // Then
    assertThat(receivedResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
  }

  @Test
  void testTombstoneAnnotationFailure() throws Exception {
    // Given
    givenAuthentication(givenClaims());
    given(service.tombstoneAnnotation(PREFIX, SUFFIX, givenAgent(), false)).willReturn(false);

    // When
    var receivedResponse = controller.tombstoneAnnotation(authentication, PREFIX, SUFFIX);

    // Then
    assertThat(receivedResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  private void givenAuthentication(Map<String, Object> claims) {
    var principal = mock(Jwt.class);
    given(authentication.getPrincipal()).willReturn(principal);
    given(principal.getClaims()).willReturn(claims);
  }
}
