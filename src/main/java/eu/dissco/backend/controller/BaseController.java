package eu.dissco.backend.controller;

import static eu.dissco.backend.schema.Identifier.OdsGupriLevel.GLOBALLY_UNIQUE_STABLE_PERSISTENT_RESOLVABLE;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.domain.MasJobRequest;
import eu.dissco.backend.domain.openapi.shared.MasSchedulingRequest;
import eu.dissco.backend.exceptions.ConflictException;
import eu.dissco.backend.exceptions.ForbiddenException;
import eu.dissco.backend.properties.ApplicationProperties;
import eu.dissco.backend.schema.Agent;
import eu.dissco.backend.schema.Agent.Type;
import eu.dissco.backend.schema.Identifier;
import eu.dissco.backend.schema.Identifier.DctermsType;
import eu.dissco.backend.schema.Identifier.OdsIdentifierStatus;
import eu.dissco.backend.schema.OdsHasRole;
import jakarta.servlet.http.HttpServletRequest;
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
  private static final String ORCID = "orcid";
  protected final ObjectMapper mapper;
  private final ApplicationProperties applicationProperties;

  // OpenAPI Messages
  protected static final String PAGE_NUM_OAS = "Desired page number";
  protected static final String PAGE_SIZE_OAS = "Desired page size";

  protected static final String PREFIX_OAS = "Prefix of target ID";
  protected static final String SUFFIX_OAS = "Suffix of target ID";

  protected static final String VERSION_OAS = "Desired version";
  protected static final String JOB_STATUS_OAS ="Optional filter on job status";


  protected static Agent getAgent(Authentication authentication) throws ForbiddenException {
    var claims = ((Jwt) authentication.getPrincipal()).getClaims();
    if (claims.containsKey(ORCID)) {
      StringBuilder fullName = new StringBuilder();
      if (claims.containsKey("given_name")) {
        fullName.append(claims.get("given_name"));
      }
      if (claims.containsKey("family_name")) {
        if (!fullName.isEmpty()) {
          fullName.append(" ");
        }
        fullName.append(claims.get("family_name"));
      }
      var id = (String) claims.get(ORCID);
      return new Agent()
          .withType(Type.SCHEMA_PERSON)
          .withId(id)
          .withSchemaIdentifier(id)
          .withSchemaName(fullName.toString())
          .withOdsHasRoles(List.of(
              new OdsHasRole()
                  .withType("schema:Role")
                  .withSchemaRoleName("annotator")))
          .withOdsHasIdentifiers(List.of(
              new Identifier()
                  .withId(id)
                  .withDctermsIdentifier(id)
                  .withType("ods:Identifier")
                  .withDctermsType(DctermsType.URL)
                  .withDctermsTitle(ORCID)
                  .withOdsIsPartOfLabel(false)
                  .withOdsIdentifierStatus(OdsIdentifierStatus.PREFERRED)
                  .withOdsGupriLevel(GLOBALLY_UNIQUE_STABLE_PERSISTENT_RESOLVABLE)));
    } else {
      log.error("Missing ORCID in token");
      throw new ForbiddenException("Missing ORCID in token");
    }
  }

  protected static boolean isAdmin(Authentication authentication) {
    try {
      var claims = ((Jwt) authentication.getPrincipal()).getClaims();
      var roles = ((Map<String, List<String>>) claims.get("realm_access")).get("roles");
      return (roles.contains("dissco-admin"));
    } catch (NullPointerException e) {
      return false;
    } catch (ClassCastException e) {
      log.warn("Unable to read claims", e);
      return false;
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

  protected Map<String, MasJobRequest> getMassRequestFromRequest(MasSchedulingRequest requestBody)
      throws ConflictException {
    if (!requestBody.data().type().equals("MasRequest")) {
      throw new ConflictException();
    }
    if (requestBody.data().attributes().mass() == null) {
      throw new IllegalArgumentException();
    }
    return requestBody.data().attributes().mass().stream()
        .collect(Collectors.toMap(MasJobRequest::masId, Function.identity()));
  }

}
