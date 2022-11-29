package eu.dissco.backend.service;

import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.USER_ID_TOKEN;
import static eu.dissco.backend.TestUtils.givenJsonApiData;
import static eu.dissco.backend.TestUtils.givenUser;
import static eu.dissco.backend.TestUtils.givenUserRequest;
import static eu.dissco.backend.TestUtils.givenUserRequestInvalidType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import eu.dissco.backend.exceptions.InvalidIdException;
import eu.dissco.backend.exceptions.InvalidTypeException;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.exceptions.UserExistsException;
import eu.dissco.backend.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepository repository;

  private UserService service;

  @BeforeEach
  void setup() {
    service = new UserService(MAPPER, repository);
  }

  @Test
  void testCreateNewUser() throws Exception {
    // Given
    given(repository.find(USER_ID_TOKEN)).willReturn(Optional.empty());
    given(repository.createNewUser(USER_ID_TOKEN, givenUser())).willReturn(givenUser());

    // When
    var result = service.createNewUser(givenUserRequest());

    // Then
    assertThat(result).isEqualTo(givenJsonApiData());
  }

  @Test
  void testCreateAlreadyExists() {
    // Given
    given(repository.find(USER_ID_TOKEN)).willReturn(Optional.of(givenUser()));

    // When
    var exception = assertThrowsExactly(UserExistsException.class,
        () -> service.createNewUser(givenUserRequest()));

    // Then
    then(repository).shouldHaveNoMoreInteractions();
    assertThat(exception).isInstanceOf(UserExistsException.class);
  }

  @Test
  void testCreateInvalidType() {
    // Given

    // When
    var exception = assertThrowsExactly(InvalidTypeException.class,
        () -> service.createNewUser(givenUserRequestInvalidType()));

    // Then
    then(repository).shouldHaveNoInteractions();
    assertThat(exception).isInstanceOf(InvalidTypeException.class);
  }

  @Test
  void testFindUser() throws NotFoundException {
    // Given
    given(repository.find(USER_ID_TOKEN)).willReturn(Optional.of(givenUser()));

    // When
    var result = service.findUser(USER_ID_TOKEN);

    // Then
    assertThat(result).isEqualTo(givenJsonApiData());
  }

  @Test
  void testUserNotFound() {
    // Given
    given(repository.find(USER_ID_TOKEN)).willReturn(Optional.empty());

    // When
    var exception = assertThrowsExactly(NotFoundException.class,
        () -> service.findUser(USER_ID_TOKEN));

    // Then
    assertThat(exception).isInstanceOf(NotFoundException.class);
  }

  @Test
  void testUpdateUser() throws Exception {
    // Given
    given(repository.find(USER_ID_TOKEN)).willReturn(Optional.of(givenUser()));
    given(repository.updateUser(USER_ID_TOKEN, MAPPER.valueToTree(givenUser()))).willReturn(
        givenUser());

    // When
    var result = service.updateUser(USER_ID_TOKEN, givenUserRequest());

    // Then
    assertThat(result).isEqualTo(givenJsonApiData());
  }

  @Test
  void testUpdateUserInvalidId() {
    // Given

    // When
    var exception = assertThrowsExactly(InvalidIdException.class,
        () -> service.updateUser(USER_ID_TOKEN, givenUserRequest("Another user Id")));

    // Then
    assertThat(exception).isInstanceOf(InvalidIdException.class);
    then(repository).shouldHaveNoInteractions();
  }

  @Test
  void testUpdateUserNoUser() {
    // Given
    given(repository.find(USER_ID_TOKEN)).willReturn(Optional.empty());

    // When
    var exception = assertThrowsExactly(NotFoundException.class,
        () -> service.updateUser(USER_ID_TOKEN, givenUserRequest()));

    // Then
    assertThat(exception).isInstanceOf(NotFoundException.class);
    then(repository).shouldHaveNoMoreInteractions();
  }

  @Test
  void testDeleteUser() throws NotFoundException {
    // Given
    given(repository.find(USER_ID_TOKEN)).willReturn(Optional.of(givenUser()));

    // When
    service.deleteUser(USER_ID_TOKEN);

    // Then
    then(repository).should().deleteUser(USER_ID_TOKEN);
  }

  @Test
  void testDeleteUserNoUser() {
    // Given
    given(repository.find(USER_ID_TOKEN)).willReturn(Optional.empty());

    // When
    var exception = assertThrowsExactly(NotFoundException.class,
        () -> service.deleteUser(USER_ID_TOKEN));

    // Then
    assertThat(exception).isInstanceOf(NotFoundException.class);
    then(repository).shouldHaveNoMoreInteractions();
  }

}
