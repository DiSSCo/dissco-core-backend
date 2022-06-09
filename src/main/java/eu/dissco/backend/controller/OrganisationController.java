package eu.dissco.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.dissco.backend.domain.OrganisationDocument;
import eu.dissco.backend.domain.OrganisationTuple;
import eu.dissco.backend.service.OrganisationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cnri.cordra.api.CordraException;
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
  public ResponseEntity<List<String>> getOrganisationNames() throws CordraException {
    log.info("Received get request for organisation names");
    var names = service.getNames();
    return ResponseEntity.ok(names);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/tuples", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<OrganisationTuple>> getOrganisationTuple() throws CordraException {
    log.info("Received get request for organisation tuples");
    var tuples = service.getTuples();
    return ResponseEntity.ok(tuples);
  }

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/document", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> createDocument(@RequestBody OrganisationDocument document)
      throws CordraException, JsonProcessingException {
    log.info("Received new document for organisation: {}", document.getOrganisationId());
    var result = service.createNewDocument(document);
    log.info("Successfully store a document, create handle: {}", result.id);
    return ResponseEntity.status(HttpStatus.CREATED).body(result.id);
  }

}
