package eu.dissco.backend.controller;

import eu.dissco.backend.domain.SourceSystemRecord;
import eu.dissco.backend.service.SourceSystemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@RestController
@RequestMapping("/api/v1/source-systems")
@RequiredArgsConstructor
public class SourceSystemController {

  private final SourceSystemService service;

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{prefix}/{postfix}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<SourceSystemRecord> getSourceSystemById(
      @PathVariable("prefix") String prefix,
      @PathVariable("postfix") String postfix) {
    var id = prefix + '/' + postfix;
    log.info("Received get request for source system with id: {}", id);
    var sourceSystem = service.getSourceSystemById(id);
    return ResponseEntity.ok(sourceSystem);
  }

}
