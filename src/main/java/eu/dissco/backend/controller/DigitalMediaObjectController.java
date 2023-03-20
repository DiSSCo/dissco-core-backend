package eu.dissco.backend.controller;

import static eu.dissco.backend.controller.ControllerUtils.DEFAULT_PAGE_NUM;
import static eu.dissco.backend.controller.ControllerUtils.DEFAULT_PAGE_SIZE;
import static eu.dissco.backend.controller.ControllerUtils.SANDBOX_URI;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.service.DigitalMediaObjectService;
import javax.servlet.http.HttpServletRequest;
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
@RequestMapping("api/v1/digitalmedia")
@RequiredArgsConstructor
public class DigitalMediaObjectController {

  private final DigitalMediaObjectService service;
  private final ObjectMapper mapper;


  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiListResponseWrapper> getDigitalMediaObjects(
      @RequestParam(defaultValue = DEFAULT_PAGE_NUM) int pageNumber,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int pageSize, HttpServletRequest request) {
    log.info("Received get request for digital media objects in json format");
    String path = SANDBOX_URI + request.getRequestURI();
    var digitalMedia = service.getDigitalMediaObjects(pageNumber, pageSize, path);
    return ResponseEntity.ok(digitalMedia);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{prefix}/{postfix}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> getMultiMediaById(
      @PathVariable("prefix") String prefix, @PathVariable("postfix") String postfix,
      HttpServletRequest request) {
    var id = prefix + '/' + postfix;
    String path = SANDBOX_URI + request.getRequestURI();
    log.info("Received get request for multiMedia with id: {}", id);
    var multiMedia = service.getDigitalMediaById(id, path);
    return ResponseEntity.ok(multiMedia);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{prefix}/{postfix}/annotations", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiListResponseWrapper> getAnnotationsById(
      @PathVariable("prefix") String prefix, @PathVariable("postfix") String postfix, HttpServletRequest request) {
    String path = SANDBOX_URI + request.getRequestURI();
    var id = prefix + '/' + postfix;
    log.info("Received get request for annotations on digitalMedia with id: {}", id);
    var annotations = service.getAnnotationsOnDigitalMedia(id, path);
    return ResponseEntity.ok(annotations);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{prefix}/{postfix}/versions", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> getDigitalMediaVersions(
      @PathVariable("prefix") String prefix, @PathVariable("postfix") String postfix, HttpServletRequest request)
      throws NotFoundException {
    String path = SANDBOX_URI + request.getRequestURI();
    var id = prefix + '/' + postfix;
    log.info("Received get request for versions of digital media with id: {}", id);
    var versions = service.getDigitalMediaVersions(id, path);
    return ResponseEntity.ok(versions);
  }

  @GetMapping(value = "/{prefix}/{postfix}/{version}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> getDigitalMediaObjectByVersion(
      @PathVariable("prefix") String prefix, @PathVariable("postfix") String postfix,
      @PathVariable("version") int version, HttpServletRequest request) throws JsonProcessingException, NotFoundException {
    var id = prefix + '/' + postfix;
    var path = SANDBOX_URI + request.getRequestURI();
    log.info("Received get request for digital media: {} with version: {}", id, version);
    var digitalMedia = service.getDigitalMediaObjectByVersion(id, version, path);
    return ResponseEntity.ok(digitalMedia);
  }



}
