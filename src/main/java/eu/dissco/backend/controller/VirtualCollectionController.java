package eu.dissco.backend.controller;

import static eu.dissco.backend.domain.FdoType.VIRTUAL_COLLECTION;
import static eu.dissco.backend.utils.AgentUtils.ROLE_NAME_VIRTUAL_COLLECTION;
import static eu.dissco.backend.utils.ProxyUtils.HANDLE_PROXY;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import eu.dissco.backend.domain.openapi.shared.VersionResponse;
import eu.dissco.backend.domain.openapi.virtual_collection.VirtualCollectionResponseList;
import eu.dissco.backend.domain.openapi.virtual_collection.VirtualCollectionResponseSingle;
import eu.dissco.backend.exceptions.ForbiddenException;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.exceptions.ProcessingFailedException;
import eu.dissco.backend.properties.ApplicationProperties;
import eu.dissco.backend.schema.VirtualCollectionRequest;
import eu.dissco.backend.service.VirtualCollectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@RestController
@RequestMapping("/virtual-collection/v1")
public class VirtualCollectionController extends BaseController {

  private final VirtualCollectionService service;

  public VirtualCollectionController(ObjectMapper mapper,
      ApplicationProperties applicationProperties, VirtualCollectionService service) {
    super(mapper, applicationProperties);
    this.service = service;
  }

  @Operation(summary = "Get paginated virtual collections")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Virtual Collections successfully retrieved", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = VirtualCollectionResponseList.class))
      })
  })
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiListResponseWrapper> getVirtualCollections(
      @Parameter(description = PAGE_NUM_OAS) @RequestParam(defaultValue = DEFAULT_PAGE_NUM) int pageNumber,
      @Parameter(description = PAGE_SIZE_OAS) @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int pageSize,
      HttpServletRequest request) {
    log.info("Received get request for virtual collections with page number: {} and page size: {}",
        pageNumber, pageSize);
    var specimen = service.getVirtualCollections(pageNumber, pageSize, getPath(request));
    return ResponseEntity.ok(specimen);
  }

  @Operation(summary = "Get latest virtual collection by id")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Virtual Collection successfully retrieved", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = VirtualCollectionResponseSingle.class))
      })
  })
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{prefix}/{suffix}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> getVirtualCollectionById(
      @PathVariable("prefix") String prefix,
      @PathVariable("suffix") String suffix, HttpServletRequest servletRequest)
      throws NotFoundException {
    var id = prefix + '/' + suffix;
    log.info("Received request for virtual collection with id: {}", id);
    String path = getPath(servletRequest);
    var virtualCollection = service.getVirtualCollectionById(id, path);
    return ResponseEntity.ok(virtualCollection);
  }

  @Operation(summary = "Get paginated virtual collections for an authenticated user")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Virtual collections successfully retrieved", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = VirtualCollectionResponseList.class))
      })
  })
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/creator", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiListResponseWrapper> getVirtualCollectionsForUser(
      @Parameter(description = PAGE_NUM_OAS) @RequestParam(defaultValue = DEFAULT_PAGE_NUM) int pageNumber,
      @Parameter(description = PAGE_SIZE_OAS) @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int pageSize,
      HttpServletRequest request, Authentication authentication) throws ForbiddenException {
    var orcid = getAgent(authentication, ROLE_NAME_VIRTUAL_COLLECTION).getId();
    log.info("Received get request to show all virtual collections for user: {}", orcid);
    var annotations = service.getVirtualCollectionsForUser(orcid, pageNumber, pageSize,
        getPath(request));
    return ResponseEntity.ok(annotations);
  }

  @Operation(summary = "Get virtual collection by ID and desired version")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Virtual Collection successfully retrieved", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = VirtualCollectionResponseSingle.class))
      })
  })
  @GetMapping(value = "/{prefix}/{suffix}/{version}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> getVirtualCollectionByVersion(
      @Parameter(description = PREFIX_OAS) @PathVariable("prefix") String prefix,
      @Parameter(description = SUFFIX_OAS) @PathVariable("suffix") String suffix,
      @Parameter(description = VERSION_OAS) @PathVariable("version") int version,
      HttpServletRequest request)
      throws JsonProcessingException, NotFoundException {
    var id = HANDLE_PROXY + prefix + '/' + suffix;
    log.info("Received get request for virtual collection: {} with version: {}", id, version);
    var annotation = service.getVirtualCollectionByVersion(id, version, getPath(request));
    return ResponseEntity.ok(annotation);
  }

  @Operation(summary = "Get all versions for a given virtual collection")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Virtual Collection versions successfully retrieved", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = VersionResponse.class))
      })
  })
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{prefix}/{suffix}/versions", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> getVirtualCollectionVersions(
      @Parameter(description = PREFIX_OAS) @PathVariable("prefix") String prefix,
      @Parameter(description = SUFFIX_OAS) @PathVariable("suffix") String suffix,
      HttpServletRequest request) throws NotFoundException {
    var id = HANDLE_PROXY + prefix + '/' + suffix;
    log.info("Received get request for versions of virtual collection with id: {}", id);
    var versions = service.getVirtualCollectionVersions(id, getPath(request));
    return ResponseEntity.ok(versions);
  }

  @Operation(
      summary = "Create virtual collection",
      description = """
          Creates a new virtual collection. Only users who have registered their ORCID may create annotations.
          """)
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Virtual Collection successfully created", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = VirtualCollectionResponseSingle.class))
      })
  })
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('dissco-virtual-collection')")
  @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> createVirtualCollection(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Virtual Collection adhering to JSON:API standard",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = eu.dissco.backend.domain.openapi.virtual_collection.VirtualCollectionRequest.class)))
      Authentication authentication,
      @RequestBody eu.dissco.backend.domain.openapi.virtual_collection.VirtualCollectionRequest requestBody,
      HttpServletRequest request)
      throws ForbiddenException, ProcessingFailedException {
    var virtualCollection = getVirtualCollectionFromRequest(requestBody);
    var agent = getAgent(authentication, ROLE_NAME_VIRTUAL_COLLECTION);
    log.info("Received new virtualCollectionRequests from agent: {}", agent.getId());
    var virtualCollectionResponse = service.persistVirtualCollection(virtualCollection, agent,
        getPath(request));
    return ResponseEntity.status(HttpStatus.CREATED).body(virtualCollectionResponse);
  }

  @Operation(
      summary = "Update existing virtual collection",
      description = """
          Update an existing virtual collection. Users may only update virtual collection they have created.
          """)
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Virtual Collection successfully updated", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = VirtualCollectionResponseSingle.class))
      })
  })
  @PreAuthorize("hasRole('dissco-virtual-collection')")
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping(value = "/{prefix}/{suffix}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonApiWrapper> updateVirtualCollection(
      @io.swagger.v3.oas.annotations.parameters.RequestBody
          (description = "Virtual Collection adhering to JSON:API standard",
              content = {
                  @Content(mediaType = "application/json", schema = @Schema(implementation = eu.dissco.backend.domain.openapi.virtual_collection.VirtualCollectionRequest.class))})
      Authentication authentication,
      @RequestBody eu.dissco.backend.domain.openapi.virtual_collection.VirtualCollectionRequest requestBody,
      @Parameter(description = PREFIX_OAS) @PathVariable("prefix") String prefix,
      @Parameter(description = SUFFIX_OAS) @PathVariable("suffix") String suffix,
      HttpServletRequest request)
      throws NotFoundException, JsonProcessingException, ForbiddenException, ProcessingFailedException {
    var id = prefix + '/' + suffix;
    var agent = getAgent(authentication, ROLE_NAME_VIRTUAL_COLLECTION);
    var virtualCollection = getVirtualCollectionFromRequest(requestBody);
    log.info("Received update for virtual collection: {} from user: {}", id, agent.getId());
    var virtualCollectionResponse = service.updateVirtualCollection(id, virtualCollection, agent,
        getPath(request));
    if (virtualCollectionResponse != null) {
      return ResponseEntity.status(HttpStatus.OK).body(virtualCollectionResponse);
    } else {
      return ResponseEntity.status(HttpStatus.OK).build();
    }
  }

  @Operation(summary = "Tombstone a given virtual collection",
      description = """
          Tombstone a given virtual collection. Users may only tombstone virtual collections they created.
          """)
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Virtual Collection successfully tombstoned")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('dissco-virtual-collection')")
  @DeleteMapping(value = "/{prefix}/{suffix}")
  public ResponseEntity<Void> tombstoneVirtualCollection(Authentication authentication,
      @Parameter(description = PREFIX_OAS) @PathVariable("prefix") String prefix,
      @Parameter(description = SUFFIX_OAS) @PathVariable("suffix") String suffix)
      throws NotFoundException, ForbiddenException, ProcessingFailedException {
    var agent = getAgent(authentication, ROLE_NAME_VIRTUAL_COLLECTION);
    var isAdmin = isAdmin(authentication);
    log.info("Received delete for virtualCollection: {} from user: {}", (prefix + '/' + suffix),
        agent.getId());
    var success = service.tombstoneVirtualCollection(prefix, suffix, agent, isAdmin);
    if (success) {
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    } else {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  private VirtualCollectionRequest getVirtualCollectionFromRequest(
      eu.dissco.backend.domain.openapi.virtual_collection.VirtualCollectionRequest requestBody) {
    if (!requestBody.data().type().equals(VIRTUAL_COLLECTION)) {
      throw new IllegalArgumentException(
          "Invalid type. Type must be " + VIRTUAL_COLLECTION.getPid() + " or "
              + VIRTUAL_COLLECTION.getName()
              + " but was " + requestBody.data().type());
    }
    var virtualCollectionRequest = requestBody.data().attributes();
    if ((virtualCollectionRequest.getLtcCollectionName() == null
        || virtualCollectionRequest.getLtcCollectionName().isEmpty())
        || virtualCollectionRequest.getLtcBasisOfScheme() == null
        || virtualCollectionRequest.getOdsHasTargetDigitalObjectFilter() == null) {
      throw new IllegalArgumentException("LTC Collection Name must not be null or empty");
    }
    return virtualCollectionRequest;
  }

}
