package eu.dissco.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.component.SchemaValidatorComponent;
import eu.dissco.backend.domain.annotation.batch.AnnotationEventRequest;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiRequestWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import eu.dissco.backend.exceptions.ForbiddenException;
import eu.dissco.backend.exceptions.InvalidAnnotationRequestException;
import eu.dissco.backend.exceptions.NoAnnotationFoundException;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.properties.ApplicationProperties;
import eu.dissco.backend.schema.AnnotationProcessingRequest;
import eu.dissco.backend.service.AnnotationService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
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
@RequestMapping("/api/v1/annotation")
public class AnnotationController extends BaseController {

  public static final String ANNOTATION_TYPE = "ods:Annotation";
  private final AnnotationService service;
  private final SchemaValidatorComponent schemaValidator;

  public AnnotationController(
      ApplicationProperties applicationProperties, ObjectMapper mapper, AnnotationService service,
      SchemaValidatorComponent schemaValidator) {
    super(mapper, applicationProperties);
    this.service = service;
    this.schemaValidator = schemaValidator;
  }

  @GetMapping(value = "/{prefix}/{suffix}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> getAnnotation(@PathVariable("prefix") String prefix,
      @PathVariable("suffix") String suffix, HttpServletRequest request) {
    var id = prefix + '/' + suffix;
    log.info("Received get request for annotationRequests: {}", id);
    var annotation = service.getAnnotation(id, getPath(request));
    return ResponseEntity.ok(annotation);
  }

  @GetMapping(value = "/latest", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiListResponseWrapper> getLatestAnnotations(
      @RequestParam(defaultValue = DEFAULT_PAGE_NUM) int pageNumber,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int pageSize, HttpServletRequest request)
      throws IOException {
    log.info("Received get request for latest paginated annotationRequests. Page number: {}, page size {}",
        pageNumber, pageSize);
    var annotations = service.getLatestAnnotations(pageNumber, pageSize, getPath(request));
    return ResponseEntity.ok(annotations);
  }

  @GetMapping(value = "/{prefix}/{suffix}/{version}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> getAnnotationByVersion(
      @PathVariable("prefix") String prefix,
      @PathVariable("suffix") String suffix, @PathVariable("version") int version,
      HttpServletRequest request)
      throws JsonProcessingException, NotFoundException {
    var id = prefix + '/' + suffix;
    log.info("Received get request for annotationRequests: {} with version: {}", id, version);
    var annotation = service.getAnnotationByVersion(id, version, getPath(request));
    return ResponseEntity.ok(annotation);
  }


  @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiListResponseWrapper> getAnnotations(
      @RequestParam(defaultValue = DEFAULT_PAGE_NUM) int pageNumber,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int pageSize, HttpServletRequest request) {
    log.info("Received get request for json paginated annotationRequests. Page number: {}, page size {}",
        pageNumber, pageSize);
    var annotations = service.getAnnotations(pageNumber, pageSize, getPath(request));
    return ResponseEntity.ok(annotations);
  }

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> createAnnotation(Authentication authentication,
      @RequestBody JsonApiRequestWrapper requestBody, HttpServletRequest request)
      throws JsonProcessingException, ForbiddenException {
    var annotation = getAnnotationFromRequest(requestBody);
    var user = getUser(authentication);
    log.info("Received new annotationRequests from user: {}", user.orcid());
    var annotationResponse = service.persistAnnotation(annotation, user, getPath(request));
    if (annotationResponse != null) {
      return ResponseEntity.status(HttpStatus.CREATED).body(annotationResponse);
    } else {
      return ResponseEntity.status(HttpStatus.OK).build();
    }
  }

  @PreAuthorize("hasRole('dissco-web-batch-annotations')")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/batch", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonNode> getCountForBatchAnnotations(@RequestBody JsonNode request) throws IOException {
    log.info("Received request for batch annotation count");
    var result = service.getCountForBatchAnnotations(request);
    return ResponseEntity.ok(result);
  }

  @PreAuthorize("hasRole('dissco-web-batch-annotations')")
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/batch", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> createAnnotationBatch(Authentication authentication,
      @RequestBody JsonApiRequestWrapper requestBody, HttpServletRequest request)
      throws JsonProcessingException, ForbiddenException, InvalidAnnotationRequestException {
    var event = getAnnotationFromRequestEvent(requestBody);
    schemaValidator.validateAnnotationEventRequest(event, true);
    var user = getUser(authentication);
    log.info("Received new batch annotation from user: {}", user);
    var annotationResponse = service.persistAnnotationBatch(event, user, getPath(request));
    if (annotationResponse != null) {
      return ResponseEntity.status(HttpStatus.CREATED).body(annotationResponse);
    } else {
      return ResponseEntity.status(HttpStatus.OK).build();
    }
  }

  @ResponseStatus(HttpStatus.OK)
  @PatchMapping(value = "/{prefix}/{suffix}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> updateAnnotation(Authentication authentication,
      @RequestBody JsonApiRequestWrapper requestBody, @PathVariable("prefix") String prefix,
      @PathVariable("suffix") String suffix, HttpServletRequest request)
      throws NoAnnotationFoundException, JsonProcessingException, ForbiddenException {
    var id = prefix + '/' + suffix;
    var user = getUser(authentication);
    var annotation = getAnnotationFromRequest(requestBody);
    log.info("Received update for annotationRequests: {} from user: {}", id, user.orcid());
    var annotationResponse = service.updateAnnotation(id, annotation, user, getPath(request),
        prefix, suffix);
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
      Authentication authentication) throws IOException, ForbiddenException {
    var orcid = getUser(authentication).orcid();
    log.info("Received get request to show all annotationRequests for user: {}", orcid);
    var annotations = service.getAnnotationsForUser(orcid, pageNumber, pageSize,
        getPath(request));
    return ResponseEntity.ok(annotations);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{prefix}/{suffix}/versions", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> getAnnotationVersions(@PathVariable("prefix") String prefix,
      @PathVariable("suffix") String suffix, HttpServletRequest request) throws NotFoundException {
    var id = prefix + '/' + suffix;
    log.info("Received get request for versions of annotationRequests with id: {}", id);
    var versions = service.getAnnotationVersions(id, getPath(request));
    return ResponseEntity.ok(versions);
  }

  @PreAuthorize("isAuthenticated()")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping(value = "/{prefix}/{suffix}")
  public ResponseEntity<Void> deleteAnnotation(Authentication authentication,
      @PathVariable("prefix") String prefix, @PathVariable("suffix") String suffix)
      throws NoAnnotationFoundException, ForbiddenException {
    var orcid = getUser(authentication).orcid();
    log.info("Received delete for annotationRequests: {} from user: {}", (prefix + suffix), orcid);
    var success = service.deleteAnnotation(prefix, suffix, orcid);
    if (success) {
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    } else {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  private AnnotationProcessingRequest getAnnotationFromRequest(JsonApiRequestWrapper requestBody)
      throws JsonProcessingException {
    if (!requestBody.data().type().equals(ANNOTATION_TYPE)) {
      throw new IllegalArgumentException(
          "Invalid type. Type must be " + ANNOTATION_TYPE + " but was " + requestBody.data().type());
    }
    return mapper.treeToValue(requestBody.data().attributes(), AnnotationProcessingRequest.class);
  }

  private AnnotationEventRequest getAnnotationFromRequestEvent(JsonApiRequestWrapper requestBody)
      throws JsonProcessingException {
    if (!requestBody.data().type().equals(ANNOTATION_TYPE)) {
      throw new IllegalArgumentException(
          "Invalid type. Type must be " + ANNOTATION_TYPE + " but was " + requestBody.data().type());
    }
    return mapper.treeToValue(requestBody.data().attributes(), AnnotationEventRequest.class);
  }

  @ExceptionHandler(NoAnnotationFoundException.class)
  public ResponseEntity<String> handleException(NoAnnotationFoundException e) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(e.getMessage());
  }

}