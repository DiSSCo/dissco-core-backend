package eu.dissco.backend.controller;

import static eu.dissco.backend.TestUtils.FORBIDDEN_MESSAGE;
import static eu.dissco.backend.TestUtils.USER_ID_TOKEN;
import static eu.dissco.backend.TestUtils.givenJsonApiData;
import static eu.dissco.backend.TestUtils.givenUserRequest;
import static eu.dissco.backend.TestUtils.givenUserResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.BDDMockito.given;

import eu.dissco.backend.exceptions.ConflictException;
import eu.dissco.backend.exceptions.ForbiddenException;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

  @Mock
  private UserService service;
  @Mock
  private KeycloakPrincipal<KeycloakSecurityContext> principal;
  @Mock
  private KeycloakSecurityContext securityContext;
  @Mock
  private AccessToken accessToken;

  private Authentication authentication;
  private UserController controller;

  @BeforeEach
  void setup() {
    controller = new UserController(service);
  }

  @Test
  void testCreateNewUser() throws Exception {
    // Given
    givenAuthentication(USER_ID_TOKEN);
    given(service.createNewUser(givenUserRequest())).willReturn(givenJsonApiData());

    // When
    var result = controller.createNewUser(authentication, givenUserRequest());

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
            givenUserRequest("f3cdgcb7-9324-4bb4-9f41-d7dfae4a44b1")));

    // Then
    assertThat(exception).hasMessage(FORBIDDEN_MESSAGE);
  }

  @Test
  void testGetUser() throws NotFoundException {
    // Given
    givenAuthentication(USER_ID_TOKEN);
    given(service.findUser(USER_ID_TOKEN)).willReturn(givenJsonApiData());

    // When
    var result = controller.getUser(authentication, USER_ID_TOKEN);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isEqualTo(givenUserResponse());
  }

  @Test
  void testUpdateUser() throws Exception {
    // Given
    givenAuthentication(USER_ID_TOKEN);
    given(service.updateUser(USER_ID_TOKEN, givenUserRequest())).willReturn(givenJsonApiData());

    // When
    var result = controller.updateUser(authentication, USER_ID_TOKEN, givenUserRequest());

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

  @Test
  void testNotFoundException() {
    // Given

    // When
    var result = controller.handleException(new NotFoundException());

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void testForbiddenException() {
    // Given

    // When
    var result = controller.handleException(new ForbiddenException(FORBIDDEN_MESSAGE));

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void testConflictException() {
    // Given

    // When
    var result = controller.handleException(new ConflictException());

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
  }

  private void givenAuthentication(String userId) {
    authentication = new TestingAuthenticationToken(principal, null);
    given(principal.getKeycloakSecurityContext()).willReturn(securityContext);
    given(securityContext.getToken()).willReturn(accessToken);
    given(accessToken.getSubject()).willReturn(userId);
  }

}
