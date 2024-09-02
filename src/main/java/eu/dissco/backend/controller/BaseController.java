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
    var tokenEncoded = ((Jwt) authentication.getPrincipal()).getTokenValue();
    var tokenBody = tokenEncoded.split("\\.")[1];
    var tokenString = Base64.getUrlDecoder().decode(tokenBody);
    JsonNode token;
    try {
      token = mapper.readTree(tokenString);
    } catch (IOException e) {
      log.error("Unable to read authentication token");
      throw new ForbiddenException("Unable to read authentication token");
    }
    if (token.get("orcid") != null) {
      StringBuilder fullName = new StringBuilder();
      if (token.get("given_name") != null){
        fullName.append(token.get("given_name").asText())
            .append(" ");
      }
      if (token.get("family_name") != null){
        fullName.append(token.get("family_name").asText());
      }
      return new User(fullName.toString(), token.get("orcid").asText());
    } else {
      log.error("Missing ORCID from token");
      throw new ForbiddenException("Missing ORCID from token");
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
