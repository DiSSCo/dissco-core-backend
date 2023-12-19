package eu.dissco.backend.controller;

import static eu.dissco.backend.TestUtils.FORBIDDEN_MESSAGE;
import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.ORCID;
import static eu.dissco.backend.TestUtils.USER_ID_TOKEN;
import static eu.dissco.backend.TestUtils.givenJsonApiData;
import static eu.dissco.backend.TestUtils.givenUserRequest;
import static eu.dissco.backend.TestUtils.givenUserResponse;
import static eu.dissco.backend.utils.MasJobRecordUtils.givenMjrListResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.BDDMockito.given;

import eu.dissco.backend.domain.MasJobState;
import eu.dissco.backend.exceptions.ForbiddenException;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.properties.ApplicationProperties;
import eu.dissco.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

  private final String USER_URL = "/api/v1/users/";
  MockHttpServletRequest mockRequest;
  @Mock
  private ApplicationProperties applicationProperties;
  @Mock
  private UserService service;
  @Mock
  private Authentication authentication;
  private UserController controller;

  @BeforeEach
  void setup() {
    controller = new UserController(applicationProperties, MAPPER, service);
    mockRequest = new MockHttpServletRequest();
  }

  @Test
  void testCreateNewUser() throws Exception {
    // Given
    givenAuthentication(USER_ID_TOKEN);
    given(service.createNewUser(givenUserRequest())).willReturn(givenJsonApiData());
    given(applicationProperties.getBaseUrl()).willReturn("https://sandbox.dissco.tech");
    mockRequest.setRequestURI(USER_URL + USER_ID_TOKEN);

    // When
    var result = controller.createNewUser(authentication, givenUserRequest(), mockRequest);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(result.getBody()).isEqualTo(givenUserResponse());
  }

  @Test
  void testForbidden() {
    // Given
    givenAuthentication(USER_ID_TOKEN);

    // When
    var exception = assertThrowsExactly(ForbiddenException.class,
        () -> controller.createNewUser(authentication,
            givenUserRequest("f3cdgcb7-9324-4bb4-9f41-d7dfae4a44b1"), mockRequest));

    // Then
    assertThat(exception).hasMessage(FORBIDDEN_MESSAGE);
  }

  @Test
  void testGetUser() throws NotFoundException {
    // Given
    givenAuthentication(USER_ID_TOKEN);
    given(service.findUser(USER_ID_TOKEN)).willReturn(givenJsonApiData());
    given(applicationProperties.getBaseUrl()).willReturn("https://sandbox.dissco.tech");
    mockRequest.setRequestURI(USER_URL + USER_ID_TOKEN);

    // When
    var result = controller.getUser(authentication, USER_ID_TOKEN, mockRequest);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isEqualTo(givenUserResponse());
  }

  @Test
  void testGetMasJobRecords() {
    // Given
    int pageNum = 1;
    int pageSize = 1;
    givenAuthentication(USER_ID_TOKEN);
    var expected = givenMjrListResponse(pageNum, pageSize, true);
    var path = "https://sandbox.dissco.tech" + USER_URL;
    given(applicationProperties.getBaseUrl()).willReturn("https://sandbox.dissco.tech");
    mockRequest.setRequestURI(USER_URL);
    given(
        service.getMasJobRecordsForUser(USER_ID_TOKEN, path, pageNum, pageSize,
            MasJobState.SCHEDULED)).willReturn(expected);

    // When
    var result = controller.getMasJobRecords(pageNum, pageSize, MasJobState.SCHEDULED, authentication, mockRequest);

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isEqualTo(expected);
  }

  @Test
  void testGetUserFromOrcid() throws NotFoundException {
    // Given
    givenAuthentication(USER_ID_TOKEN);
    given(service.findUserFromOrcid(ORCID)).willReturn(givenJsonApiData());
    given(applicationProperties.getBaseUrl()).willReturn("https://sandbox.dissco.tech");
    mockRequest.setRequestURI(USER_URL + USER_ID_TOKEN);

    // When
    var result = controller.getUserFromOrcid(authentication, ORCID, mockRequest);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isEqualTo(givenUserResponse());
  }

  @Test
  void testUpdateUser() throws Exception {
    // Given
    givenAuthentication(USER_ID_TOKEN);
    given(service.updateUser(USER_ID_TOKEN, givenUserRequest())).willReturn(givenJsonApiData());
    given(applicationProperties.getBaseUrl()).willReturn("https://sandbox.dissco.tech");
    mockRequest.setRequestURI(USER_URL + USER_ID_TOKEN);

    // When
    var result = controller.updateUser(authentication, USER_ID_TOKEN, givenUserRequest(),
        mockRequest);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isEqualTo(givenUserResponse());
  }

  @Test
  void testDeleteUser() throws Exception {
    // Given
    givenAuthentication(USER_ID_TOKEN);

    // When
    var result = controller.deleteUser(authentication, USER_ID_TOKEN);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
  }

  private void givenAuthentication(String userId) {
    given(authentication.getName()).willReturn(userId);
  }

}
