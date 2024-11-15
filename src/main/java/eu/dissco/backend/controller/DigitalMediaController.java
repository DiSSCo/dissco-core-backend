package eu.dissco.backend.controller;


import static eu.dissco.backend.repository.RepositoryUtils.DOI_STRING;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.database.jooq.enums.JobState;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiRequestWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import eu.dissco.backend.domain.openapi.annotation.AnnotationResponseList;
import eu.dissco.backend.domain.openapi.annotation.AnnotationResponseSingle;
import eu.dissco.backend.domain.openapi.media.DigitalMediaResponseList;
import eu.dissco.backend.domain.openapi.media.DigitalMediaResponseSingle;
import eu.dissco.backend.domain.openapi.shared.MasResponseList;
import eu.dissco.backend.domain.openapi.shared.MasSchedulingRequest;
import eu.dissco.backend.domain.openapi.shared.MjrResponseList;
import eu.dissco.backend.domain.openapi.shared.VersionResponse;
import eu.dissco.backend.exceptions.ConflictException;
import eu.dissco.backend.exceptions.ForbiddenException;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.exceptions.PidCreationException;
import eu.dissco.backend.properties.ApplicationProperties;
import eu.dissco.backend.service.DigitalMediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("api/v1/digital-media")
public class DigitalMediaController extends BaseController {

  private final DigitalMediaService service;

  public DigitalMediaController(ApplicationProperties applicationProperties,
      ObjectMapper mapper, DigitalMediaService service) {
    super(mapper, applicationProperties);
    this.service = service;
  }

  @Operation(summary = "Get paginated digital medias")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Digital media successfully retrieved", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = DigitalMediaResponseList.class))
      })
  })
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiListResponseWrapper> getDigitalMediaObjects(
      @Parameter(description = PREFIX_OAS) @RequestParam(defaultValue = DEFAULT_PAGE_NUM) int pageNumber,
      @Parameter(description = SUFFIX_OAS) @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int pageSize,
      HttpServletRequest request) {
    log.info("Received get request for digital digital medias in json format");
    var digitalMedia = service.getDigitalMediaObjects(pageNumber, pageSize, getPath(request));
    return ResponseEntity.ok(digitalMedia);
  }

  @Operation(summary = "Get digital media by ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Digital media successfully retrieved", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = DigitalMediaResponseSingle.class))
      })
  })
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{prefix}/{suffix}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> getDigitalMediaObjectById(
      @Parameter(description = PREFIX_OAS) @PathVariable("prefix") String prefix,
      @Parameter(description = SUFFIX_OAS) @PathVariable("suffix") String suffix,
      HttpServletRequest request) {
    var id = prefix + '/' + suffix;
    log.info("Received get request for multiMedia with id: {}", id);
    var multiMedia = service.getDigitalMediaById(id, getPath(request));
    return ResponseEntity.ok(multiMedia);
  }

  @Operation(summary = "Get annotations for a given media object")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Digital media annotations successfully retrieved", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = AnnotationResponseList.class))
      })
  })
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{prefix}/{suffix}/annotations", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiListResponseWrapper> getMediaAnnotationsById(
      @Parameter(description = PREFIX_OAS) @PathVariable("prefix") String prefix,
      @Parameter(description = SUFFIX_OAS) @PathVariable("suffix") String suffix,
      HttpServletRequest request) {
    var id = prefix + '/' + suffix;
    log.info("Received get request for annotationRequests on digitalMedia with id: {}", id);
    var annotations = service.getAnnotationsOnDigitalMedia(id, getPath(request));
    return ResponseEntity.ok(annotations);
  }

  @Operation(summary = "Get all versions for a given digital media")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Digital media versions successfully retrieved", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = VersionResponse.class))
      })
  })
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{prefix}/{suffix}/versions", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> getDigitalMediaVersions(
      @Parameter(description = PREFIX_OAS) @PathVariable("prefix") String prefix,
      @Parameter(description = SUFFIX_OAS) @PathVariable("suffix") String suffix,
      HttpServletRequest request)
      throws NotFoundException {
    var id = DOI_STRING + prefix + '/' + suffix;
    log.info("Received get request for versions of digital media with id: {}", id);
    var versions = service.getDigitalMediaVersions(id, getPath(request));
    return ResponseEntity.ok(versions);
  }

  @Operation(summary = "Get digital media by ID and desired version")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Digital media successfully retrieved", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = DigitalMediaResponseSingle.class))
      })
  })
  @GetMapping(value = "/{prefix}/{suffix}/{version}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> getDigitalMediaObjectByVersion(
      @Parameter(description = PREFIX_OAS) @PathVariable("prefix") String prefix,
      @Parameter(description = SUFFIX_OAS) @PathVariable("suffix") String suffix,
      @Parameter(description = VERSION_OAS) @PathVariable("version") int version,
      HttpServletRequest request)
      throws JsonProcessingException, NotFoundException {
    var id = DOI_STRING + prefix + '/' + suffix;
    log.info("Received get request for digital media: {} with version: {}", id, version);
    var digitalMedia = service.getDigitalMediaObjectByVersion(id, version, getPath(request));
    return ResponseEntity.ok(digitalMedia);
  }

  @Operation(
      summary = "Get MASs that may be run on the given digital media",
      description = """
          Retrieves a list of Machine Annotation Services (MASs) suitable for processing a given
          digital media, based on the MASs' respective filter criteria.
          """
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Digital media MASs successfully retrieved", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = AnnotationResponseList.class))
      })
  })
  @GetMapping(value = "/{prefix}/{suffix}/mas", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiListResponseWrapper> getMassForDigitalMediaObject(
      @Parameter(description = PREFIX_OAS) @PathVariable("prefix") String prefix,
      @Parameter(description = SUFFIX_OAS) @PathVariable("suffix") String suffix,
      HttpServletRequest request) {
    var id = prefix + '/' + suffix;
    log.info("Received get request for mass for digital media: {}", id);
    var mass = service.getMass(id, getPath(request));
    return ResponseEntity.ok(mass);
  }

  @Operation(
      summary = "Get MAS jobs for digital media",
      description = """
          Retrieves a list of Machine Annotation Service Job Records (MJRs).
          These are scheduled, running, or completed machine annotation service jobs.
          Pagination is offered.
          """
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "MAS Job records successfully retrieved", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = MjrResponseList.class))
      })
  })
  @GetMapping(value = "/{prefix}/{suffix}/mjr", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiListResponseWrapper> getMasJobRecordForMedia(
      @Parameter(description = PREFIX_OAS) @PathVariable("prefix") String prefix,
      @Parameter(description = SUFFIX_OAS) @PathVariable("suffix") String suffix,
      @Parameter(description = "Optional filter on job status") @RequestParam(required = false) JobState state,
      @Parameter(description = PAGE_NUM_OAS) @RequestParam(defaultValue = DEFAULT_PAGE_NUM) int pageNumber,
      @Parameter(description = PAGE_SIZE_OAS) @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int pageSize,
      HttpServletRequest request
  ) throws NotFoundException {
    var path = getPath(request);
    var id = prefix + '/' + suffix;
    return ResponseEntity.ok(
        service.getMasJobRecordsForMedia(id, path, state, pageNumber, pageSize));
  }

  @Operation(
      summary = "Get original digital media data",
      description = """
          DiSSCo provides harmonised data according to the OpenDS specification.
          This endpoint provides the unharmonised data as it appears in the source system.
          """
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Original Data successfully retrieved", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = MjrResponseList.class))
      })
  })
  @GetMapping(value = "/{prefix}/{suffix}/original-data", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> getOriginalDataForMedia(
      @Parameter(description = PREFIX_OAS) @PathVariable("prefix") String prefix,
      @Parameter(description = SUFFIX_OAS) @PathVariable("suffix") String suffix,
      HttpServletRequest request) {
    var path = getPath(request);
    var id = prefix + '/' + suffix;
    return ResponseEntity.ok(service.getOriginalDataForMedia(id, path));
  }

  @Operation(
      summary = "Schedule Machine Annotation Services",
      description = """
         Schedules applicable MASs on a given digital media.
         Only users who have provided their ORCID may schedule MASs.
         """
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "MAS successfully scheduled", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = MjrResponseList.class))
      })
  })
  @PostMapping(value = "/{prefix}/{suffix}/mas", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiListResponseWrapper> scheduleMassForDigitalMediaObject(
      @Parameter(description = PREFIX_OAS) @PathVariable("prefix") String prefix,
      @Parameter(description = PREFIX_OAS) @PathVariable("suffix") String suffix,
      @RequestBody MasSchedulingRequest requestBody, Authentication authentication,
      HttpServletRequest request)
      throws ConflictException, ForbiddenException, PidCreationException, NotFoundException {
    var orcid = getAgent(authentication).getId();
    var id = prefix + '/' + suffix;
    var masRequests = getMassRequestFromRequest(requestBody);
    log.info("Received request to schedule all relevant MASs of: {} on digital media: {}",
        masRequests, id);

    var massResponse = service.scheduleMass(id, masRequests, getPath(request), orcid);
    return ResponseEntity.accepted().body(massResponse);
  }

}
