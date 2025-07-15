package eu.dissco.backend.service;

import static eu.dissco.backend.domain.FdoType.VIRTUAL_COLLECTION;
import static eu.dissco.backend.service.DigitalServiceUtils.createVersionNode;
import static eu.dissco.backend.utils.JsonApiUtils.wrapListResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.domain.FdoType;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.domain.jsonapi.JsonApiLinks;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.exceptions.PidCreationException;
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
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class VirtualCollectionService {

  private static final String HANDLE_PROXY = "https://hdl.handle.net/";
  private static final String VIRTUAL_COLLECTION_NOT_FOUND = "Unable to find virtual collection {}";
  private static final String VIRTUAL_COLLECTION_PROVENANCE = "virtual_collection_provenance";
  private final VirtualCollectionRepository virtualCollectionRepository;
  private final RabbitMqPublisherService rabbitMqPublisherService;
  private final MongoRepository mongoRepository;
  private final HandleComponent handleComponent;
  private final ObjectMapper mapper;

  private static LtcBasisOfScheme getLtcBasisOfSchem(VirtualCollectionRequest virtualCollection) {
    return LtcBasisOfScheme.fromValue(virtualCollection.getLtcBasisOfScheme().value());
  }

  public JsonApiWrapper persistVirtualCollection(VirtualCollectionRequest virtualCollectionRequest,
      Agent agent, String path) {
    log.info("Requesting handle for Virtual Collection: {}",
        virtualCollectionRequest.getLtcCollectionName());
    var handle = handleComponent.postHandleVirtualCollection(virtualCollectionRequest);
    var virtualCollection = buildVirtualCollection(virtualCollectionRequest, agent, handle);
    virtualCollectionRepository.createVirtualCollection(virtualCollection);
    publishCreateEvent(virtualCollection, agent);
    var dataNode = new JsonApiData(virtualCollection.getId(), FdoType.VIRTUAL_COLLECTION.getName(),
        mapper.valueToTree(virtualCollection));
    return new JsonApiWrapper(dataNode, new JsonApiLinks(path));
  }

  private void publishCreateEvent(VirtualCollection virtualCollection, Agent agent)
      throws ProcessingFailedException {
    try {
      rabbitMqPublisherService.publishCreateEvent(mapper.valueToTree(virtualCollection), agent);
    } catch (JsonProcessingException e) {
      log.error("Unable to publish message to RabbitMQ", e);
      rollbackVirtualCollection(virtualCollection);
      throw new ProcessingFailedException("Failed to create new machine annotation service", e);
    }
  }

  private void rollbackVirtualCollection(VirtualCollection virtualCollection) {
    try {
      handleComponent.rollbackVirtualCollection(virtualCollection.getId());
    } catch (PidCreationException e) {
      log.error(
          "Unable to rollback handle creation for virtual collection. Manually delete the following handle: {}. Cause of error: ",
          virtualCollection.getId(), e);
    }
    virtualCollectionRepository.rollbackVirtualCollectionCreate(virtualCollection.getId());
  }

  private VirtualCollection buildVirtualCollection(VirtualCollectionRequest virtualCollection,
      Agent agent, String handle) {
    var id = HANDLE_PROXY + handle;
    return new VirtualCollection()
        .withId(id)
        .withDctermsIdentifier(id)
        .withType("ods:VirtualCollection")
        .withOdsFdoType("https://hdl.handle.net/21.T11148/2ac65a933b7a0361b651")
        .withSchemaVersion(1)
        .withOdsStatus(OdsStatus.ACTIVE)
        .withLtcCollectionName(virtualCollection.getLtcCollectionName())
        .withLtcDescription(virtualCollection.getLtcDescription())
        .withLtcBasisOfScheme(getLtcBasisOfSchem(virtualCollection))
        .withSchemaDateCreated(Date.from(Instant.now()))
        .withSchemaDateModified(Date.from(Instant.now()))
        .withSchemaCreator(agent)
        .withOdsHasTargetDigitalObjectFilter(
            virtualCollection.getOdsHasTargetDigitalObjectFilter());
  }

  public JsonApiWrapper getVirtualCollectionById(String id, String path) throws NotFoundException {
    var virtualCollection = virtualCollectionRepository.getVirtualCollectionById(id);
    if (virtualCollection != null) {
      var dataNode = new JsonApiData(virtualCollection.getId(),
          FdoType.VIRTUAL_COLLECTION.getName(), mapper.valueToTree(virtualCollection));
      return new JsonApiWrapper(dataNode, new JsonApiLinks(path));
    }
    log.warn(VIRTUAL_COLLECTION_NOT_FOUND, id);
    throw new NotFoundException("Virtual collection with id " + id + " not found");
  }

  public JsonApiListResponseWrapper getVirtualCollection(int pageNumber, int pageSize,
      String path) {
    var virtualCollections = virtualCollectionRepository.getVirtualCollection(pageNumber, pageSize);
    var dataNodePlusOne = mapToDataNodePlusOne(virtualCollections);
    return wrapListResponse(dataNodePlusOne, virtualCollections.getLeft(), pageSize, pageNumber,
        path);
  }

  private List<JsonApiData> mapToDataNodePlusOne(
      Pair<Integer, List<VirtualCollection>> virtualCollections) {
    var virtualCollectionList = virtualCollections.getRight();
    return virtualCollectionList.stream()
        .map(vc -> new JsonApiData(vc.getId(), FdoType.VIRTUAL_COLLECTION.getName(),
            mapper.valueToTree(vc)))
        .toList();
  }

  public JsonApiListResponseWrapper getVirtualCollectionForUser(String orcid, int pageNumber,
      int pageSize, String path) {
    var virtualCollections = virtualCollectionRepository.getVirtualCollectionForUser(orcid,
        pageNumber, pageSize);
    var dataNodePlusOne = mapToDataNodePlusOne(virtualCollections);
    return wrapListResponse(dataNodePlusOne, virtualCollections.getLeft(), pageSize, pageNumber,
        path);
  }

  public JsonApiWrapper getVirtualCollectionByVersion(String id, int version, String path)
      throws NotFoundException, JsonProcessingException {
    var eventNode = mongoRepository.getByVersion(id, version, VIRTUAL_COLLECTION_PROVENANCE);
    mapper.treeToValue(eventNode.get(VIRTUAL_COLLECTION.getName()), VirtualCollection.class);
    var dataNode = new JsonApiData(HANDLE_PROXY + id, VIRTUAL_COLLECTION.getName(), eventNode);
    return new JsonApiWrapper(dataNode, new JsonApiLinks(path));
  }

  public JsonApiWrapper getVirtualCollectionVersions(String id, String path)
      throws NotFoundException {
    var versions = mongoRepository.getVersions(id, VIRTUAL_COLLECTION_PROVENANCE);
    var versionsNode = createVersionNode(versions, mapper);
    var dataNode = new JsonApiData(id, "virtualCollectionVersions", versionsNode);
    return new JsonApiWrapper(dataNode, new JsonApiLinks(path));
  }
}
