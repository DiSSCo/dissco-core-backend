package eu.dissco.backend.controller;

import static eu.dissco.backend.controller.ControllerUtils.DEFAULT_PAGE_NUM;
import static eu.dissco.backend.controller.ControllerUtils.DEFAULT_PAGE_SIZE;
import static eu.dissco.backend.controller.ControllerUtils.SANDBOX_URI;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.dissco.backend.domain.DigitalSpecimenJsonLD;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.exceptions.UnknownParameterException;
import eu.dissco.backend.exceptions.UnprocessableEntityException;
import eu.dissco.backend.service.SpecimenService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@RestController
@RequestMapping("/api/v1/specimens")
@RequiredArgsConstructor
public class SpecimenController {

  private final SpecimenService service;

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiListResponseWrapper> getSpecimen(
      @RequestParam(defaultValue = DEFAULT_PAGE_NUM) int pageNumber,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int pageSize, HttpServletRequest request) {
    log.info("Received get request for specimen");
    var path = SANDBOX_URI + request.getRequestURI();
    var specimen = service.getSpecimen(pageNumber, pageSize, path);
    return ResponseEntity.ok(specimen);
  }

  @GetMapping(value = "/latest", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiListResponseWrapper> getLatestSpecimen(
      @RequestParam(defaultValue = DEFAULT_PAGE_NUM) int pageNumber,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int pageSize, HttpServletRequest request)
      throws IOException {
    log.info("Received get request for latest digital specimen");
    var path = SANDBOX_URI + request.getRequestURI();
    var specimens = service.getLatestSpecimen(pageNumber, pageSize, path);
    return ResponseEntity.ok(specimens);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{prefix}/{suffix}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> getSpecimenById(@PathVariable("prefix") String prefix,
      @PathVariable("suffix") String suffix, HttpServletRequest request) {
    var id = prefix + '/' + suffix;
    log.info("Received get request for specimen with id: {}", id);
    var path = SANDBOX_URI + request.getRequestURI();
    var specimen = service.getSpecimenById(id, path);
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
    log.info("Received get request for specimen with id: {}", id);
    var path = SANDBOX_URI + request.getRequestURI();
    var specimen = service.getSpecimenByIdFull(id, path);
    return ResponseEntity.ok(specimen);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{prefix}/{suffix}/{version}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> getSpecimenByVersion(@PathVariable("prefix") String prefix,
      @PathVariable("suffix") String suffix, @PathVariable("version") int version,
      HttpServletRequest request)
      throws JsonProcessingException, NotFoundException, UnprocessableEntityException {
    var id = prefix + '/' + suffix;
    log.info("Received get request for specimen with id and version: {}", id);
    var path = SANDBOX_URI + request.getRequestURI();
    var specimen = service.getSpecimenByVersion(id, version, path);
    return ResponseEntity.ok(specimen);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{prefix}/{suffix}/versions", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> getSpecimenVersions(@PathVariable("prefix") String prefix,
      @PathVariable("suffix") String suffix, HttpServletRequest request) throws NotFoundException {
    var id = prefix + '/' + suffix;
    log.info("Received get request for specimen with id and version: {}", id);
    var path = SANDBOX_URI + request.getRequestURI();
    var versions = service.getSpecimenVersions(id, path);
    return ResponseEntity.ok(versions);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{prefix}/{suffix}/annotations", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiListResponseWrapper> getSpecimenAnnotations(
      @PathVariable("prefix") String prefix, @PathVariable("suffix") String suffix,
      HttpServletRequest request) {
    var id = prefix + '/' + suffix;
    log.info("Received get request for annotations of specimen with id: {}", id);
    var path = SANDBOX_URI + request.getRequestURI();
    var annotations = service.getAnnotations(id, path);
    return ResponseEntity.ok(annotations);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{prefix}/{suffix}/digitalmedia", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiListResponseWrapper> getSpecimenDigitalMedia(
      @PathVariable("prefix") String prefix, @PathVariable("suffix") String suffix,
      HttpServletRequest request) {
    var id = prefix + '/' + suffix;
    log.info("Received get request for digitalmedia of specimen with id: {}", id);
    var path = SANDBOX_URI + request.getRequestURI();
    var digitalMedia = service.getDigitalMedia(id, path);
    return ResponseEntity.ok(digitalMedia);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiListResponseWrapper> search(
      @RequestParam MultiValueMap<String, String> params,
      HttpServletRequest request) throws IOException, UnknownParameterException {
    log.info("Received request params: {}", params);
    var path = SANDBOX_URI + request.getRequestURI();
    var specimen = service.search(params, path);
    return ResponseEntity.ok(specimen);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "aggregation", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> aggregation(
      @RequestParam MultiValueMap<String, String> params, HttpServletRequest request)
      throws IOException, UnknownParameterException {
    log.info("Request for aggregations");
    var path = SANDBOX_URI + request.getRequestURI() + "?" + Objects.requireNonNullElse(
        request.getQueryString(), "");
    var aggregations = service.aggregations(params, path);
    return ResponseEntity.ok(aggregations);
  }
}


