package eu.dissco.backend.controller;

import eu.dissco.backend.domain.AnnotationResponse;
import eu.dissco.backend.domain.DigitalSpecimen;
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
@RequestMapping("/api/v1/specimen")
@RequiredArgsConstructor
public class SpecimenController {

  private final SpecimenService service;

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<DigitalSpecimen>> getSpecimen(
      @RequestParam(defaultValue = "1") int pageNumber,
      @RequestParam(defaultValue = "10") int pageSize) {
    log.info("Received get request for specimen");
    var specimen = service.getSpecimen(pageNumber, pageSize);
    return ResponseEntity.ok(specimen);
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
  @GetMapping(value = "/{prefix}/{postfix}/annotations", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<AnnotationResponse>> getSpecimenAnnotations(@PathVariable("prefix") String prefix,
      @PathVariable("postfix") String postfix) {
    var id = prefix + '/' + postfix;
    log.info("Received get request for annotations of specimen with id: {}", id);
    var specimen = service.getAnnotations(id);
    return ResponseEntity.ok(specimen);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<DigitalSpecimen>> searchSpecimen(@RequestParam String query,
      @RequestParam(defaultValue = "1") int pageNumber,
      @RequestParam(defaultValue = "10") int pageSize)
      throws IOException {
    log.info("Received get request with query: {}", query);
    var specimen = service.search(query, pageNumber, pageSize);
    return ResponseEntity.ok(specimen);
  }

}
