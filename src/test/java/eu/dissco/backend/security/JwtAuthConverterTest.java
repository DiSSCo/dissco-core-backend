package eu.dissco.backend.security;

import static org.assertj.core.api.Assertions.assertThat;

import eu.dissco.backend.properties.SecurityProperties;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class JwtAuthConverterTest {

  private JwtAuthConverter converter;

  @BeforeEach
  void setup() {
    converter = new JwtAuthConverter(new SecurityProperties());
  }

  @Test
  void testEmptyToken() {
    // Given
    var jwt = new Jwt("SomeRandomStringWithTheFullValue", Instant.now(), Instant.now(),
        Map.of("kid", "SomeRandom", "typ", "JWT", "alg", "RS256"),
        Map.of("sub", "adf294ba-bb03-4962-8042-a37f1648458e"));

    // When
    var token = converter.convert(jwt);

    // Then
    assertThat(token.isAuthenticated()).isTrue();
    assertThat(token.getName()).isEqualTo("adf294ba-bb03-4962-8042-a37f1648458e");
    assertThat(token.getAuthorities()).isEmpty();
  }

  @Test
  void testTokenRoles() {
    // Given
    var jwt = new Jwt("SomeRandomStringWithTheFullValue", Instant.now(), Instant.now(),
        Map.of("kid", "SomeRandom", "typ", "JWT", "alg", "RS256"),
        Map.of("sub", "adf294ba-bb03-4962-8042-a37f1648458e",
            "realm_access", Map.of("roles", List.of("virtual-collection-admin")),
            "resource_access",
            Map.of("dissco-backend", Map.of("roles", List.of("dissco-web-batch-annotations")))));

    // When
    var token = converter.convert(jwt);

    // Then
    assertThat(token.isAuthenticated()).isTrue();
    assertThat(token.getName()).isEqualTo("adf294ba-bb03-4962-8042-a37f1648458e");
    assertThat(token.getAuthorities()).hasSize(2);
  }

  @Test
  void testTokenMissingPropertyRoles() {
    // Given
    var jwt = new Jwt("SomeRandomStringWithTheFullValue", Instant.now(), Instant.now(),
        Map.of("kid", "SomeRandom", "typ", "JWT", "alg", "RS256"),
        Map.of("sub", "adf294ba-bb03-4962-8042-a37f1648458e",
            "resource_access",
            Map.of("orchestration-service", Map.of("no-roles", List.of("I am not here")))));

    // When
    var token = converter.convert(jwt);

    // Then
    assertThat(token.isAuthenticated()).isTrue();
    assertThat(token.getName()).isEqualTo("adf294ba-bb03-4962-8042-a37f1648458e");
    assertThat(token.getAuthorities()).isEmpty();
  }

}
