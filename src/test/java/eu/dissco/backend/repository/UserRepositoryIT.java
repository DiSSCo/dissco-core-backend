package eu.dissco.backend.repository;

import static eu.dissco.backend.TestUtils.MAPPER;
import static eu.dissco.backend.TestUtils.USER_ID_TOKEN;
import static eu.dissco.backend.TestUtils.givenUser;
import static eu.dissco.backend.database.jooq.Tables.NEW_USER;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserRepositoryIT extends BaseRepositoryIT {

  private UserRepository repository;

  @BeforeEach
  void setup() {
    repository = new UserRepository(context);
  }

  @AfterEach
  void destroy() {
    context.truncate(NEW_USER).execute();
  }

  @Test
  void testCreateNewUser() {
    // Given

    // When
    var result = repository.createNewUser(USER_ID_TOKEN, givenUser());

    // Then
    assertThat(result).isEqualTo(givenUser());
  }

  @Test
  void testFindUser() {
    // Given
    repository.createNewUser(USER_ID_TOKEN, givenUser());

    // When
    var result = repository.find(USER_ID_TOKEN);

    // Then
    assertThat(result).isPresent().contains(givenUser());
  }

  @Test
  void testDeleteUser() {
    // Given
    repository.createNewUser(USER_ID_TOKEN, givenUser());

    // When
    repository.deleteUser(USER_ID_TOKEN);

    // Then
    var result = repository.find(USER_ID_TOKEN);
    assertThat(result).isEmpty();
  }

  @Test
  void testUpdateUser() {
    // Given
    repository.createNewUser(USER_ID_TOKEN, givenUser());
    var updatedAttributes = givenAttributes();

    // When
    var result = repository.updateUser(USER_ID_TOKEN, updatedAttributes);

    // Then
    assertThat(result.lastName()).isEqualTo("updatedLastName");
    assertThat(result.firstName()).isEqualTo("Test");
    assertThat(result.orcid()).isEqualTo("updatedOrcid");
  }

  private JsonNode givenAttributes() {
    return MAPPER.createObjectNode()
        .put("lastName", "updatedLastName")
        .put("orcid", "updatedOrcid")
        .put("randomKey", "randomValue");
  }

}
