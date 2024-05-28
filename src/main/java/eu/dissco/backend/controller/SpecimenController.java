package eu.dissco.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.database.jooq.enums.JobState;
import eu.dissco.backend.domain.DigitalSpecimenJsonLD;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiRequestWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import eu.dissco.backend.exceptions.ConflictException;
import eu.dissco.backend.exceptions.ForbiddenException;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.exceptions.PidCreationException;
import eu.dissco.backend.exceptions.UnknownParameterException;
import eu.dissco.backend.properties.ApplicationProperties;
import eu.dissco.backend.service.SpecimenService;
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
@RequestMapping("/api/v1/specimens")
public class SpecimenController extends BaseController {

  private final SpecimenService service;

  public SpecimenController(ApplicationProperties applicationProperties, ObjectMapper mapper,
      SpecimenService service) {
    super(mapper, applicationProperties);
    this.service = service;
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiListResponseWrapper> getSpecimen(
      @RequestParam(defaultValue = DEFAULT_PAGE_NUM) int pageNumber,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int pageSize, HttpServletRequest request)
      throws IOException {
    log.info("Received get request for specimen");
    var specimen = service.getSpecimen(pageNumber, pageSize, getPath(request));
    return ResponseEntity.ok(specimen);
  }

  @GetMapping(value = "/latest", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiListResponseWrapper> getLatestSpecimen(
      @RequestParam(defaultValue = DEFAULT_PAGE_NUM) int pageNumber,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int pageSize, HttpServletRequest request)
      throws IOException {
    log.info("Received get request for latest digital specimen");
    var specimens = service.getLatestSpecimen(pageNumber, pageSize, getPath(request));
    return ResponseEntity.ok(specimens);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{prefix}/{suffix}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> getSpecimenById(@PathVariable("prefix") String prefix,
      @PathVariable("suffix") String suffix, HttpServletRequest request) {
    var id = prefix + '/' + suffix;
    log.info("Received get request for specimen with id: {}", id);
    var specimen = service.getSpecimenById(id, getPath(request));
    return ResponseEntity.ok(specimen);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{prefix}/{suffix}/jsonld", produces = "application/ld+json")
  public ResponseEntity<DigitalSpecimenJsonLD> getSpecimenByIdJsonLD(
      @PathVariable("prefix") String prefix, @PathVariable("suffix") String suffix) {
    var id = prefix + '/' + suffix;
    log.info("Received get request for jsonld view of specimen with id: {}", id);
    var specimen = service.getSpecimenByIdJsonLD(id);
    return ResponseEntity.ok(specimen);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{prefix}/{suffix}/full", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> getSpecimenByIdFull(
      @PathVariable("prefix") String prefix, @PathVariable("suffix") String suffix,
      HttpServletRequest request) {
    var id = prefix + '/' + suffix;
    log.info("Received get request for full specimen with id: {}", id);
    var specimen = service.getSpecimenByIdFull(id, getPath(request));
    return ResponseEntity.ok(specimen);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{prefix}/{suffix}/{version}/full", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> getSpecimenByVersionFull(
      @PathVariable("prefix") String prefix,
      @PathVariable("suffix") String suffix, @PathVariable("version") int version,
      HttpServletRequest request) throws NotFoundException, JsonProcessingException {
    var id = prefix + '/' + suffix;
    log.info("Received get request for full specimen with id: {} and version: {}", id, version);
    var specimen = service.getSpecimenByVersionFull(id, version, getPath(request));
    return ResponseEntity.ok(specimen);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{prefix}/{suffix}/{version}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> getSpecimenByVersion(@PathVariable("prefix") String prefix,
      @PathVariable("suffix") String suffix, @PathVariable("version") int version,
      HttpServletRequest request)
      throws JsonProcessingException, NotFoundException {
    var id = prefix + '/' + suffix;
    log.info("Received get request for specimen with id and version: {}", id);
    var specimen = service.getSpecimenByVersion(id, version, getPath(request));
    return ResponseEntity.ok(specimen);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{prefix}/{suffix}/versions", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> getSpecimenVersions(@PathVariable("prefix") String prefix,
      @PathVariable("suffix") String suffix, HttpServletRequest request) throws NotFoundException {
    var id = prefix + '/' + suffix;
    log.info("Received get request for specimen with id and version: {}", id);
    var versions = service.getSpecimenVersions(id, getPath(request));
    return ResponseEntity.ok(versions);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{prefix}/{suffix}/annotations", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiListResponseWrapper> getSpecimenAnnotations(
      @PathVariable("prefix") String prefix, @PathVariable("suffix") String suffix,
      HttpServletRequest request) {
    var id = prefix + '/' + suffix;
    log.info("Received get request for annotations of specimen with id: {}", id);
    var annotations = service.getAnnotations(id, getPath(request));
    return ResponseEntity.ok(annotations);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{prefix}/{suffix}/digitalmedia", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiListResponseWrapper> getSpecimenDigitalMedia(
      @PathVariable("prefix") String prefix, @PathVariable("suffix") String suffix,
      HttpServletRequest request) {
    var id = prefix + '/' + suffix;
    log.info("Received get request for digitalmedia of specimen with id: {}", id);
    var digitalMedia = service.getDigitalMedia(id, getPath(request));
    return ResponseEntity.ok(digitalMedia);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{prefix}/{suffix}/mjr", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiListResponseWrapper> getMasJobRecordsForSpecimen(
      @PathVariable("prefix") String prefix, @PathVariable("suffix") String suffix,
      @RequestParam(required = false) JobState state,
      @RequestParam(defaultValue = DEFAULT_PAGE_NUM) int pageNumber,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int pageSize,
      HttpServletRequest request) throws NotFoundException {
    var id = prefix + '/' + suffix;
    log.info("Received get request for MAS Job records for specimen {}", id);
    String path = getPath(request);
    return ResponseEntity.ok(
        service.getMasJobRecordsForSpecimen(id, state, path, pageNumber, pageSize));
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiListResponseWrapper> search(
      @RequestParam MultiValueMap<String, String> params,
      HttpServletRequest request) throws IOException, UnknownParameterException {
    log.info("Received request params: {}", params);
    var specimen = service.search(params, getPath(request));
    return ResponseEntity.ok(specimen);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "aggregation", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> aggregation(
      @RequestParam MultiValueMap<String, String> params, HttpServletRequest request)
      throws IOException, UnknownParameterException {
    log.info("Request for aggregations");
    var aggregations = service.aggregations(params, getPath(request));
    return ResponseEntity.ok(aggregations);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "taxonomy/aggregation", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> taxonAggregation(
      @RequestParam MultiValueMap<String, String> params, HttpServletRequest request)
      throws IOException, UnknownParameterException {
    log.info("Request for taxonomy aggregations");
    var aggregations = service.taxonAggregations(params, getPath(request));
    return ResponseEntity.ok(aggregations);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "discipline", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> discipline(HttpServletRequest request) throws IOException {
    log.info("Request for aggregations");
    var aggregations = service.discipline(getPath(request));
    return ResponseEntity.ok(aggregations);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "searchTermValue", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> searchTermValue(
      @RequestParam String term, @RequestParam String value,
      @RequestParam(defaultValue = "false") boolean sort,
      HttpServletRequest request)
      throws IOException, UnknownParameterException {
    log.info("Request text search for term value of term: {} with value: {}", term, value);
    var result = service.searchTermValue(term, value, getPath(request), sort);
    return ResponseEntity.ok(result);
  }

  @GetMapping(value = "/{prefix}/{suffix}/mas", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiListResponseWrapper> getMassForDigitalSpecimen(
      @PathVariable("prefix") String prefix, @PathVariable("suffix") String suffix,
      HttpServletRequest request) {
    var id = prefix + '/' + suffix;
    log.info("Received get request for mass for digital specimen: {}", id);
    var mass = service.getMass(id, getPath(request));
    return ResponseEntity.ok(mass);
  }

  @PostMapping(value = "/{prefix}/{suffix}/mas", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiListResponseWrapper> scheduleMassForDigitalSpecimen(
      @PathVariable("prefix") String prefix, @PathVariable("suffix") String suffix,
      @RequestBody JsonApiRequestWrapper requestBody, Authentication authentication,
      HttpServletRequest request)
      throws ConflictException, ForbiddenException, PidCreationException {
    var userId = authentication.getName();
    var id = prefix + '/' + suffix;
    var masRequests = getMassRequestFromRequest(requestBody);
    log.info("Received request to schedule all relevant MASs for: {} on digital specimen: {}",
        masRequests, id);
    var massResponse = service.scheduleMass(id, masRequests, userId, getPath(request));
    return ResponseEntity.accepted().body(massResponse);
  }

}


