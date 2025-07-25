package eu.dissco.backend.controller;

import static eu.dissco.backend.domain.FdoType.ANNOTATION;
import static eu.dissco.backend.repository.RepositoryUtils.HANDLE_STRING;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.component.SchemaValidatorComponent;
import eu.dissco.backend.domain.annotation.batch.AnnotationEventRequest;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import eu.dissco.backend.domain.openapi.annotation.AnnotationRequest;
import eu.dissco.backend.domain.openapi.annotation.AnnotationResponseList;
import eu.dissco.backend.domain.openapi.annotation.AnnotationResponseSingle;
import eu.dissco.backend.domain.openapi.annotation.BatchAnnotationCountRequest;
import eu.dissco.backend.domain.openapi.annotation.BatchAnnotationCountResponse;
import eu.dissco.backend.domain.openapi.annotation.BatchAnnotationRequest;
import eu.dissco.backend.domain.openapi.shared.VersionResponse;
import eu.dissco.backend.exceptions.ForbiddenException;
import eu.dissco.backend.exceptions.InvalidAnnotationRequestException;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.properties.ApplicationProperties;
import eu.dissco.backend.schema.AnnotationProcessingRequest;
import eu.dissco.backend.service.AnnotationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/annotation/v1")
public class AnnotationController extends BaseController {

  private final AnnotationService service;
  private final SchemaValidatorComponent schemaValidator;

  public AnnotationController(
      ApplicationProperties applicationProperties, ObjectMapper mapper, AnnotationService service,
      SchemaValidatorComponent schemaValidator) {
    super(mapper, applicationProperties);
    this.service = service;
    this.schemaValidator = schemaValidator;
  }

  @Operation(summary = "Get annotation by id")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Annotation retrieved", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = AnnotationResponseSingle.class))
      })
  })
  @GetMapping(value = "/{prefix}/{suffix}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> getAnnotation(
      @Parameter(description = PREFIX_OAS) @PathVariable("prefix") String prefix,
      @Parameter(description = SUFFIX_OAS) @PathVariable("suffix") String suffix,
      HttpServletRequest request) throws NotFoundException {
    var id = prefix + '/' + suffix;
    log.info("Received get request for annotationRequests: {}", id);
    var annotation = service.getAnnotation(id, getPath(request));
    return ResponseEntity.ok(annotation);
  }

  @Operation(summary = "Get annotation by ID and desired version")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Annotation successfully retrieved", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = AnnotationResponseSingle.class))
      })
  })
  @GetMapping(value = "/{prefix}/{suffix}/{version}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> getAnnotationByVersion(
      @Parameter(description = PREFIX_OAS) @PathVariable("prefix") String prefix,
      @Parameter(description = SUFFIX_OAS) @PathVariable("suffix") String suffix,
      @Parameter(description = VERSION_OAS) @PathVariable("version") int version,
      HttpServletRequest request)
      throws JsonProcessingException, NotFoundException {
    var id = HANDLE_STRING + prefix + '/' + suffix;
    log.info("Received get request for annotationRequests: {} with version: {}", id, version);
    var annotation = service.getAnnotationByVersion(id, version, getPath(request));
    return ResponseEntity.ok(annotation);
  }

  @Operation(summary = "Get annotations, paginated")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Annotations retrieved", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = AnnotationResponseList.class))
      })
  })
  @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiListResponseWrapper> getAnnotations(
      @Parameter(description = PAGE_NUM_OAS) @RequestParam(defaultValue = DEFAULT_PAGE_NUM) int pageNumber,
      @Parameter(description = PAGE_SIZE_OAS) @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int pageSize,
      HttpServletRequest request) {
    log.info(
        "Received get request for json paginated annotationRequests. Page number: {}, page size {}",
        pageNumber, pageSize);
    var annotations = service.getAnnotations(pageNumber, pageSize, getPath(request));
    return ResponseEntity.ok(annotations);
  }

  @Operation(
      summary = "Create annotation",
      description = """
          Create an annotation on a digital specimen or digital media. Only users who have 
          registered their ORCID may create annotations.
          """)
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Annotation successfully created", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = AnnotationResponseSingle.class))
      })
  })
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> createAnnotation(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Annotation adhering to JSON:API standard",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = AnnotationRequest.class)))
      Authentication authentication,
      @RequestBody AnnotationRequest requestBody, HttpServletRequest request)
      throws JsonProcessingException, ForbiddenException {
    var annotation = getAnnotationFromRequest(requestBody);
    var agent = getAgent(authentication);
    log.info("Received new annotationRequests from agent: {}", agent.getId());
    var annotationResponse = service.persistAnnotation(annotation, agent, getPath(request));
    if (annotationResponse != null) {
      return ResponseEntity.status(HttpStatus.CREATED).body(annotationResponse);
    } else {
      return ResponseEntity.status(HttpStatus.OK).build();
    }
  }

  @Operation(summary = "Given a set of search parameters, calculates how many objects would be annotated in a batch annotation event",
      description = """
          Given a set of search parameters, calculates how many objects would be annotated in a batch annotation event.
          This is a prerequisite for applying batch annotations. This can only be requested by users with the "batch annotations" permission.
          """)
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Projected Annotation count calculated", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = BatchAnnotationCountResponse.class))
      })
  })
  @PreAuthorize("hasRole('dissco-web-batch-annotations')")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/batch", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonNode> getCountForBatchAnnotations(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Annotation adhering to JSON:API standard",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = BatchAnnotationCountRequest.class)))
      @RequestBody BatchAnnotationCountRequest request)
      throws IOException {
    log.info("Received request for batch annotation count");
    var result = service.getCountForBatchAnnotations(request);
    return ResponseEntity.ok(result);
  }

  @Operation(summary = "Apply an annotation to all objects that match a given criteria",
      description = """
          Given a set of search parameters, applies an annotation to all objects that match this criteria.
          The first annotation created, which is the annotation on the provided target, is returned.
          Subsequent annotations are scheduled. This is only possible for users with the "batch annotations" permission.
          """
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Batching scheduled; initial annotation returned", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = AnnotationResponseSingle.class))
      })
  })
  @PreAuthorize("hasRole('dissco-web-batch-annotations')")
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/batch", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> createAnnotationBatch(Authentication authentication,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Annotation batch request",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = BatchAnnotationRequest.class)))
      @RequestBody BatchAnnotationRequest requestBody, HttpServletRequest request)
      throws JsonProcessingException, ForbiddenException, InvalidAnnotationRequestException {
    var event = getAnnotationFromRequestEvent(requestBody);
    schemaValidator.validateAnnotationEventRequest(event, true);
    var user = getAgent(authentication);
    log.info("Received new batch annotation from user: {}", user);
    var annotationResponse = service.persistAnnotationBatch(event, user, getPath(request));
    if (annotationResponse != null) {
      return ResponseEntity.status(HttpStatus.CREATED).body(annotationResponse);
    } else {
      return ResponseEntity.status(HttpStatus.OK).build();
    }
  }

  @Operation(
      summary = "Update existing annotation",
      description = """
          Update an existing annotation. Users may only update annotations they have created.
          """)
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Annotation successfully updated", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = AnnotationResponseSingle.class))
      })
  })
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping(value = "/{prefix}/{suffix}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> updateAnnotation(
      @io.swagger.v3.oas.annotations.parameters.RequestBody
          (description = "Annotation adhering to JSON:API standard",
              content = {
                  @Content(mediaType = "application/json", schema = @Schema(implementation = AnnotationRequest.class))})
      Authentication authentication, @RequestBody AnnotationRequest requestBody,
      @Parameter(description = PREFIX_OAS) @PathVariable("prefix") String prefix,
      @Parameter(description = SUFFIX_OAS) @PathVariable("suffix") String suffix,
      HttpServletRequest request)
      throws NotFoundException, JsonProcessingException, ForbiddenException {
    var id = prefix + '/' + suffix;
    var agent = getAgent(authentication);
    var annotation = getAnnotationFromRequest(requestBody);
    log.info("Received update for annotationRequests: {} from user: {}", id, agent.getId());
    var annotationResponse = service.updateAnnotation(id, annotation, agent, getPath(request),
        prefix, suffix);
    if (annotationResponse != null) {
      return ResponseEntity.status(HttpStatus.OK).body(annotationResponse);
    } else {
      return ResponseEntity.status(HttpStatus.OK).build();
    }
  }

  @Operation(summary = "Get all annotations for an authenticated user")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Annotations successfully retrieved", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = AnnotationResponseList.class))
      })
  })
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/creator", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiListResponseWrapper> getAnnotationsForUser(
      @Parameter(description = PAGE_NUM_OAS) @RequestParam(defaultValue = DEFAULT_PAGE_NUM) int pageNumber,
      @Parameter(description = PAGE_SIZE_OAS) @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int pageSize,
      HttpServletRequest request,
      Authentication authentication) throws IOException, ForbiddenException {
    var orcid = getAgent(authentication).getId();
    log.info("Received get request to show all annotationRequests for user: {}", orcid);
    var annotations = service.getAnnotationsForUser(orcid, pageNumber, pageSize,
        getPath(request));
    return ResponseEntity.ok(annotations);
  }

  @Operation(summary = "Get all versions for a given annotation")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Annotation versions successfully retrieved", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = VersionResponse.class))
      })
  })
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{prefix}/{suffix}/versions", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> getAnnotationVersions(
      @Parameter(description = PREFIX_OAS) @PathVariable("prefix") String prefix,
      @Parameter(description = SUFFIX_OAS) @PathVariable("suffix") String suffix,
      HttpServletRequest request) throws NotFoundException {
    var id = HANDLE_STRING + prefix + '/' + suffix;
    log.info("Received get request for versions of annotationRequests with id: {}", id);
    var versions = service.getAnnotationVersions(id, getPath(request));
    return ResponseEntity.ok(versions);
  }

  @Operation(summary = "Tombstone a given annotation",
      description = """
          Tombstone a given annotation. Users may only tombstone annotations they created.
          """)
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Annotation successfully tombstoned")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping(value = "/{prefix}/{suffix}")
  public ResponseEntity<Void> tombstoneAnnotation(Authentication authentication,
      @Parameter(description = PREFIX_OAS) @PathVariable("prefix") String prefix,
      @Parameter(description = SUFFIX_OAS) @PathVariable("suffix") String suffix)
      throws NotFoundException, ForbiddenException {
    var agent = getAgent(authentication);
    var isAdmin = isAdmin(authentication);
    log.info("Received delete for annotationRequests: {} from user: {}", (prefix + suffix),
        agent.getId());
    var success = service.tombstoneAnnotation(prefix, suffix, agent, isAdmin);
    if (success) {
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    } else {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  private AnnotationProcessingRequest getAnnotationFromRequest(AnnotationRequest requestBody) {
    if (!requestBody.data().type().equals(ANNOTATION)) {
      throw new IllegalArgumentException(
          "Invalid type. Type must be " + ANNOTATION.getPid() + " or " + ANNOTATION.getName()
              + " but was " + requestBody.data().type());
    }
    return requestBody.data().attributes();
  }

  private AnnotationEventRequest getAnnotationFromRequestEvent(BatchAnnotationRequest requestBody) {
    if (!requestBody.data().type().equals(ANNOTATION)) {
      throw new IllegalArgumentException(
          "Invalid type. Type must be " + ANNOTATION.getPid() + " or " + ANNOTATION.getName()
              + " but was " + requestBody.data().type());
    }
    return requestBody.data().attributes();
  }

}