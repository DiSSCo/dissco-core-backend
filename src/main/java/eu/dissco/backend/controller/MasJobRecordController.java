package eu.dissco.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.domain.AnnotationState;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.properties.ApplicationProperties;
import eu.dissco.backend.service.MasJobRecordService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@RestController
@RequestMapping("api/v1/mjr")
public class MasJobRecordController extends BaseController {

  private final MasJobRecordService service;

  public MasJobRecordController(ObjectMapper mapper,
      ApplicationProperties applicationProperties, MasJobRecordService service) {
    super(mapper, applicationProperties);
    this.service = service;
  }

  @GetMapping(value = "/{jobId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> getMasJobRecord(
      @PathVariable("jobId") UUID jobId, HttpServletRequest request) throws NotFoundException {
    return ResponseEntity.ok().body(service.getMasJobRecordById(jobId, getPath(request)));
  }

  @GetMapping(value = "/creator/"
      + "{creatorId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiListResponseWrapper> getMasJobRecordsForCreator(
      @PathVariable("creatorId") String creatorId,
      @RequestParam(defaultValue = DEFAULT_PAGE_NUM) int pageNumber,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int pageSize,
      @RequestParam(required = false) AnnotationState state,
      HttpServletRequest request) {
    return ResponseEntity.ok().body(
        service.getMasJobRecordsByCreator(creatorId, getPath(request), pageNumber, pageSize,
            state));

  }

  @PatchMapping(value = "/{creatorId}/{masJobId}/running")
  public ResponseEntity<Void> markMjrAsRunning(
      @PathVariable("creatorId") String creatorId,
      @PathVariable("masJobId") UUID masJobId) throws NotFoundException {
    service.markMasJobRecordAsRunning(creatorId, masJobId);
    return ResponseEntity.ok().build();
  }
}
