package eu.dissco.backend.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.database.jooq.enums.MjrJobState;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiRequestWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import eu.dissco.backend.exceptions.ConflictException;
import eu.dissco.backend.exceptions.ForbiddenException;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.exceptions.PidCreationException;
import eu.dissco.backend.properties.ApplicationProperties;
import eu.dissco.backend.service.DigitalMediaObjectService;
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
@RequestMapping("api/v1/digitalmedia")
public class DigitalMediaObjectController extends BaseController {

  private final DigitalMediaObjectService service;

  public DigitalMediaObjectController(ApplicationProperties applicationProperties,
      ObjectMapper mapper, DigitalMediaObjectService service) {
    super(mapper, applicationProperties);
    this.service = service;
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiListResponseWrapper> getDigitalMediaObjects(
      @RequestParam(defaultValue = DEFAULT_PAGE_NUM) int pageNumber,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int pageSize, HttpServletRequest request) {
    log.info("Received get request for digital media objects in json format");
    var digitalMedia = service.getDigitalMediaObjects(pageNumber, pageSize, getPath(request));
    return ResponseEntity.ok(digitalMedia);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{prefix}/{suffix}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> getDigitalMediaObjectById(
      @PathVariable("prefix") String prefix, @PathVariable("suffix") String suffix,
      HttpServletRequest request) {
    var id = prefix + '/' + suffix;
    log.info("Received get request for multiMedia with id: {}", id);
    var multiMedia = service.getDigitalMediaById(id, getPath(request));
    return ResponseEntity.ok(multiMedia);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{prefix}/{suffix}/annotations", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiListResponseWrapper> getMediaAnnotationsById(
      @PathVariable("prefix") String prefix, @PathVariable("suffix") String suffix,
      HttpServletRequest request) {
    var id = prefix + '/' + suffix;
    log.info("Received get request for annotations on digitalMedia with id: {}", id);
    var annotations = service.getAnnotationsOnDigitalMedia(id, getPath(request));
    return ResponseEntity.ok(annotations);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{prefix}/{suffix}/versions", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> getDigitalMediaVersions(
      @PathVariable("prefix") String prefix, @PathVariable("suffix") String suffix,
      HttpServletRequest request)
      throws NotFoundException {
    var id = prefix + '/' + suffix;
    log.info("Received get request for versions of digital media with id: {}", id);
    var versions = service.getDigitalMediaVersions(id, getPath(request));
    return ResponseEntity.ok(versions);
  }

  @GetMapping(value = "/{prefix}/{suffix}/{version}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> getDigitalMediaObjectByVersion(
      @PathVariable("prefix") String prefix, @PathVariable("suffix") String suffix,
      @PathVariable("version") int version, HttpServletRequest request)
      throws JsonProcessingException, NotFoundException {
    var id = prefix + '/' + suffix;
    log.info("Received get request for digital media: {} with version: {}", id, version);
    var digitalMedia = service.getDigitalMediaObjectByVersion(id, version, getPath(request));
    return ResponseEntity.ok(digitalMedia);
  }

  @GetMapping(value = "/{prefix}/{suffix}/mas", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiListResponseWrapper> getMassForDigitalMediaObject(
      @PathVariable("prefix") String prefix, @PathVariable("suffix") String suffix,
      HttpServletRequest request) {
    var id = prefix + '/' + suffix;
    log.info("Received get request for mass for digital media: {}", id);
    var mass = service.getMass(id, getPath(request));
    return ResponseEntity.ok(mass);
  }

  @GetMapping(value = "/{prefix}/{suffix}/mjr", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiListResponseWrapper> getMasJobRecordForMedia(
      @PathVariable("prefix") String prefix,
      @PathVariable("suffix") String suffix,
      @RequestParam(required = false) MjrJobState state,
      @RequestParam(defaultValue = DEFAULT_PAGE_NUM) int pageNumber,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int pageSize,
      HttpServletRequest request
  ) throws NotFoundException {
    var path = getPath(request);
    var id = prefix + '/' + suffix;
    return ResponseEntity.ok(service.getMasJobRecordsForMedia(id, path, state, pageNumber, pageSize));
  }

  @PostMapping(value = "/{prefix}/{suffix}/mas", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiListResponseWrapper> scheduleMassForDigitalMediaObject(
      @PathVariable("prefix") String prefix, @PathVariable("suffix") String suffix,
      @RequestBody JsonApiRequestWrapper requestBody, Authentication authentication, HttpServletRequest request)
      throws JsonProcessingException, ConflictException, ForbiddenException, PidCreationException {
    var userId = authentication.getName();
    var id = prefix + '/' + suffix;
    var masIds = getMassFromRequest(requestBody);
    log.info("Received request to schedule all relevant MASs of: {} on digital media: {}", masIds,
        id);
    var massResponse = service.scheduleMass(id, masIds, getPath(request), userId);
    return ResponseEntity.accepted().body(massResponse);
  }

}
