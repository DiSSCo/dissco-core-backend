package eu.dissco.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.database.jooq.enums.JobState;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import eu.dissco.backend.domain.openapi.shared.MjrResponseList;
import eu.dissco.backend.domain.openapi.shared.MjrResponseSingle;
import eu.dissco.backend.exceptions.ForbiddenException;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.properties.ApplicationProperties;
import eu.dissco.backend.service.MasJobRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@RestController
@RequestMapping("/api/v1/mjr")
public class MasJobRecordController extends BaseController {

  private final MasJobRecordService service;

  public MasJobRecordController(ObjectMapper mapper,
      ApplicationProperties applicationProperties, MasJobRecordService service) {
    super(mapper, applicationProperties);
    this.service = service;
  }

  @Operation(
      summary = "Get MAS Job Record by ID",
      description = """
          Retrieves record of running, scheduled, or completed Machine Annotation Service job
          """
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "MAS Job Record successfully retrieved", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = MjrResponseSingle.class))
      })
  })
  @GetMapping(value = "/{jobIdPrefix}/{jobIdSuffix}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> getMasJobRecord(
      @Parameter(description = PREFIX_OAS) @PathVariable("jobIdPrefix") String masJobHandlePrefix,
      @Parameter(description = SUFFIX_OAS) @PathVariable("jobIdSuffix") String masJobHandleSuffix,
      HttpServletRequest request) throws NotFoundException {
    var masJobHandle = masJobHandlePrefix + "/" + masJobHandleSuffix;
    return ResponseEntity.ok().body(service.getMasJobRecordById(masJobHandle, getPath(request)));
  }

  @Operation(
      summary = "Get MAS Job Record by creator",
      description = """
          Retrieves record of running, scheduled, or completed Machine Annotation Service job
          """
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "MAS Job Record successfully retrieved", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = MjrResponseList.class))
      })
  })
  @GetMapping(value = "/creator", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiListResponseWrapper> getMasJobRecordsForCreator(
      @Parameter(description = PAGE_NUM_OAS) @RequestParam(defaultValue = DEFAULT_PAGE_NUM) int pageNumber,
      @Parameter(description = PAGE_SIZE_OAS) @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int pageSize,
      @Parameter(description = JOB_STATUS_OAS) @RequestParam(required = false) JobState state,
      HttpServletRequest request, Authentication authentication) throws ForbiddenException {
    var creatorId = getAgent(authentication).getSchemaIdentifier();
    return ResponseEntity.ok().body(
        service.getMasJobRecordsByMasId(creatorId, getPath(request), pageNumber, pageSize,
            state));
  }

  @Operation(
      summary = "Mark job as running",
      description = """
          Utility function for Machine Annotation Services to update job status to "running".
          """
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "MAS Job Record successfully retrieved")
  })
  @GetMapping(value = "/{masIdPrefix}/{masIdSuffix}/{jobIdPrefix}/{jobIdSuffix}/running")
  public ResponseEntity<Void> markMjrAsRunning(
      @Parameter(description = "Prefix of ID of MAS") @PathVariable("masIdPrefix") String masIdPrefix,
      @Parameter(description = "Suffix of ID of MAS") @PathVariable("masIdSuffix") String masIdSuffix,
      @Parameter(description = "Prefix of ID of Job") @PathVariable("jobIdPrefix") String jobIdPrefix,
      @Parameter(description = "Suffix of ID of Job") @PathVariable("jobIdSuffix") String jobIdSuffix)
      throws NotFoundException {
    var masId = masIdPrefix + "/" + masIdSuffix;
    var jobId = jobIdPrefix + "/" + jobIdSuffix;
    service.markMasJobRecordAsRunning(masId, jobId);
    log.info("MAS Service {} successfully marked job {} as running", masId, jobId);
    return ResponseEntity.ok().build();
  }
}
