package eu.dissco.backend.service;

import static eu.dissco.backend.repository.RepositoryUtils.DOI_STRING;
import static eu.dissco.backend.service.DigitalServiceUtils.createVersionNode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.dissco.backend.database.jooq.enums.JobState;
import eu.dissco.backend.database.jooq.enums.MjrTargetType;
import eu.dissco.backend.domain.DigitalMediaFull;
import eu.dissco.backend.domain.MasJobRequest;
import eu.dissco.backend.domain.FdoType;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.domain.jsonapi.JsonApiLinks;
import eu.dissco.backend.domain.jsonapi.JsonApiLinksFull;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import eu.dissco.backend.exceptions.ConflictException;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.exceptions.PidCreationException;
import eu.dissco.backend.repository.DigitalMediaRepository;
import eu.dissco.backend.repository.DigitalSpecimenRepository;
import eu.dissco.backend.repository.MongoRepository;
import eu.dissco.backend.schema.DigitalMedia;
import eu.dissco.backend.schema.DigitalSpecimen;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DigitalMediaService {

  private final DigitalMediaRepository repository;
  private final AnnotationService annotationService;
  private final DigitalSpecimenRepository digitalSpecimenRepository;
  private final MachineAnnotationServiceService masService;
  private final MongoRepository mongoRepository;
  private final ObjectMapper mapper;
  private final MasJobRecordService masJobRecordService;

  // Controller Functions
  public JsonApiListResponseWrapper getDigitalMediaObjects(int pageNumber, int pageSize,
      String path) {
    var mediaPlusOne = repository.getDigitalMediaObjects(pageNumber, pageSize);
    var dataNodePlusOne = mediaPlusOne.stream().map(media ->
        new JsonApiData(media.getDctermsIdentifier(), FdoType.DIGITAL_MEDIA.getName(),
            mapper.valueToTree(media))).toList();
    return wrapResponse(dataNodePlusOne, pageNumber, pageSize, path);
  }

  public JsonApiWrapper getDigitalMediaById(String id, String path) {
    var mediaObject = repository.getLatestDigitalMediaObjectById(id);
    var dataNode = new JsonApiData(mediaObject.getDctermsIdentifier(),
        FdoType.DIGITAL_MEDIA.getName(), mediaObject, mapper);
    var linksNode = new JsonApiLinks(path);
    return new JsonApiWrapper(dataNode, linksNode);
  }

  public JsonApiListResponseWrapper getAnnotationsOnDigitalMedia(String mediaId, String path) {
    return annotationService.getAnnotationForTarget(mediaId, path);
  }

  public JsonApiWrapper getDigitalMediaVersions(String id, String path) throws NotFoundException {
    var versions = mongoRepository.getVersions(id, "digital_media_provenance");
    var versionNode = createVersionNode(versions, mapper);
    var dataNode = new JsonApiData(id, "digitalMediaVersions", versionNode);
    return new JsonApiWrapper(dataNode, new JsonApiLinks(path));
  }

  public JsonApiWrapper getDigitalMediaObjectByVersion(String id, int version, String path)
      throws JsonProcessingException, NotFoundException {
    var digitalMediaNode = mongoRepository.getByVersion(id, version, "digital_media_provenance");
    var digitalMedia = mapResultToDigitalMedia(digitalMediaNode);
    var dataNode = new JsonApiData(digitalMedia.getDctermsIdentifier(), FdoType.DIGITAL_MEDIA.getName(), digitalMedia,
        mapper);
    return new JsonApiWrapper(dataNode, new JsonApiLinks(path));
  }

  public JsonApiListResponseWrapper getMasJobRecordsForMedia(String targetId, String path,
      JobState state, int pageNum, int pageSize) {
    return masJobRecordService.getMasJobRecordByTargetId(targetId, state, path, pageNum, pageSize);
  }

  private DigitalMedia mapResultToDigitalMedia(JsonNode dataNode) {
    return mapper.convertValue(dataNode, DigitalMedia.class);
  }

  // Used By Other Services
  public List<DigitalMediaFull> getDigitalMediaObjectFull(String id) {
    var digitalMediaFull = new ArrayList<DigitalMediaFull>();
    var digitalMedias = repository.getDigitalMediaForSpecimen(id);
    for (var digitalMedia : digitalMedias) {
      var annotation = annotationService.getAnnotationForTargetObject(digitalMedia.getDctermsIdentifier());
      digitalMediaFull.add(new DigitalMediaFull(digitalMedia, annotation));
    }
    return digitalMediaFull;
  }

  public List<String> getDigitalMediaIdsForSpecimen(String id) {
    return repository.getDigitalMediaIdsForSpecimen(id);
  }

  public List<JsonApiData> getDigitalMediaForSpecimen(String id) {
    var mediaList = repository.getDigitalMediaForSpecimen(id);
    List<JsonApiData> dataNode = new ArrayList<>();
    mediaList.forEach(media -> dataNode.add(
        new JsonApiData(media.getDctermsIdentifier(), FdoType.DIGITAL_MEDIA.getName(), mapper.valueToTree(media))));
    return dataNode;
  }

  // Response Wrapper
  private JsonApiListResponseWrapper wrapResponse(List<JsonApiData> dataNodePlusOne, int pageNumber,
      int pageSize, String path) {
    boolean hasNextPage = dataNodePlusOne.size() > pageSize;
    var dataNode = hasNextPage ? dataNodePlusOne.subList(0, pageSize) : dataNodePlusOne;
    var linksNode = new JsonApiLinksFull(pageNumber, pageSize, hasNextPage, path);
    return new JsonApiListResponseWrapper(dataNode, linksNode);
  }

  public JsonApiListResponseWrapper getMass(String id, String path) {
    var digitalMedia = repository.getLatestDigitalMediaObjectById(id);
    var digitalSpecimen = digitalSpecimenRepository.getLatestSpecimenById(
        getDsDoiFromDmo(digitalMedia));
    var flattenObjectData = flattenAttributes(digitalMedia, digitalSpecimen);
    return masService.getMassForObject(flattenObjectData, path);
  }

  public JsonApiWrapper getOriginalDataForMedia(String targetId, String path) {
    var originalData = repository.getMediaOriginalData(targetId);
    return new JsonApiWrapper(
        new JsonApiData(targetId, FdoType.DIGITAL_MEDIA.getName(), originalData),
        new JsonApiLinks(path));
  }

  private String getDsDoiFromDmo(DigitalMedia digitalMedia) {
    for (var entityRelationship : digitalMedia.getOdsHasEntityRelationships()) {
      if (entityRelationship.getDwcRelationshipOfResource().equals("hasDigitalSpecimen")) {
        return entityRelationship.getDwcRelatedResourceID().replace(DOI_STRING, "");
      }
    }
    log.warn("Digital Media Object with doi: {} is not attached to a specimen",
        digitalMedia.getDctermsIdentifier());
    return null;
  }

  private JsonNode flattenAttributes(DigitalMedia digitalMedia,
      DigitalSpecimen digitalSpecimen) {
    var objectNode = (ObjectNode) mapper.valueToTree(digitalMedia);
    objectNode.set("digitalSpecimen", mapper.valueToTree(digitalSpecimen));
    return objectNode;
  }

  public JsonApiListResponseWrapper scheduleMass(String id, Map<String, MasJobRequest> masRequests,
      String path, String orcid)
      throws PidCreationException, ConflictException, NotFoundException {
    var digitalMedia = repository.getLatestDigitalMediaObjectById(id);
    if (digitalMedia == null){
      log.error("Unable to find media with id {}", id);
      throw new NotFoundException("Specimen " + id + " not found");
    }
    var digitalSpecimen = digitalSpecimenRepository.getLatestSpecimenById(
        getDsDoiFromDmo(digitalMedia));
    if (digitalSpecimen == null){
      log.error("Unable to find specimen for media with id {}", id);
      throw new NotFoundException("Unable to find related specimen for media with id " + id);
    }
    var flattenObjectData = flattenAttributes(digitalMedia, digitalSpecimen);
    return masService.scheduleMass(flattenObjectData, masRequests, path, digitalMedia, id, orcid,
        MjrTargetType.MEDIA_OBJECT);
  }

}
