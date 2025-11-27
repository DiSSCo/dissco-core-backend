package eu.dissco.backend.service;

import static eu.dissco.backend.domain.FdoType.VIRTUAL_COLLECTION;
import static eu.dissco.backend.domain.VirtualCollectionAction.DELETE;
import static eu.dissco.backend.service.DigitalServiceUtils.createVersionNode;
import static eu.dissco.backend.utils.JsonApiUtils.wrapListResponse;
import static eu.dissco.backend.utils.TombstoneUtils.buildTombstoneMetadata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.domain.FdoType;
import eu.dissco.backend.domain.MongoCollection;
import eu.dissco.backend.domain.VirtualCollectionAction;
import eu.dissco.backend.domain.VirtualCollectionEvent;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.domain.jsonapi.JsonApiLinks;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import eu.dissco.backend.exceptions.ForbiddenException;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.exceptions.PidException;
import eu.dissco.backend.exceptions.ProcessingFailedException;
import eu.dissco.backend.repository.MongoRepository;
import eu.dissco.backend.repository.VirtualCollectionRepository;
import eu.dissco.backend.schema.Agent;
import eu.dissco.backend.schema.VirtualCollection;
import eu.dissco.backend.schema.VirtualCollection.LtcBasisOfScheme;
import eu.dissco.backend.schema.VirtualCollection.OdsStatus;
import eu.dissco.backend.schema.VirtualCollectionRequest;
import eu.dissco.backend.web.HandleComponent;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class VirtualCollectionService {

  private static final String HANDLE_PROXY = "https://hdl.handle.net/";
  private static final String VIRTUAL_COLLECTION_NOT_FOUND = "Unable to find virtual collection {}";
  private final VirtualCollectionRepository repository;
  private final RabbitMqPublisherService rabbitMqPublisherService;
  private final MongoRepository mongoRepository;
  private final HandleComponent handleComponent;
  private final ObjectMapper mapper;

  private static LtcBasisOfScheme getLtcBasisOfScheme(VirtualCollectionRequest virtualCollection) {
    return LtcBasisOfScheme.fromValue(virtualCollection.getLtcBasisOfScheme().value());
  }

  public JsonApiWrapper persistVirtualCollection(VirtualCollectionRequest virtualCollectionRequest,
      Agent agent, String path) {
    log.info("Requesting handle for Virtual Collection: {}",
        virtualCollectionRequest.getLtcCollectionName());
    String handle = postHandle(virtualCollectionRequest);
    var virtualCollection = buildVirtualCollection(virtualCollectionRequest, 1, agent, handle,
        Date.from(Instant.now()));
    repository.createVirtualCollection(virtualCollection);
    publishCreateEvent(virtualCollection, agent);
    publishVirtualCollectionEvent(new VirtualCollectionEvent(
        VirtualCollectionAction.CREATE, virtualCollection));
    var dataNode = new JsonApiData(virtualCollection.getId(), FdoType.VIRTUAL_COLLECTION.getName(),
        mapper.valueToTree(virtualCollection));
    return new JsonApiWrapper(dataNode, new JsonApiLinks(path));
  }

  private void publishVirtualCollectionEvent(VirtualCollectionEvent virtualCollectionEvent) {
    try {
      rabbitMqPublisherService.publishVirtualCollectionEvent(virtualCollectionEvent);
    } catch (JsonProcessingException e) {
      log.error(
          "Fatal exception, unable to publish virtual collection event to RabbitMQ. Manual action required",
          e);
    }
  }

  private String postHandle(VirtualCollectionRequest virtualCollectionRequest) {
    try {
      return handleComponent.postHandleVirtualCollection(virtualCollectionRequest);
    } catch (PidException e) {
      log.error("Failed to create handle for virtual collection request: {}",
          virtualCollectionRequest, e);
      throw new ProcessingFailedException("Failed to create new virtual collection", e);
    }
  }

  private void publishCreateEvent(VirtualCollection virtualCollection, Agent agent)
      throws ProcessingFailedException {
    try {
      rabbitMqPublisherService.publishCreateEvent(virtualCollection, agent);
    } catch (JsonProcessingException e) {
      log.error("Unable to publish message to RabbitMQ", e);
      rollbackVirtualCollection(virtualCollection);
      throw new ProcessingFailedException("Failed to create new virtual collection", e);
    }
  }

  private void rollbackVirtualCollection(VirtualCollection virtualCollection) {
    try {
      handleComponent.rollbackVirtualCollection(virtualCollection.getId());
    } catch (PidException e) {
      log.error(
          "Unable to rollback handle creation for virtual collection. Manually delete the following handle: {}. Cause of error: ",
          virtualCollection.getId(), e);
    }
    repository.rollbackVirtualCollectionCreate(virtualCollection.getId());
  }

  private VirtualCollection buildVirtualCollection(VirtualCollectionRequest virtualCollection,
      int version,
      Agent agent, String handle, Date createdTimestamp) {
    var id = HANDLE_PROXY + handle;
    return new VirtualCollection()
        .withId(id)
        .withDctermsIdentifier(id)
        .withType("ods:VirtualCollection")
        .withOdsFdoType("https://hdl.handle.net/21.T11148/2ac65a933b7a0361b651")
        .withSchemaVersion(version)
        .withOdsStatus(OdsStatus.ACTIVE)
        .withLtcCollectionName(virtualCollection.getLtcCollectionName())
        .withLtcDescription(virtualCollection.getLtcDescription())
        .withLtcBasisOfScheme(getLtcBasisOfScheme(virtualCollection))
        .withSchemaDateCreated(createdTimestamp)
        .withSchemaDateModified(Date.from(Instant.now()))
        .withSchemaCreator(agent)
        .withOdsHasTargetDigitalObjectFilter(
            virtualCollection.getOdsHasTargetDigitalObjectFilter());
  }

  public JsonApiWrapper getVirtualCollectionById(String id, String path) throws NotFoundException {
    var virtualCollection = repository.getVirtualCollectionById(id);
    if (virtualCollection != null) {
      var dataNode = new JsonApiData(virtualCollection.getId(),
          FdoType.VIRTUAL_COLLECTION.getName(), mapper.valueToTree(virtualCollection));
      return new JsonApiWrapper(dataNode, new JsonApiLinks(path));
    }
    log.warn(VIRTUAL_COLLECTION_NOT_FOUND, id);
    throw new NotFoundException("Virtual collection with id " + id + " not found");
  }

  public JsonApiListResponseWrapper getVirtualCollections(int pageNumber, int pageSize,
      String path) {
    var virtualCollections = repository.getVirtualCollections(pageNumber, pageSize);
    var dataNodePlusOne = mapToDataNodePlusOne(virtualCollections);
    return wrapListResponse(dataNodePlusOne, pageSize, pageNumber, path);
  }

  private List<JsonApiData> mapToDataNodePlusOne(List<VirtualCollection> virtualCollections) {
    return virtualCollections.stream()
        .map(vc -> new JsonApiData(vc.getId(), FdoType.VIRTUAL_COLLECTION.getName(),
            mapper.valueToTree(vc)))
        .toList();
  }

  public JsonApiListResponseWrapper getVirtualCollectionsForUser(String orcid, int pageNumber,
      int pageSize, String path) {
    var virtualCollections = repository.getVirtualCollectionsForUser(orcid,
        pageNumber, pageSize);
    var dataNodePlusOne = mapToDataNodePlusOne(virtualCollections);
    return wrapListResponse(dataNodePlusOne, pageSize, pageNumber, path);
  }

  public JsonApiWrapper getVirtualCollectionByVersion(String id, int version, String path)
      throws NotFoundException, JsonProcessingException {
    var eventNode = mongoRepository.getByVersion(id, version, MongoCollection.VIRTUAL_COLLECTION);
    var dataNode = new JsonApiData(HANDLE_PROXY + id, VIRTUAL_COLLECTION.getName(), eventNode);
    return new JsonApiWrapper(dataNode, new JsonApiLinks(path));
  }

  public JsonApiWrapper getVirtualCollectionVersions(String id, String path)
      throws NotFoundException {
    var versions = mongoRepository.getVersions(id, MongoCollection.VIRTUAL_COLLECTION);
    var versionsNode = createVersionNode(versions, mapper);
    var dataNode = new JsonApiData(id, "virtualCollectionVersions", versionsNode);
    return new JsonApiWrapper(dataNode, new JsonApiLinks(path));
  }

  public boolean tombstoneVirtualCollection(String prefix, String suffix, Agent agent,
      boolean isAdmin) throws NotFoundException {
    var id = prefix + "/" + suffix;
    var result = getActiveVirtualCollection(agent, isAdmin, id);
    if (result.isPresent()) {
      return tombstoneActiveVirtualCollection(agent, result.get(), id);
    } else {
      if (isAdmin) {
        log.info("No active virtual collection with id: {}", id);
      } else {
        log.info("No active virtual collection with id: {} found for user: {}", id, agent.getId());
      }
      throw new NotFoundException(
          "No active virtual collection with id: " + id + " was found for user: " + agent.getId());
    }
  }

  private boolean tombstoneActiveVirtualCollection(Agent agent, VirtualCollection virtualCollection,
      String id) {
    tombstoneHandle(id);
    var timestamp = Instant.now();
    var tombstoneVirtualCollection = buildTombstoneVirtualCollection(virtualCollection, agent,
        timestamp);
    repository.tombstoneVirtualCollection(tombstoneVirtualCollection);
    publishTombstoneEvent(agent, virtualCollection, tombstoneVirtualCollection);
    publishVirtualCollectionEvent(new VirtualCollectionEvent(DELETE, tombstoneVirtualCollection));
    return true;
  }

  private void publishTombstoneEvent(Agent agent, VirtualCollection virtualCollection,
      VirtualCollection tombstoneVirtualCollection) {
    try {
      rabbitMqPublisherService.publishTombstoneEvent(tombstoneVirtualCollection, virtualCollection,
          agent);
    } catch (JsonProcessingException e) {
      log.error("Unable to publish tombstone event to provenance service", e);
      throw new ProcessingFailedException(
          "Unable to publish tombstone event to provenance service", e);
    }
  }

  private void tombstoneHandle(String handle) throws ProcessingFailedException {
    try {
      handleComponent.tombstoneHandle(handle);
    } catch (PidException e) {
      log.error("Unable to tombstone handle {}", handle, e);
      throw new ProcessingFailedException("Unable to tombstone handle", e);
    }
  }

  private VirtualCollection buildTombstoneVirtualCollection(VirtualCollection virtualCollection,
      Agent tombstoningAgent, Instant timestamp) {
    return new VirtualCollection()
        .withId(virtualCollection.getId())
        .withType(virtualCollection.getType())
        .withDctermsIdentifier(virtualCollection.getDctermsIdentifier())
        .withOdsFdoType(virtualCollection.getOdsFdoType())
        .withOdsStatus(OdsStatus.TOMBSTONE)
        .withSchemaVersion(virtualCollection.getSchemaVersion() + 1)
        .withLtcCollectionName(virtualCollection.getLtcCollectionName())
        .withLtcDescription(virtualCollection.getLtcDescription())
        .withLtcBasisOfScheme(virtualCollection.getLtcBasisOfScheme())
        .withSchemaDateCreated(virtualCollection.getSchemaDateCreated())
        .withSchemaDateModified(Date.from(timestamp))
        .withSchemaCreator(tombstoningAgent)
        .withOdsHasTargetDigitalObjectFilter(
            virtualCollection.getOdsHasTargetDigitalObjectFilter())
        .withOdsHasTombstoneMetadata(buildTombstoneMetadata(tombstoningAgent,
            "Virtual Collection tombstoned by agent through the dissco backend", timestamp));
  }

  private Optional<VirtualCollection> getActiveVirtualCollection(Agent agent, boolean isAdmin,
      String id) {
    Optional<VirtualCollection> result;
    if (isAdmin) {
      log.info("Admin tombstoning virtual collection {}", id);
      result = repository.getActiveVirtualCollection(id, null);
    } else {
      log.info("Creator tombstoning virtual collection {}", id);
      result = repository.getActiveVirtualCollection(id, agent.getId());
    }
    return result;
  }

  public JsonApiWrapper updateVirtualCollection(String id,
      VirtualCollectionRequest virtualCollectionRequest, Agent agent, String path)
      throws NotFoundException, JsonProcessingException, ForbiddenException {
    var currentVirtualCollectionOptional = repository.getActiveVirtualCollection(id, agent.getId());
    if (currentVirtualCollectionOptional.isEmpty()) {
      log.warn(VIRTUAL_COLLECTION_NOT_FOUND, id);
      throw new NotFoundException("Virtual collection with id " + id + " not found");
    }
    var currentVirtualCollection = currentVirtualCollectionOptional.get();
    var virtualCollection = buildVirtualCollection(virtualCollectionRequest,
        currentVirtualCollection.getSchemaVersion() + 1, agent, id,
        currentVirtualCollection.getSchemaDateCreated());
    if (isEqual(currentVirtualCollection, virtualCollection)) {
      log.info("Virtual collection with id {} is not modified, skipping update", id);
      return null;
    } else {
      checkHandleUpdate(currentVirtualCollection, virtualCollection);
      repository.updateVirtualCollection(virtualCollection);
      rabbitMqPublisherService.publishUpdateEvent(virtualCollection, currentVirtualCollection,
          agent);
      var dataNode = new JsonApiData(virtualCollection.getId(),
          FdoType.VIRTUAL_COLLECTION.getName(), mapper.valueToTree(virtualCollection));
      return new JsonApiWrapper(dataNode, new JsonApiLinks(path));
    }
  }

  private void checkHandleUpdate(VirtualCollection currentVirtualCollection,
      VirtualCollection virtualCollection) {
    if (!Objects.equals(currentVirtualCollection.getLtcCollectionName(),
        virtualCollection.getLtcCollectionName()) ||
        !Objects.equals(currentVirtualCollection.getLtcBasisOfScheme(),
            virtualCollection.getLtcBasisOfScheme())) {
      log.info("Handle update is required for virtual collection with id {}",
          currentVirtualCollection.getId());
      try {
        handleComponent.updateHandle(virtualCollection);
      } catch (PidException e) {
        log.error("Failed to update handle for virtual collection with id {}",
            currentVirtualCollection.getId(), e);
        throw new ProcessingFailedException(
            "Failed to update handle for virtual collection with id "
                + currentVirtualCollection.getId(), e);
      }
    }
  }

  private boolean isEqual(VirtualCollection currentVirtualCollection,
      VirtualCollection virtualCollection) throws ForbiddenException {
    if (!Objects.equals(currentVirtualCollection.getOdsHasTargetDigitalObjectFilter(),
        virtualCollection.getOdsHasTargetDigitalObjectFilter())) {
      log.warn(
          "OdsHasTargetDigitalObjectFilter is not allowed to be modified for virtual collection with id {}",
          currentVirtualCollection.getId());
      throw new ForbiddenException(
          "OdsHasTargetDigitalObjectFilter is not allowed to be modified for virtual collection with id "
              + currentVirtualCollection.getId());
    }
    return Objects.equals(currentVirtualCollection.getLtcCollectionName(),
        virtualCollection.getLtcCollectionName()) &&
        Objects.equals(currentVirtualCollection.getLtcDescription(),
            virtualCollection.getLtcDescription()) &&
        Objects.equals(currentVirtualCollection.getLtcBasisOfScheme(),
            virtualCollection.getLtcBasisOfScheme()) &&
        Objects.equals(currentVirtualCollection.getSchemaContentURL(),
            virtualCollection.getSchemaContentURL());
  }
}
