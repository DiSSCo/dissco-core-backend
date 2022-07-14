package eu.dissco.backend.controller;

import eu.dissco.backend.domain.OrganisationDocument;
import eu.dissco.backend.domain.OrganisationTuple;
import eu.dissco.backend.service.OrganisationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@RestController
@RequestMapping("/api/v1/organisation")
@RequiredArgsConstructor
public class OrganisationController {

  private final OrganisationService service;

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/names", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<String>> getOrganisationNames() {
    log.info("Received get request for organisation names");
    var names = service.getNames();
    return ResponseEntity.ok(names);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/tuples", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<OrganisationTuple>> getOrganisationTuple() {
    log.info("Received get request for organisation tuples");
    var tuples = service.getTuples();
    return ResponseEntity.ok(tuples);
  }

}
