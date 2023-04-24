package eu.dissco.backend.controller;

import static eu.dissco.backend.controller.ControllerUtils.SANDBOX_URI;

import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.service.OrganisationService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
    var names = service.getOrganisationNames();
    return ResponseEntity.ok(names);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/tuples", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiListResponseWrapper> getOrganisations(HttpServletRequest request) {
    log.info("Received get request for organisation tuples");
    var path = SANDBOX_URI + request.getRequestURI();
    var organisations = service.getOrganisations(path);
    return ResponseEntity.ok(organisations);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/countries", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiListResponseWrapper> getOrganisationCountries(HttpServletRequest request) {
    log.info("Received get request for organisation countries");
    var path = SANDBOX_URI + request.getRequestURI();
    var countries = service.getCountries(path);
    return ResponseEntity.ok(countries);
  }

}
