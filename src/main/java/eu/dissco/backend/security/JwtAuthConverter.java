package eu.dissco.backend.security;

import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

  @Override
  public AbstractAuthenticationToken convert(@NotNull Jwt jwt) {
    return new JwtAuthenticationToken(jwt, extractRoles(jwt), getPrincipalClaimName(jwt));
  }

  private Set<GrantedAuthority> extractRoles(Jwt jwt) {
    Set<GrantedAuthority> authorities = new HashSet<>();
    if (jwt.getClaims().containsKey("resource_access")) {
      ((Map<String, Object>) jwt.getClaims().get("resource_access")).forEach((k, v) -> {
        Map<String, Object> resourceAccess = (Map<String, Object>) v;
        resourceAccess.forEach((k1, v1) -> {
          if (k1.equals("roles")) {
            ((Collection<String>) v1).forEach(
                role -> authorities.add((GrantedAuthority) () -> "ROLE_" + role));
          }
        });
      });
    }
    if (jwt.getClaims().containsKey("realm_access")) {
      ((Map<String, Object>) jwt.getClaims().get("realm_access")).forEach((k, v) -> {
        if (k.equals("roles")) {
          ((Collection<String>) v).forEach(
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
