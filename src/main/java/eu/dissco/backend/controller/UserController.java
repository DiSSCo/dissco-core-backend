package eu.dissco.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.dissco.backend.domain.jsonapi.JsonApiLinks;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import eu.dissco.backend.exceptions.ConflictException;
import eu.dissco.backend.exceptions.ForbiddenException;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

  private static final String SELF_LINK = "https://sandbox.dissco.tech/api/v1/users/";

  private final UserService service;

  private static void checkAuthorisation(String tokenId, String id) throws ForbiddenException {
    if (!tokenId.equals(id)) {
      throw new ForbiddenException("User: " + tokenId + " is not allowed to perform this action");
    }
  }

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ResponseEntity<JsonApiWrapper> createNewUser(Authentication authentication,
      @RequestBody JsonApiWrapper request)
      throws JsonProcessingException, ConflictException, ForbiddenException {
    var tokenId = getNameFromToken(authentication);
    log.info("User: {} has requested to update user information of: {}", tokenId,
        request.getData().getId());
    checkAuthorisation(tokenId, request.getData().getId());
    var response = service.createNewUser(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new JsonApiWrapper(response, new JsonApiLinks(SELF_LINK + request.getData().getId())));
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{id}")
  public ResponseEntity<JsonApiWrapper> getUser(Authentication authentication,
      @PathVariable("id") String id) throws NotFoundException {
    log.info("User: {} has requested user information of: {}", getNameFromToken(authentication),
        id);
    var response = service.findUser(id);
    return ResponseEntity.ok(new JsonApiWrapper(response, new JsonApiLinks(SELF_LINK + id)));
  }

  @PreAuthorize("isAuthenticated()")
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping(value = "/{id}")
  public ResponseEntity<JsonApiWrapper> updateUser(Authentication authentication,
      @PathVariable("id") String id, @RequestBody JsonApiWrapper request)
      throws NotFoundException, ConflictException, ForbiddenException {
    var tokenId = getNameFromToken(authentication);
    log.info("User: {} has requested to update user information of: {}", tokenId, id);
    checkAuthorisation(tokenId, id);
    var response = service.updateUser(id, request);
    return ResponseEntity.ok(new JsonApiWrapper(response, new JsonApiLinks(SELF_LINK + id)));
  }

  @PreAuthorize("isAuthenticated()")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping(value = "/{id}")
  public ResponseEntity<Void> deleteUser(Authentication authentication,
      @PathVariable("id") String id) throws ForbiddenException, NotFoundException {
    var tokenId = getNameFromToken(authentication);
    log.info("User: {} has requested to delete user information of: {}",
        getNameFromToken(authentication), id);
    checkAuthorisation(tokenId, id);
    service.deleteUser(id);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  private String getNameFromToken(Authentication authentication) {
    return authentication.getName();
  }

}
