package eu.dissco.backend.controller;

import eu.dissco.backend.domain.OrganisationDocument;
import eu.dissco.backend.service.OrganisationDocumentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@RestController
@RequestMapping("/api/v1/organisation/document")
@RequiredArgsConstructor
public class OrganisationDocumentController {

  private final OrganisationDocumentService service;

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> createDocument(@RequestBody OrganisationDocument document) {
    log.info("Received new document for organisation: {}", document.getOrganisationId());
    var result = service.createNewDocument(document);
    log.info("Successfully store a document, create handle: {}", result.getDocumentId());
    return ResponseEntity.status(HttpStatus.CREATED).body(result.getDocumentId());
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<OrganisationDocument> getDocument(@PathVariable("id") String id) {
    log.info("Received get request for document: {} document", id);
    var result = service.getDocument(id);
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/organisation/{ror}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<OrganisationDocument>> getDocumentsForOrganisation(
      @PathVariable("ror") String ror) {
    log.info("Received get request for all documents for organisation: {}", ror);
    var result = service.getDocumentsForOrganisation(ror);
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
  }

}
