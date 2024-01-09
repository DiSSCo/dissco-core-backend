package eu.dissco.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.database.jooq.enums.JobStates;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.properties.ApplicationProperties;
import eu.dissco.backend.service.MasJobRecordService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

  @GetMapping(value = "/{jobIdPrefix}/{jobIdSuffix}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> getMasJobRecord(
      @PathVariable("jobIdPrefix") String masJobHandlePrefix,
      @PathVariable("jobIdSuffix") String masJobHandleSuffix,
      HttpServletRequest request) throws NotFoundException {
    var masJobHandle = masJobHandlePrefix + "/" + masJobHandleSuffix;
    return ResponseEntity.ok().body(service.getMasJobRecordById(masJobHandle, getPath(request)));
  }

  @GetMapping(value = "/creator/"
      + "{creatorId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiListResponseWrapper> getMasJobRecordsForCreator(
      @PathVariable("creatorId") String creatorId,
      @RequestParam(defaultValue = DEFAULT_PAGE_NUM) int pageNumber,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int pageSize,
      @RequestParam(required = false) JobStates state,
      HttpServletRequest request) {
    return ResponseEntity.ok().body(
        service.getMasJobRecordsByMasId(creatorId, getPath(request), pageNumber, pageSize,
            state));

  }

  @GetMapping(value = "/{masIdPrefix}/{masIdSuffix}/{jobIdPrefix}/{jobIdSuffix}/running")
  public ResponseEntity<Void> markMjrAsRunning(
      @PathVariable("masIdPrefix") String masIdPrefix,
      @PathVariable("masIdSuffix") String masIdSuffix,
      @PathVariable("jobIdPrefix") String jobIdPrefix,
      @PathVariable("jobIdSuffix") String jobIdSuffix) throws NotFoundException {
    var masId = masIdPrefix + "/" + masIdSuffix;
    var jobId = jobIdPrefix + "/" + jobIdSuffix;
    service.markMasJobRecordAsRunning(masId, jobId);
    log.info("MAS Service {} successfully marked job {} as running", masId, jobId);
    return ResponseEntity.ok().build();
  }
}
