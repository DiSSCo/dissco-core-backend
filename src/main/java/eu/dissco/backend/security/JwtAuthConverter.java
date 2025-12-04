package eu.dissco.backend.security;

import eu.dissco.backend.properties.SecurityProperties;
import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

  private final SecurityProperties securityProperties;

  @Override
  public AbstractAuthenticationToken convert(@NotNull Jwt jwt) {
    return new JwtAuthenticationToken(jwt, extractRoles(jwt), getPrincipalClaimName(jwt));
  }

  private Set<GrantedAuthority> extractRoles(Jwt jwt) {
    Set<GrantedAuthority> authorities = new HashSet<>();
    if (jwt.getClaims().containsKey("resource_access")) {
      ((Map<String, Object>) jwt.getClaims().get("resource_access")).forEach(
          (clientName, properties) -> {
            if (clientName.equals(securityProperties.getClientId())) {
              Map<String, Object> resourceAccess = (Map<String, Object>) properties;
              resourceAccess.forEach((propertyName, value) -> {
                if (propertyName.equals("roles")) {
                  ((Collection<String>) value).forEach(
                      role -> authorities.add((GrantedAuthority) () -> "ROLE_" + role));
                }
              });
            }
          });
    }
    if (jwt.getClaims().containsKey("realm_access")) {
      ((Map<String, Object>) jwt.getClaims().get("realm_access")).forEach((propertyName, value) -> {
        if (propertyName.equals("roles")) {
          ((Collection<String>) value).forEach(
              role -> authorities.add((GrantedAuthority) () -> "ROLE_" + role));
        }
      });
    }
    return authorities;
  }

  private String getPrincipalClaimName(Jwt jwt) {
    return jwt.getClaim(JwtClaimNames.SUB);
  }
}
