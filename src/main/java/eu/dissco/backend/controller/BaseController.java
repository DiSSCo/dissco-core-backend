package eu.dissco.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.domain.MasJobRequest;
import eu.dissco.backend.domain.jsonapi.JsonApiRequestWrapper;
import eu.dissco.backend.exceptions.ConflictException;
import eu.dissco.backend.properties.ApplicationProperties;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public abstract class BaseController {

  public static final String DATE_STRING = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
  protected static final String DEFAULT_PAGE_NUM = "1";
  protected static final String DEFAULT_PAGE_SIZE = "10";
  private static final TypeReference<List<MasJobRequest>> LIST_TYPE = new TypeReference<>() {
  };
  protected final ObjectMapper mapper;
  private final ApplicationProperties applicationProperties;

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
