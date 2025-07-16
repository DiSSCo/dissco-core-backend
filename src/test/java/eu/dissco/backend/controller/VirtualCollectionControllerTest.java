package eu.dissco.backend.controller;

import static eu.dissco.backend.TestUtils.HANDLE;
import static eu.dissco.backend.TestUtils.ID;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.PREFIX;
import static eu.dissco.backend.TestUtils.SUFFIX;
import static eu.dissco.backend.TestUtils.USER_ID_TOKEN;
import static eu.dissco.backend.TestUtils.givenAgent;
import static eu.dissco.backend.TestUtils.givenAuthentication;
import static eu.dissco.backend.TestUtils.givenClaims;
import static eu.dissco.backend.utils.VirtualCollectionUtils.VIRTUAL_COLLECTION_NAME;
import static eu.dissco.backend.utils.VirtualCollectionUtils.VIRTUAL_COLLECTION_PATH;
import static eu.dissco.backend.utils.VirtualCollectionUtils.givenTargetFilter;
import static eu.dissco.backend.utils.VirtualCollectionUtils.givenVirtualCollectionJsonResponse;
import static eu.dissco.backend.utils.VirtualCollectionUtils.givenVirtualCollectionRequest;
import static eu.dissco.backend.utils.VirtualCollectionUtils.givenVirtualCollectionResponseWrapper;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.dissco.backend.domain.FdoType;
import eu.dissco.backend.domain.openapi.virtual_collection.VirtualCollectionRequest;
import eu.dissco.backend.domain.openapi.virtual_collection.VirtualCollectionRequest.VirtualCollectionRequestData;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.properties.ApplicationProperties;
import eu.dissco.backend.schema.VirtualCollectionRequest.LtcBasisOfScheme;
import eu.dissco.backend.service.VirtualCollectionService;
import eu.dissco.backend.utils.VirtualCollectionUtils;
import java.io.IOException;
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

@ExtendWith(MockitoExtension.class)
class VirtualCollectionControllerTest {


  @Mock
  private VirtualCollectionService service;
  @Mock
  private Authentication authentication;
  @Mock
  private ApplicationProperties applicationProperties;
  private VirtualCollectionController controller;

  private MockHttpServletRequest mockRequest;

  @BeforeEach
  void setup() {
    mockRequest = new MockHttpServletRequest();
    mockRequest.setRequestURI(VirtualCollectionUtils.VIRTUAL_COLLECTION_URI);
    controller = new VirtualCollectionController(MAPPER, applicationProperties, service);
  }

  @Test
  void testGetVirtualCollectionById() throws NotFoundException {
    // Given
    var expectedResponse = givenVirtualCollectionResponseWrapper(VIRTUAL_COLLECTION_PATH);
    given(service.getVirtualCollectionById(ID, VIRTUAL_COLLECTION_PATH)).willReturn(
        expectedResponse);
    given(applicationProperties.getBaseUrl()).willReturn("https://sandbox.dissco.tech");

    // When
    var receivedResponse = controller.getVirtualCollectionById(PREFIX, SUFFIX, mockRequest);

    // Then
    assertThat(receivedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(receivedResponse.getBody()).isEqualTo(expectedResponse);
  }


  @Test
  void testGetVirtualCollectionVersion() throws NotFoundException, JsonProcessingException {
    // Given
    var version = 1;
    var expectedResponse = ResponseEntity.ok(
        givenVirtualCollectionResponseWrapper(VIRTUAL_COLLECTION_PATH));
    given(service.getVirtualCollectionByVersion(HANDLE + ID, version,
        VIRTUAL_COLLECTION_PATH)).willReturn(expectedResponse.getBody());
    given(applicationProperties.getBaseUrl()).willReturn("https://sandbox.dissco.tech");

    // When
    var receivedResponse = controller.getVirtualCollectionByVersion(PREFIX, SUFFIX, version,
        mockRequest);

    // Then
    assertThat(receivedResponse).isEqualTo(expectedResponse);
  }

  @Test
  void testGetVirtualCollection() {
    // Given
    int pageNumber = 1;
    int pageSize = 11;
    var expectedJson = givenVirtualCollectionJsonResponse(VIRTUAL_COLLECTION_PATH, pageNumber,
        pageSize,
        USER_ID_TOKEN, ID, true);
    var expectedResponse = ResponseEntity.ok(expectedJson);
    given(service.getVirtualCollection(pageNumber, pageSize, VIRTUAL_COLLECTION_PATH)).willReturn(
        expectedJson);
    given(applicationProperties.getBaseUrl()).willReturn("https://sandbox.dissco.tech");

    // When
    var receivedResponse = controller.getVirtualCollection(pageNumber, pageSize, mockRequest);

    // Then
    assertThat(receivedResponse).isEqualTo(expectedResponse);
  }

  @Test
  void testGetAnnotationsForUserJsonResponse() throws Exception {
    // Given
    givenAuthentication(authentication, givenClaims());

    // When
    var receivedResponse = controller.getVirtualCollectionForUser(1, 1, mockRequest,
        authentication);

    // Then
    assertThat(receivedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testGetAnnotationsByVersion() throws NotFoundException {
    // Given

    // When
    var receivedResponse = controller.getVirtualCollectionVersions(PREFIX, SUFFIX, mockRequest);

    // Then
    assertThat(receivedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    then(service).should().getVirtualCollectionVersions(eq(HANDLE + ID), anyString());
  }

  @Test
  void testCreateAnnotation() throws Exception {
    // Given
    givenAuthentication(authentication, givenClaims());
    var virtualCollection = givenVirtualCollectionRequest();

    var request = new VirtualCollectionRequest(
        new VirtualCollectionRequestData(FdoType.VIRTUAL_COLLECTION,
            givenVirtualCollectionRequest()));
    var expectedResponse = givenVirtualCollectionResponseWrapper(VIRTUAL_COLLECTION_PATH);
    given(
        service.persistVirtualCollection(virtualCollection, givenAgent(), VIRTUAL_COLLECTION_PATH))
        .willReturn(expectedResponse);
    given(applicationProperties.getBaseUrl()).willReturn("https://sandbox.dissco.tech");

    // When
    var receivedResponse = controller.createVirtualCollection(authentication, request, mockRequest);

    // Then
    assertThat(receivedResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(receivedResponse.getBody()).isEqualTo(expectedResponse);
  }

  @ParameterizedTest
  @MethodSource("sourceInvalidRequest")
  void testCreateAnnotationWithInvalidRequest(FdoType fdoType, eu.dissco.backend.schema.VirtualCollectionRequest virtualCollectionRequest) {
    // Given
    var request = new VirtualCollectionRequest(new VirtualCollectionRequestData(fdoType, virtualCollectionRequest));

    // When / Then
    assertThrows(IllegalArgumentException.class,
        () -> controller.createVirtualCollection(authentication, request, mockRequest));
  }

  static Stream<Arguments> sourceInvalidRequest() {
    return Stream.of(
        Arguments.of(FdoType.ANNOTATION, null),
        Arguments.of(FdoType.VIRTUAL_COLLECTION,
            givenVirtualCollectionRequest(VIRTUAL_COLLECTION_NAME,
                LtcBasisOfScheme.REFERENCE_COLLECTION, null)),
        Arguments.of(FdoType.VIRTUAL_COLLECTION, givenVirtualCollectionRequest("",
            LtcBasisOfScheme.REFERENCE_COLLECTION, givenTargetFilter())),
        Arguments.of(FdoType.VIRTUAL_COLLECTION, givenVirtualCollectionRequest(null,
            LtcBasisOfScheme.REFERENCE_COLLECTION, givenTargetFilter())),
        Arguments.of(FdoType.VIRTUAL_COLLECTION,
            givenVirtualCollectionRequest(VIRTUAL_COLLECTION_NAME,
                null, givenTargetFilter()))
    );
  }
}
