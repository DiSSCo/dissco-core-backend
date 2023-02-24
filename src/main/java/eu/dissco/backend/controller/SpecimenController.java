package eu.dissco.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.dissco.backend.domain.AnnotationResponse;
import eu.dissco.backend.domain.DigitalMediaObject;
import eu.dissco.backend.domain.DigitalSpecimen;
import eu.dissco.backend.domain.DigitalSpecimenFull;
import eu.dissco.backend.domain.DigitalSpecimenJsonLD;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.service.SpecimenService;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
  private static final String DEFAULT_PAGE_NUM = "1";
  private static final String DEFAULT_PAGE_SIZE = "10";


  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<DigitalSpecimen>> getSpecimen(
      @RequestParam(defaultValue = DEFAULT_PAGE_NUM) int pageNumber,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int pageSize) {
    log.info("Received get request for specimen");
    var specimen = service.getSpecimen(pageNumber, pageSize);
    return ResponseEntity.ok(specimen);
  }

  @GetMapping(value = "/latest", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<DigitalSpecimen>> getLatestSpecimen(
      @RequestParam(defaultValue = DEFAULT_PAGE_NUM) int pageNumber,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int pageSize) throws IOException {
    log.info("Received get request for latest digital specimen");
    var specimens = service.getLatestSpecimen(pageNumber, pageSize);
    return ResponseEntity.ok(specimens);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{prefix}/{postfix}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<DigitalSpecimen> getSpecimenById(@PathVariable("prefix") String prefix,
      @PathVariable("postfix") String postfix) {
    var id = prefix + '/' + postfix;
    log.info("Received get request for specimen with id: {}", id);
    var specimen = service.getSpecimenById(id);
    return ResponseEntity.ok(specimen);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{prefix}/{postfix}/jsonld", produces = "application/ld+json")
  public ResponseEntity<DigitalSpecimenJsonLD> getSpecimenByIdJsonLD(
      @PathVariable("prefix") String prefix, @PathVariable("postfix") String postfix) {
    var id = prefix + '/' + postfix;
    log.info("Received get request for jsonld view of specimen with id: {}", id);
    var specimen = service.getSpecimenByIdJsonLD(id);
    return ResponseEntity.ok(specimen);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{prefix}/{postfix}/full", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<DigitalSpecimenFull> getSpecimenByIdFull(
      @PathVariable("prefix") String prefix, @PathVariable("postfix") String postfix) {
    var id = prefix + '/' + postfix;
    log.info("Received get request for specimen with id: {}", id);
    var specimen = service.getSpecimenByIdFull(id);
    return ResponseEntity.ok(specimen);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{prefix}/{postfix}/{version}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<DigitalSpecimen> getSpecimenByVersion(@PathVariable("prefix") String prefix,
      @PathVariable("postfix") String postfix, @PathVariable("version") int version)
      throws JsonProcessingException, NotFoundException {
    var id = prefix + '/' + postfix;
    log.info("Received get request for specimen with id and version: {}", id);
    var specimen = service.getSpecimenByVersion(id, version);
    return ResponseEntity.ok(specimen);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{prefix}/{postfix}/versions", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<Integer>> getSpecimenVersions(@PathVariable("prefix") String prefix,
      @PathVariable("postfix") String postfix) throws NotFoundException {
    var id = prefix + '/' + postfix;
    log.info("Received get request for specimen with id and version: {}", id);
    var versions = service.getSpecimenVersions(id);
    return ResponseEntity.ok(versions);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{prefix}/{postfix}/annotations", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<AnnotationResponse>> getSpecimenAnnotations(
      @PathVariable("prefix") String prefix, @PathVariable("postfix") String postfix) {
    var id = prefix + '/' + postfix;
    log.info("Received get request for annotations of specimen with id: {}", id);
    var annotations = service.getAnnotations(id);
    return ResponseEntity.ok(annotations);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{prefix}/{postfix}/digitalmedia", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<DigitalMediaObject>> getSpecimenDigitalMedia(
      @PathVariable("prefix") String prefix, @PathVariable("postfix") String postfix) {
    var id = prefix + '/' + postfix;
    log.info("Received get request for digitalmedia of specimen with id: {}", id);
    var digitalMedia = service.getDigitalMedia(id);
    return ResponseEntity.ok(digitalMedia);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<DigitalSpecimen>> searchSpecimen(@RequestParam String query,
      @RequestParam(defaultValue = DEFAULT_PAGE_NUM) int pageNumber,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int pageSize) throws IOException {
    log.info("Received get request with query: {}", query);
    var specimen = service.search(query, pageNumber, pageSize);
    return ResponseEntity.ok(specimen);
  }

}
