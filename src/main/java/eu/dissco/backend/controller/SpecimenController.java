package eu.dissco.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.dissco.backend.domain.DigitalSpecimen;
import eu.dissco.backend.service.SpecimenService;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cnri.cordra.api.CordraException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
  @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<DigitalSpecimen>> getSpecimen(
      @RequestParam(defaultValue = "1") int pageNumber,
      @RequestParam(defaultValue = "10") int pageSize)
      throws CordraException, JsonProcessingException {
    log.info("Received get request for specimen");
    var specimen = service.getSpecimen(pageNumber, pageSize);
    return ResponseEntity.ok(specimen);

  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/**", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<DigitalSpecimen> getSpecimenById(HttpServletRequest request)
      throws CordraException, JsonProcessingException {
    var id = request.getRequestURI().split(request.getContextPath() + "/api/v1/specimen/")[1];
    log.info("Received get request for specimen with id: {}", id);
    var specimen = service.getSpecimenById(id);
    return ResponseEntity.ok(specimen);

  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<DigitalSpecimen>> searchSpecimen(@RequestParam String query,
      @RequestParam(defaultValue = "1") int pageNumber,
      @RequestParam(defaultValue = "10") int pageSize)
      throws CordraException, JsonProcessingException {
    log.info("Received get request with query: {}", query);
    var specimen = service.search(query, pageNumber, pageSize);
    return ResponseEntity.ok(specimen);
  }

}
