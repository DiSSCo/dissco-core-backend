package eu.dissco.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.domain.AnnotationRequest;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiRequestWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import eu.dissco.backend.domain.jsonapi.ObjectType;
import eu.dissco.backend.exceptions.NoAnnotationFoundException;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.service.AnnotationService;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@RestController
@RequestMapping("/api/v1/annotations")
@RequiredArgsConstructor
public class AnnotationController {

  private final AnnotationService service;
  private static final String SANDBOX_URI = "https://sandbox.dissco.tech/";
  private static final String DEFAULT_PAGE_NUM = "1";
  private static final String DEFAULT_PAGE_SIZE = "10";
  private final ObjectMapper mapper;

  @GetMapping(value = "/{prefix}/{postfix}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> getAnnotation(@PathVariable("prefix") String prefix,
      @PathVariable("postfix") String postfix, HttpServletRequest request) {
    String path = SANDBOX_URI + request.getRequestURI();
    var id = prefix + '/' + postfix;
    log.info("Received get request for annotation: {}", id);
    var annotation = service.getAnnotation(id, path);
    return ResponseEntity.ok(annotation);
  }

  @GetMapping(value = "/latest", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiListResponseWrapper> getLatestAnnotations(
      @RequestParam(defaultValue = DEFAULT_PAGE_NUM) int pageNumber,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int pageSize, HttpServletRequest request)
      throws IOException {
    log.info("Received get request for latest paginated annotations. Page number: {}, page size {}",
        pageNumber, pageSize);
    String path = SANDBOX_URI + request.getRequestURI();
    var annotations = service.getLatestAnnotations(pageNumber, pageSize, path);
    return ResponseEntity.ok(annotations);
  }

  @GetMapping(value = "/{prefix}/{postfix}/{version}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> getAnnotationByVersion(@PathVariable("prefix") String prefix,
      @PathVariable("postfix") String postfix, @PathVariable("version") int version, HttpServletRequest request)
      throws JsonProcessingException, NotFoundException {
    var id = prefix + '/' + postfix;
    String path = SANDBOX_URI + request.getRequestURI();
    log.info("Received get request for annotation: {} with version: {}", id, version);
    var annotation = service.getAnnotationByVersion(id, version, path);
    return ResponseEntity.ok(annotation);
  }


  @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiListResponseWrapper> getAnnotations(
      @RequestParam(defaultValue = DEFAULT_PAGE_NUM) int pageNumber,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int pageSize, HttpServletRequest request) {
    log.info("Received get request for json paginated annotations. Page number: {}, page size {}",
        pageNumber, pageSize);
    String path = SANDBOX_URI + request.getRequestURI();
    var annotations = service.getAnnotations(pageNumber, pageSize, path);
    return ResponseEntity.ok(annotations);
  }

  @PreAuthorize("isAuthenticated()")
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> createAnnotation(Authentication authentication,
      @RequestBody JsonApiRequestWrapper requestBody, HttpServletRequest request)
      throws JsonProcessingException {
    var annotation = getAnnotationFromRequest(requestBody);
    var userId = getNameFromToken(authentication);
    log.info("Received new annotation from user: {}", userId);
    String path = SANDBOX_URI + request.getRequestURI();
    var annotationResponse = service.persistAnnotation(annotation, userId, path);
    if (annotationResponse != null) {
      return ResponseEntity.status(HttpStatus.CREATED).body(annotationResponse);
    } else {
      return ResponseEntity.status(HttpStatus.OK).build();
    }
  }

  @PreAuthorize("isAuthenticated()")
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping(value = "/{prefix}/{postfix}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> updateAnnotation(Authentication authentication,
      @RequestBody JsonApiRequestWrapper requestBody, @PathVariable("prefix") String prefix,
      @PathVariable("postfix") String postfix, HttpServletRequest request) throws NoAnnotationFoundException, JsonProcessingException {
    var path = SANDBOX_URI + request.getRequestURI();
    var id = prefix + '/' + postfix;
    var userId = getNameFromToken(authentication);
    var annotation = getAnnotationFromRequest(requestBody);

    log.info("Received update for annotation: {} from user: {}", id, userId);
    var annotationResponse = service.updateAnnotation(id, annotation, userId, path);
    if (annotationResponse != null) {
      return ResponseEntity.status(HttpStatus.OK).body(annotationResponse);
    } else {
      return ResponseEntity.status(HttpStatus.OK).build();
    }
  }

  @PreAuthorize("isAuthenticated()")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/creator", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiListResponseWrapper> getAnnotationsForUser(
      @RequestParam(defaultValue = DEFAULT_PAGE_NUM) int pageNumber,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int pageSize, HttpServletRequest request,
      Authentication authentication) {
    var userId = getNameFromToken(authentication);
    log.info("Received get request to show all annotations for user: {}", userId);
    String path = SANDBOX_URI + request.getRequestURI();
    var annotations = service.getAnnotationsForUser(userId, pageNumber, pageSize, path);
    return ResponseEntity.ok(annotations);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{prefix}/{postfix}/versions", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> getAnnotationVersions(@PathVariable("prefix") String prefix,
      @PathVariable("postfix") String postfix, HttpServletRequest request) throws NotFoundException {
    var id = prefix + '/' + postfix;
    var path = SANDBOX_URI + request.getRequestURI();
    log.info("Received get request for versions of annotation with id: {}", id);
    var versions = service.getAnnotationVersions(id, path);
    return ResponseEntity.ok(versions);
  }

  @PreAuthorize("isAuthenticated()")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping(value = "/{prefix}/{postfix}")
  public ResponseEntity<Void> deleteAnnotation(Authentication authentication,
      @PathVariable("prefix") String prefix, @PathVariable("postfix") String postfix)
      throws NoAnnotationFoundException {
    var userId = getNameFromToken(authentication);
    log.info("Received delete for annotation: {} from user: {}", (prefix + postfix), userId);
    var success = service.deleteAnnotation(prefix, postfix, getNameFromToken(authentication));
    if (success) {
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    } else {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  private AnnotationRequest getAnnotationFromRequest(JsonApiRequestWrapper requestBody) throws JsonProcessingException {
    if (!requestBody.data().type().equals(ObjectType.ANNOTATION)){
      throw new IllegalArgumentException();
    }
    return mapper.treeToValue(requestBody.data().attributes(), AnnotationRequest.class);
  }

  @ExceptionHandler(NoAnnotationFoundException.class)
  public ResponseEntity<String> handleException(NoAnnotationFoundException e) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(e.getMessage());
  }

  private String getNameFromToken(Authentication authentication) {
    KeycloakPrincipal<? extends KeycloakSecurityContext> principal = (KeycloakPrincipal<?>) authentication.getPrincipal();
    AccessToken token = principal.getKeycloakSecurityContext().getToken();
    return token.getSubject();
  }
}