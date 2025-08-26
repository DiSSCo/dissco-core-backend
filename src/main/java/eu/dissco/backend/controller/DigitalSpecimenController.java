package eu.dissco.backend.controller;

import static eu.dissco.backend.repository.RepositoryUtils.DOI_STRING;
import static eu.dissco.backend.utils.AgentUtils.ROLE_NAME_ANNOTATOR;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.database.jooq.enums.JobState;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import eu.dissco.backend.domain.openapi.annotation.AnnotationResponseList;
import eu.dissco.backend.domain.openapi.media.DigitalMediaResponseList;
import eu.dissco.backend.domain.openapi.shared.MasSchedulingRequest;
import eu.dissco.backend.domain.openapi.shared.MjrResponseList;
import eu.dissco.backend.domain.openapi.shared.VersionResponse;
import eu.dissco.backend.domain.openapi.specimen.AggregationResponse;
import eu.dissco.backend.domain.openapi.specimen.DigitalSpecimenResponseFull;
import eu.dissco.backend.domain.openapi.specimen.DigitalSpecimenResponseList;
import eu.dissco.backend.domain.openapi.specimen.DigitalSpecimenResponseSingle;
import eu.dissco.backend.exceptions.ConflictException;
import eu.dissco.backend.exceptions.ForbiddenException;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.exceptions.PidException;
import eu.dissco.backend.exceptions.UnknownParameterException;
import eu.dissco.backend.properties.ApplicationProperties;
import eu.dissco.backend.service.DigitalSpecimenService;
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
import org.springframework.security.core.Authentication;
import org.springframework.util.MultiValueMap;
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
@RequestMapping("/digital-specimen/v1")
public class DigitalSpecimenController extends BaseController {

  private final DigitalSpecimenService service;

  public DigitalSpecimenController(ApplicationProperties applicationProperties, ObjectMapper mapper,
      DigitalSpecimenService service) {
    super(mapper, applicationProperties);
    this.service = service;
  }

  @Operation(summary = "Get paginated digital specimen")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Digital specimens successfully retrieved", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = DigitalSpecimenResponseList.class))
      })
  })
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiListResponseWrapper> getSpecimen(
      @Parameter(description = PAGE_NUM_OAS) @RequestParam(defaultValue = DEFAULT_PAGE_NUM) int pageNumber,
      @Parameter(description = PAGE_SIZE_OAS) @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int pageSize,
      HttpServletRequest request) throws IOException {
    log.info("Received get request for specimen");
    var specimen = service.getSpecimen(pageNumber, pageSize, getPath(request));
    return ResponseEntity.ok(specimen);
  }

  @Operation(summary = "Get latest paginated digital specimens")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Digital specimens successfully retrieved", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = DigitalSpecimenResponseList.class))
      })
  })
  @GetMapping(value = "/latest", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiListResponseWrapper> getLatestSpecimen(
      @Parameter(description = PAGE_NUM_OAS) @RequestParam(defaultValue = DEFAULT_PAGE_NUM) int pageNumber,
      @Parameter(description = PAGE_SIZE_OAS) @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int pageSize,
      HttpServletRequest request) throws IOException {
    log.info("Received get request for latest digital specimen");
    var specimens = service.getLatestSpecimen(pageNumber, pageSize, getPath(request));
    return ResponseEntity.ok(specimens);
  }

  @Operation(summary = "Get digital specimen by ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Digital specimen successfully retrieved", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = DigitalSpecimenResponseSingle.class))
      })
  })
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{prefix}/{suffix}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> getSpecimenById(
      @Parameter(description = PREFIX_OAS) @PathVariable("prefix") String prefix,
      @Parameter(description = SUFFIX_OAS) @PathVariable("suffix") String suffix,
      HttpServletRequest request) throws NotFoundException {
    var id = prefix + '/' + suffix;
    log.info("Received get request for specimen with id: {}", id);
    var specimen = service.getSpecimenById(id, getPath(request));
    return ResponseEntity.ok(specimen);
  }

  @Operation(summary = "Get full digital specimen by ID",
      description = """
          Returns full version of a given digital specimen, including digital media associated with the specimen and annotations.
          """)
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Digital specimen successfully retrieved", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = DigitalSpecimenResponseFull.class))
      })
  })
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{prefix}/{suffix}/full", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> getSpecimenByIdFull(
      @Parameter(description = PREFIX_OAS) @PathVariable("prefix") String prefix,
      @Parameter(description = SUFFIX_OAS) @PathVariable("suffix") String suffix,
      HttpServletRequest request) throws NotFoundException {
    var id = prefix + '/' + suffix;
    log.info("Received get request for full specimen with id: {}", id);
    var specimen = service.getSpecimenByIdFull(id, getPath(request));
    return ResponseEntity.ok(specimen);
  }

  @Operation(summary = "Get full digital specimen by ID and version",
      description = """
          Returns full version of a given digital specimen, including digital media associated with the specimen and annotations.
          """)
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Digital specimen successfully retrieved", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = DigitalSpecimenResponseFull.class))
      })
  })
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{prefix}/{suffix}/{version}/full", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> getSpecimenByVersionFull(
      @Parameter(description = PREFIX_OAS) @PathVariable("prefix") String prefix,
      @Parameter(description = PREFIX_OAS) @PathVariable("suffix") String suffix,
      @PathVariable("version") int version,
      HttpServletRequest request) throws NotFoundException, JsonProcessingException {
    var id = DOI_STRING + prefix + '/' + suffix;
    log.info("Received get request for full specimen with id: {} and version: {}", id, version);
    var specimen = service.getSpecimenByVersionFull(id, version, getPath(request));
    return ResponseEntity.ok(specimen);
  }

  @Operation(summary = "Get digital specimen by ID and version")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Digital specimen successfully retrieved", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = DigitalSpecimenResponseSingle.class))
      })
  })
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{prefix}/{suffix}/{version}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> getSpecimenByVersion(@PathVariable("prefix") String prefix,
      @Parameter(description = PREFIX_OAS) @PathVariable("suffix") String suffix,
      @Parameter(description = SUFFIX_OAS) @PathVariable("version") int version,
      HttpServletRequest request)
      throws JsonProcessingException, NotFoundException {
    var id = DOI_STRING + prefix + '/' + suffix;
    log.info("Received get request for specimen with id and version: {}", id);
    var specimen = service.getSpecimenByVersion(id, version, getPath(request));
    return ResponseEntity.ok(specimen);
  }

  @Operation(summary = "Get all versions for a given digital specimen")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Digital specimen versions successfully retrieved", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = VersionResponse.class))
      })
  })
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{prefix}/{suffix}/versions", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> getSpecimenVersions(
      @Parameter(description = PREFIX_OAS) @PathVariable("prefix") String prefix,
      @Parameter(description = SUFFIX_OAS) @PathVariable("suffix") String suffix,
      HttpServletRequest request) throws NotFoundException {
    var id = DOI_STRING + prefix + '/' + suffix;
    log.info("Received get request for specimen with id and version: {}", id);
    var versions = service.getSpecimenVersions(id, getPath(request));
    return ResponseEntity.ok(versions);
  }

  @Operation(summary = "Get annotations for a given digital specimen")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Digital specimen annotations successfully retrieved", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = AnnotationResponseList.class))
      })
  })
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{prefix}/{suffix}/annotations", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiListResponseWrapper> getSpecimenAnnotations(
      @Parameter(description = PREFIX_OAS) @PathVariable("prefix") String prefix,
      @Parameter(description = SUFFIX_OAS) @PathVariable("suffix") String suffix,
      HttpServletRequest request) throws NotFoundException {
    var id = prefix + '/' + suffix;
    log.info("Received get request for annotationRequests of specimen with id: {}", id);
    var annotations = service.getAnnotations(id, getPath(request));
    return ResponseEntity.ok(annotations);
  }

  @Operation(summary = "Get digital media for a given digital specimen")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Digital specimen media successfully retrieved", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = DigitalMediaResponseList.class))
      })
  })
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{prefix}/{suffix}/digital-media", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiListResponseWrapper> getSpecimenDigitalMedia(
      @Parameter(description = PREFIX_OAS) @PathVariable("prefix") String prefix,
      @Parameter(description = PREFIX_OAS) @PathVariable("suffix") String suffix,
      HttpServletRequest request) throws NotFoundException {
    var id = prefix + '/' + suffix;
    log.info("Received get request for digital media of specimen with id: {}", id);
    var digitalMedia = service.getDigitalMedia(id, getPath(request));
    return ResponseEntity.ok(digitalMedia);
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
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{prefix}/{suffix}/mjr", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiListResponseWrapper> getMasJobRecordsForSpecimen(
      @Parameter(description = PREFIX_OAS) @PathVariable("prefix") String prefix,
      @Parameter(description = SUFFIX_OAS) @PathVariable("suffix") String suffix,
      @Parameter(description = JOB_STATUS_OAS) @RequestParam(required = false) JobState state,
      @Parameter(description = PAGE_NUM_OAS) @RequestParam(defaultValue = DEFAULT_PAGE_NUM) int pageNumber,
      @Parameter(description = PAGE_SIZE_OAS) @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int pageSize,
      HttpServletRequest request) {
    var id = prefix + '/' + suffix;
    log.info("Received get request for MAS Job records for specimen {}", id);
    String path = getPath(request);
    return ResponseEntity.ok(
        service.getMasJobRecordsForSpecimen(id, state, path, pageNumber, pageSize));
  }

  @Operation(
      summary = "Search for digital specimen",
      description = """
          Accepts key-value pairs of search parameters. Available search terms (literal):
          * country
          * countryCode
          * midsLevel
          * physicalSpecimenID
          * typeStatus
          * organisatonID
          * organisationName
          * sourceSystemID
          * sourceSystemName
          * specimenName
          * datasetName
          * collectionCode
          * identifiedBy
          * basisOfRecord
          * livingOrPreserved
          * habitat
          
          Additionally, a free text query can be provided: q={some query string}
          
          An example of a combined parameter and free text query: /search?q=Sabellaria+bellis&topicDiscipline=Zoology&midsLevel=1
          
          Wildcards are supported: "*"
          """
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Search results successfully retrieved", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = DigitalSpecimenResponseList.class))
      })
  })
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiListResponseWrapper> search(
      @Parameter(description = "Search parameters") @RequestParam MultiValueMap<String, String> params,
      HttpServletRequest request) throws IOException, UnknownParameterException {
    log.info("Received request params: {}", params);
    var specimen = service.search(params, getPath(request));
    return ResponseEntity.ok(specimen);
  }

  @Operation(
      summary = "Aggregate digital specimens",
      description = """
          Accepts key-value pairs of terns to aggregate on. If no aggregation terms are set, returns 
          aggregations on all aggregatable terms.
          """
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Aggregations successfully retrieved", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = AggregationResponse.class))
      })
  })
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/aggregation", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> aggregation(
      @Parameter(description = "Aggregation terms") @RequestParam MultiValueMap<String, String> params,
      HttpServletRequest request) throws IOException, UnknownParameterException {
    log.info("Request for aggregations");
    var aggregations = service.aggregations(params, getPath(request));
    return ResponseEntity.ok(aggregations);
  }

  @Operation(
      summary = "Aggregate digital specimens on taxonomy",
      description = """
          Accepts key-value pairs of terns to aggregate on.
          """
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Taxonomic aggregations successfully retrieved", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = AggregationResponse.class))
      })
  })
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "taxonomy/aggregation", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> taxonAggregation(
      @Parameter(description = "Taxonomic aggregation terms") @RequestParam MultiValueMap<String, String> params,
      HttpServletRequest request) throws IOException, UnknownParameterException {
    log.info("Request for taxonomy aggregations");
    var aggregations = service.taxonAggregations(params, getPath(request));
    return ResponseEntity.ok(aggregations);
  }


  @Operation(
      summary = "Aggregate digital specimens on topic discipline"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Aggregations successfully retrieved", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = AggregationResponse.class))
      })
  })
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/discipline", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> discipline(HttpServletRequest request) throws IOException {
    log.info("Request for discipline aggregations");
    var aggregations = service.discipline(getPath(request));
    return ResponseEntity.ok(aggregations);
  }

  @Operation(
      summary = "Aggregate digital specimens using search terms"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Aggregations successfully retrieved", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = AggregationResponse.class))
      })
  })
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/searchTermValue", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> searchTermValue(
      @Parameter(description = "Term to search on") @RequestParam String term,
      @Parameter(description = "Value of term") @RequestParam String value,
      @Parameter (description = "Whether or not to sort") @RequestParam(defaultValue = "false") boolean sort,
      HttpServletRequest request)
      throws IOException, UnknownParameterException {
    log.info("Request text search for term value of term: {} with value: {}", term, value);
    var result = service.searchTermValue(term, value, getPath(request), sort);
    return ResponseEntity.ok(result);
  }

  @Operation(
      summary = "Get MASs that may be run on the given digital specimen",
      description = """
          Retrieves a list of Machine Annotation Services (MASs) suitable for processing a given
          digital specimen, based on the MASs' respective filter criteria.
          """
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Digital media MASs successfully retrieved", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = AnnotationResponseList.class))
      })
  })
  @GetMapping(value = "/{prefix}/{suffix}/mas", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiListResponseWrapper> getMassForDigitalSpecimen(
      @Parameter(description = PREFIX_OAS) @PathVariable("prefix") String prefix,
      @Parameter(description = SUFFIX_OAS) @PathVariable("suffix") String suffix,
      HttpServletRequest request) throws NotFoundException {
    var id = prefix + '/' + suffix;
    log.info("Received get request for mass for digital specimen: {}", id);
    var mass = service.getMass(id, getPath(request));
    return ResponseEntity.ok(mass);
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
          @Content(mediaType = "application/json", schema = @Schema(implementation = JsonApiWrapper.class))
      })
  })
  @GetMapping(value = "/{prefix}/{suffix}/original-data", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> getOriginalDataForSpecimen(
      @PathVariable("prefix") String prefix,
      @PathVariable("suffix") String suffix,
      HttpServletRequest request) throws NotFoundException {
    var path = getPath(request);
    var id = prefix + '/' + suffix;
    return ResponseEntity.ok(service.getOriginalDataForSpecimen(id, path));
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
  public ResponseEntity<JsonApiListResponseWrapper> scheduleMassForDigitalSpecimen(
      @PathVariable("prefix") String prefix, @PathVariable("suffix") String suffix,
      @RequestBody MasSchedulingRequest requestBody, Authentication authentication,
      HttpServletRequest request)
      throws ConflictException, ForbiddenException, NotFoundException {
    var orcid = getAgent(authentication, ROLE_NAME_ANNOTATOR).getId();
    var id = prefix + '/' + suffix;
    var masRequests = getMassRequestFromRequest(requestBody);
    log.info("Received request to schedule all relevant MASs for: {} on digital specimen: {}",
        masRequests, id);
    var massResponse = service.scheduleMass(id, masRequests, orcid, getPath(request));
    return ResponseEntity.accepted().body(massResponse);
  }

}


