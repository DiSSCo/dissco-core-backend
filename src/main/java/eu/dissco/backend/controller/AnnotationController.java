package eu.dissco.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.dissco.backend.domain.AnnotationRequest;
import eu.dissco.backend.domain.AnnotationResponse;
import eu.dissco.backend.domain.JsonApiMetaWrapper;
import eu.dissco.backend.domain.JsonApiWrapper;
import eu.dissco.backend.exceptions.NoAnnotationFoundException;
import eu.dissco.backend.service.AnnotationService;
import java.io.IOException;
import java.util.List;
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
import org.springframework.util.AntPathMatcher;
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

  @GetMapping(value = "/{prefix}/{postfix}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<AnnotationResponse> getAnnotation(
      @PathVariable("prefix") String prefix,
      @PathVariable("postfix") String postfix) {
    var id = prefix + '/' + postfix;
    log.info("Received get request for annotation: {}", id);
    var annotation = service.getAnnotation(id);
    return ResponseEntity.ok(annotation);
  }

  @GetMapping(value = "/latest", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<AnnotationResponse>> getLatestAnnotations(
      @RequestParam(defaultValue = "0") int pageNumber,
      @RequestParam(defaultValue = "10") int pageSize) throws IOException {

    log.info("Received get request for latest paginated annotations. Page number: {}, page size {}", pageNumber, pageSize);

    var annotations = service.getLatestAnnotations(pageNumber, pageSize);
    return ResponseEntity.ok(annotations);
  }

  @GetMapping(value = "/latest/json", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiMetaWrapper> getLatestAnnotationsJsonResponse(
      @RequestParam(defaultValue = "0") int pageNumber,
      @RequestParam(defaultValue = "10") int pageSize) throws IOException {
    log.info("Received get request for latest paginated annotations. Page number: {}, page size {}", pageNumber, pageSize);
    String path = "sandbox.dissco.tech/api/v1/annotations/latest/json";
    var annotations = service.getLatestAnnotationsJsonResponse(pageNumber, pageSize, path);
    return ResponseEntity.ok(annotations);
  }

  @GetMapping(value = "/{prefix}/{postfix}/{version}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<AnnotationResponse> getAnnotation(
      @PathVariable("prefix") String prefix,
      @PathVariable("postfix") String postfix,
      @PathVariable("version") int version) {
    var id = prefix + '/' + postfix;
    log.info("Received get request for annotation: {} with version: {}", id, version);
    var annotation = service.getAnnotationVersion(id, version);
    return ResponseEntity.ok(annotation);
  }

  @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<AnnotationResponse>> getAnnotations(
      @RequestParam(defaultValue = "0") int pageNumber,
      @RequestParam(defaultValue = "10") int pageSize) {
    log.info("Received get request for paginated annotations. Page number: {}, page size {}", pageNumber, pageSize);
    var annotations = service.getAnnotations(pageNumber, pageSize);
    return ResponseEntity.ok(annotations);
  }

  @GetMapping(value = "/all/json", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiMetaWrapper> getAnnotationsJsonResponse(
      @RequestParam(defaultValue = "0") int pageNumber,
      @RequestParam(defaultValue = "10") int pageSize) {
    log.info("Received get request for json paginated annotations. Page number: {}, page size {}", pageNumber, pageSize);
    String path = "sandbox.dissco.tech/api/v1/annotations/all/json";
    var annotations = service.getAnnotationsJsonResponse(pageNumber, pageSize, path);
    return ResponseEntity.ok(annotations);
  }

  @PreAuthorize("isAuthenticated()")
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<AnnotationResponse> createAnnotation(Authentication authentication,
      @RequestBody AnnotationRequest annotation) {
    var userId = getNameFromToken(authentication);
    log.info("Received new annotation from user: {}", userId);
    var annotationResponse = service.persistAnnotation(annotation, userId);
    if (annotationResponse != null){
      return ResponseEntity.status(HttpStatus.CREATED).body(annotationResponse);
    } else {
      return ResponseEntity.status(HttpStatus.OK).build();
    }
  }

  @PreAuthorize("isAuthenticated()")
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping(value = "/{prefix}/{postfix}", consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<AnnotationResponse> updateAnnotation(Authentication authentication,
      @RequestBody AnnotationRequest annotation,
      @PathVariable("prefix") String prefix,
      @PathVariable("postfix") String postfix) throws NoAnnotationFoundException {
    var id = prefix + '/' + postfix;
    var userId = getNameFromToken(authentication);
    log.info("Received update for annotation: {} from user: {}", id, userId);
    var annotationResponse = service.updateAnnotation(id, annotation, userId);
    if (annotationResponse != null){
      return ResponseEntity.status(HttpStatus.OK).body(annotationResponse);
    } else {
      return ResponseEntity.status(HttpStatus.OK).build();
    }
  }

  @PreAuthorize("isAuthenticated()")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/creator", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<AnnotationResponse>> getAnnotationsForUser(
      @RequestParam(defaultValue = "0") int pageNumber,
      @RequestParam(defaultValue = "10") int pageSize,
      Authentication authentication) {
    var userId = getNameFromToken(authentication);
    log.info("Received get request to show all annotations for user: {}", userId);
    var annotations = service.getAnnotationsForUser(userId, pageNumber, pageSize);
    return ResponseEntity.ok(annotations);
  }

  @PreAuthorize("isAuthenticated()")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/creator/json", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiMetaWrapper> getAnnotationsForUserJsonResponse(
      @RequestParam(defaultValue = "0") int pageNumber,
      @RequestParam(defaultValue = "10") int pageSize,
      Authentication authentication) {
    var userId = getNameFromToken(authentication);
    log.info("Received get request to show all annotations for user: {}", userId);
    String path = "sandbox.dissco.tech/api/v1/annotations/creator/json";
    var annotations = service.getAnnotationsForUserJsonResponse(userId, pageNumber, pageSize, path);
    return ResponseEntity.ok(annotations);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{prefix}/{postfix}/versions", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<Integer>> getAnnotationByVersion(@PathVariable("prefix") String prefix,
      @PathVariable("postfix") String postfix) {
    var id = prefix + '/' + postfix;
    log.info("Received get request for versions of annotation with id: {}", id);
    var versions = service.getAnnotationVersions(id);
    return ResponseEntity.ok(versions);
  }

  @PreAuthorize("isAuthenticated()")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping(value = "/{prefix}/{postfix}")
  public ResponseEntity<Void> deleteAnnotation(Authentication authentication,
      @PathVariable("prefix") String prefix,
      @PathVariable("postfix") String postfix) throws NoAnnotationFoundException {
    var userId = getNameFromToken(authentication);
    log.info("Received delete for annotation: {} from user: {}", (prefix + postfix), userId);
    var success = service.deleteAnnotation(prefix, postfix, getNameFromToken(authentication));
    if (success){
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    } else {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  @ExceptionHandler(NoAnnotationFoundException.class)
  public ResponseEntity<String> handleException(NoAnnotationFoundException e) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(e.getMessage());
  }

  private String getNameFromToken(Authentication authentication) {
    KeycloakPrincipal<? extends KeycloakSecurityContext> principal =
        (KeycloakPrincipal<?>) authentication.getPrincipal();
    AccessToken token = principal.getKeycloakSecurityContext().getToken();
    return token.getSubject();
  }
}