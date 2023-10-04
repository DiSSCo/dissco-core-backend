package eu.dissco.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.domain.jsonapi.JsonApiRequestWrapper;
import eu.dissco.backend.exceptions.ConflictException;
import eu.dissco.backend.properties.ApplicationProperties;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public abstract class BaseController {

  private final ApplicationProperties applicationProperties;
  protected final ObjectMapper mapper;

  protected static final String DEFAULT_PAGE_NUM = "1";
  protected static final String DEFAULT_PAGE_SIZE = "10";
  public static final String DATE_STRING = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

  protected String getPath(HttpServletRequest request) {
    var path = applicationProperties.getBaseUrl() + request.getRequestURI();
    var queryString = request.getQueryString();
    if (queryString != null) {
      return path + "?" + request.getQueryString();
    }
    return path;
  }

  protected List<String> getMassFromRequest(JsonApiRequestWrapper requestBody)
      throws JsonProcessingException, ConflictException {
    if (!requestBody.data().type().equals("MasRequest")) {
      throw new ConflictException();
    }
    if (requestBody.data().attributes().get("mass") == null) {
      throw new IllegalArgumentException();
    }
    return Arrays.asList(mapper.treeToValue(requestBody.data().attributes().get("mass"), String[].class));
  }
}
