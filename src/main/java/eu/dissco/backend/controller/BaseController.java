package eu.dissco.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.domain.MasJobRequest;
import eu.dissco.backend.domain.User;
import eu.dissco.backend.domain.jsonapi.JsonApiRequestWrapper;
import eu.dissco.backend.exceptions.ConflictException;
import eu.dissco.backend.exceptions.ForbiddenException;
import eu.dissco.backend.properties.ApplicationProperties;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public abstract class BaseController {

  public static final String DATE_STRING = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
  protected static final String DEFAULT_PAGE_NUM = "1";
  protected static final String DEFAULT_PAGE_SIZE = "10";
  private static final TypeReference<List<MasJobRequest>> LIST_TYPE = new TypeReference<>() {
  };
  protected final ObjectMapper mapper;
  private final ApplicationProperties applicationProperties;

  protected User getUser(Authentication authentication) throws ForbiddenException {
    var claims = ((Jwt) authentication.getPrincipal()).getClaims();

    if (claims.containsKey("orcid")) {
      StringBuilder fullName = new StringBuilder();
      if (claims.containsKey("given_name")){
        fullName.append(claims.get("given_name"));
      }
      if (claims.containsKey("family_name")){
        if (!fullName.isEmpty()) {
          fullName.append(" ");
        }
        fullName.append(claims.get("family_name"));
      }
      return new User(fullName.toString(), (String) claims.get("orcid"));
    } else {
      log.error("Missing ORCID in token");
      throw new ForbiddenException("Missing ORCID in token");
    }
  }


  protected String getPath(HttpServletRequest request) {
    var path = applicationProperties.getBaseUrl() + request.getRequestURI();
    var queryString = request.getQueryString();
    if (queryString != null) {
      return path + "?" + request.getQueryString();
    }
    return path;
  }

  protected Map<String, MasJobRequest> getMassRequestFromRequest(JsonApiRequestWrapper requestBody)
      throws ConflictException {
    if (!requestBody.data().type().equals("MasRequest")) {
      throw new ConflictException();
    }
    if (requestBody.data().attributes().get("mass") == null) {
      throw new IllegalArgumentException();
    }
    var list = mapper.convertValue(requestBody.data().attributes().get("mass"), LIST_TYPE);
    return list.stream().collect(Collectors.toMap(MasJobRequest::masId, Function.identity()));
  }

}
