package eu.dissco.backend.controller;

import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.PREFIX;
import static eu.dissco.backend.TestUtils.SUFFIX;
import static eu.dissco.backend.TestUtils.USER_ID_TOKEN;
import static eu.dissco.backend.utils.AnnotationUtils.givenAnnotationJsonResponseNoPagination;
import static eu.dissco.backend.utils.DigitalMediaObjectUtils.DIGITAL_MEDIA_PATH;
import static eu.dissco.backend.utils.DigitalMediaObjectUtils.DIGITAL_MEDIA_URI;
import static eu.dissco.backend.utils.DigitalMediaObjectUtils.givenDigitalMediaJsonResponse;
import static eu.dissco.backend.utils.MachineAnnotationServiceUtils.givenMasJobRequest;
import static eu.dissco.backend.utils.MachineAnnotationServiceUtils.givenMasRequest;
import static eu.dissco.backend.utils.MachineAnnotationServiceUtils.givenMasResponse;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.BDDMockito.given;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.dissco.backend.database.jooq.enums.JobState;
import eu.dissco.backend.exceptions.ConflictException;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.properties.ApplicationProperties;
import eu.dissco.backend.service.DigitalMediaService;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class DigitalMediaControllerTest {

  @Mock
  private DigitalMediaService service;
  @Mock
  private ApplicationProperties applicationProperties;
  @Mock
  private Authentication authentication;
  private DigitalMediaController controller;
  private MockHttpServletRequest mockRequest;

  @BeforeEach
  void setup() {
    controller = new DigitalMediaController(applicationProperties, MAPPER, service);
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
    given(applicationProperties.getBaseUrl()).willReturn("https://sandbox.dissco.tech");

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
    given(applicationProperties.getBaseUrl()).willReturn("https://sandbox.dissco.tech");

    // When
    var responseReceived = controller.getDigitalMediaObjectById(PREFIX, SUFFIX, mockRequest);

    // Then
    assertThat(responseReceived.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testGetDigitalMediaVersions() throws NotFoundException {
    // When
    var responseReceived = controller.getDigitalMediaVersions(PREFIX, SUFFIX, mockRequest);

    // Then
    assertThat(responseReceived.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testGetDigitalMediaObjectVersion() throws NotFoundException, JsonProcessingException {
    // Given
    int version = 1;
    given(service.getDigitalMediaObjectByVersion(ID, version, DIGITAL_MEDIA_PATH)).willReturn(
        givenDigitalMediaJsonResponse(DIGITAL_MEDIA_PATH, ID));
    given(applicationProperties.getBaseUrl()).willReturn("https://sandbox.dissco.tech");

    // When
    var responseReceived = controller.getDigitalMediaObjectByVersion(PREFIX, SUFFIX, version,
        mockRequest);

    // Then
    assertThat(responseReceived.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testGetAnnotationsById() {
    // Given
    var responseExpected = givenAnnotationJsonResponseNoPagination(USER_ID_TOKEN,
        List.of("1", "2"));
    given(service.getAnnotationsOnDigitalMedia(ID, DIGITAL_MEDIA_PATH)).willReturn(
        responseExpected);
    given(applicationProperties.getBaseUrl()).willReturn("https://sandbox.dissco.tech");

    // When
    var responseReceived = controller.getMediaAnnotationsById(PREFIX, SUFFIX, mockRequest);

    // Then
    assertThat(responseReceived.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(responseReceived.getBody()).isEqualTo(responseExpected);
  }

  @Test
  void testGetMasJobRecordForMedia() throws Exception {
    // When
    var result = controller.getMasJobRecordForMedia(PREFIX, SUFFIX, JobState.SCHEDULED, 1, 1,
        mockRequest);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testGetMas() {
    // Given
    var expectedResponse = givenMasResponse(DIGITAL_MEDIA_PATH);
    given(service.getMass(ID, DIGITAL_MEDIA_PATH)).willReturn(expectedResponse);
    given(applicationProperties.getBaseUrl()).willReturn("https://sandbox.dissco.tech");

    // When
    var result = controller.getMassForDigitalMediaObject(PREFIX, SUFFIX, mockRequest);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isEqualTo(expectedResponse);
  }

  @Test
  void testScheduleMas() throws Exception {
    // Given
    var expectedResponse = givenMasResponse(DIGITAL_MEDIA_PATH);
    var request = givenMasRequest();
    givenAuthentication();
    given(service.scheduleMass(ID, Map.of(ID, givenMasJobRequest()), DIGITAL_MEDIA_PATH,
        USER_ID_TOKEN)).willReturn(expectedResponse);
    given(applicationProperties.getBaseUrl()).willReturn("https://sandbox.dissco.tech");

    // When
    var result = controller.scheduleMassForDigitalMediaObject(PREFIX, SUFFIX, request,
        authentication, mockRequest);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    assertThat(result.getBody()).isEqualTo(expectedResponse);
  }

  @Test
  void testScheduleMasInvalidType() {
    // Given
    var request = givenMasRequest("Invalid Type");
    givenAuthentication();

    // When / Then
    assertThrowsExactly(ConflictException.class,
        () -> controller.scheduleMassForDigitalMediaObject(PREFIX, SUFFIX, request, authentication,
            mockRequest));
  }

  private void givenAuthentication() {
    given(authentication.getName()).willReturn(USER_ID_TOKEN);
  }

}
