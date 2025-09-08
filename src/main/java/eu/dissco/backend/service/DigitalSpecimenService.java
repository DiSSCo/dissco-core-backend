package eu.dissco.backend.service;

import static eu.dissco.backend.domain.DefaultMappingTerms.TOPIC_DISCIPLINE;
import static eu.dissco.backend.domain.DefaultMappingTerms.getParamMapping;
import static eu.dissco.backend.service.DigitalServiceUtils.createVersionNode;
import static eu.dissco.backend.utils.JsonApiUtils.wrapListResponse;
import static java.util.Comparator.comparing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.dissco.backend.database.jooq.enums.JobState;
import eu.dissco.backend.database.jooq.enums.MjrTargetType;
import eu.dissco.backend.domain.DefaultMappingTerms;
import eu.dissco.backend.domain.DigitalSpecimenFull;
import eu.dissco.backend.domain.FdoType;
import eu.dissco.backend.domain.MappingTerm;
import eu.dissco.backend.domain.MasJobRequest;
import eu.dissco.backend.domain.MongoCollection;
import eu.dissco.backend.domain.TaxonMappingTerms;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import eu.dissco.backend.domain.jsonapi.JsonApiLinks;
import eu.dissco.backend.domain.jsonapi.JsonApiLinksFull;
import eu.dissco.backend.domain.jsonapi.JsonApiListResponseWrapper;
import eu.dissco.backend.domain.jsonapi.JsonApiMeta;
import eu.dissco.backend.domain.jsonapi.JsonApiWrapper;
import eu.dissco.backend.exceptions.MasSchedulingException;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.exceptions.UnknownParameterException;
import eu.dissco.backend.repository.DigitalSpecimenRepository;
import eu.dissco.backend.repository.ElasticSearchRepository;
import eu.dissco.backend.repository.MongoRepository;
import eu.dissco.backend.schema.DigitalSpecimen;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class DigitalSpecimenService {

  private static final String DEFAULT_PAGE_NUM = "1";
  private static final String DEFAULT_PAGE_SIZE = "10";
  private static final String AGGREGATIONS_TYPE = "aggregations";
  private static final String SPECIMEN_NOT_FOUND = "Unable to find specimen {}";
  private final ObjectMapper mapper;
  private final DigitalSpecimenRepository repository;
  private final ElasticSearchRepository elasticRepository;
  private final DigitalMediaService digitalMediaService;
  private final MachineAnnotationServiceService masService;
  private final AnnotationService annotationService;
  private final MongoRepository mongoRepository;
  private final MasJobRecordService masJobRecordService;

  private static Set<MappingTerm> retrieveTaxRanks(
      Map<TaxonMappingTerms, List<String>> mappedParams) {
    var maxResult = mappedParams.keySet().stream().max(comparing(Enum::ordinal));
    var levels = new HashSet<MappingTerm>();
    if (maxResult.isEmpty()) {
      levels.add(TaxonMappingTerms.KINGDOM);
    } else {
      for (var value : TaxonMappingTerms.getTaxonMapping().values()) {
        if (value.ordinal() <= maxResult.get().ordinal() + 1) {
          levels.add(value);
        }
      }
    }
    return levels;
  }

  public JsonApiListResponseWrapper getSpecimen(int pageNumber, int pageSize, String path)
      throws IOException {
    var elasticSearchResults = elasticRepository.getSpecimens(pageNumber, pageSize);
    var digitalSpecimenList = elasticSearchResults.getRight();
    var dataNodePlusOne = digitalSpecimenList.stream()
        .map(specimen -> new JsonApiData(specimen.getDctermsIdentifier(),
            FdoType.DIGITAL_SPECIMEN.getName(), specimen, mapper))
        .toList();
    return wrapListResponse(dataNodePlusOne, elasticSearchResults.getLeft(),
        pageSize, pageNumber, path);
  }

  public JsonApiListResponseWrapper getLatestSpecimen(int pageNumber, int pageSize, String path)
      throws IOException {
    var elasticSearchResults = elasticRepository.getLatestSpecimen(pageNumber, pageSize);
    var digitalSpecimenList = elasticSearchResults.getRight();
    var dataNodePlusOne = digitalSpecimenList.stream()
        .map(specimen -> new JsonApiData(specimen.getDctermsIdentifier(),
            FdoType.DIGITAL_SPECIMEN.getName(), specimen, mapper))
        .toList();
    return wrapListResponse(dataNodePlusOne, elasticSearchResults.getLeft(),
        pageSize, pageNumber, path);
  }

  public JsonApiWrapper getSpecimenById(String id, String path) throws NotFoundException {
    var digitalSpecimen = repository.getLatestSpecimenById(id);
    if (digitalSpecimen != null) {
      var dataNode = new JsonApiData(digitalSpecimen.getDctermsIdentifier(),
          FdoType.DIGITAL_SPECIMEN.getName(), digitalSpecimen,
          mapper);
      return new JsonApiWrapper(dataNode, new JsonApiLinks(path));
    }
    log.warn(SPECIMEN_NOT_FOUND, id);
    throw new NotFoundException("Unable to find specimen " + id);
  }

  public JsonApiWrapper getSpecimenByIdFull(String id, String path) throws NotFoundException {
    var digitalSpecimen = repository.getLatestSpecimenById(id);
    if (digitalSpecimen != null) {
      return mapFullSpecimen(id, path, digitalSpecimen);
    }
    log.warn(SPECIMEN_NOT_FOUND, id);
    throw new NotFoundException("Unable to find specimen " + id);
  }

  public JsonApiWrapper getSpecimenByVersionFull(String id, int version, String path)
      throws NotFoundException, JsonProcessingException {
    var specimenNode = mongoRepository.getByVersion(id, version, MongoCollection.DIGITAL_SPECIMEN);
    if (specimenNode != null) {
      var specimen = mapResultToSpecimen(specimenNode);
      return mapFullSpecimen(id, path, specimen);
    }
    log.warn(SPECIMEN_NOT_FOUND, id);
    throw new NotFoundException("Unable to find specimen " + id);
  }

  public JsonApiListResponseWrapper getMasJobRecordsForSpecimen(String targetId,
      JobState state, String path, int pageNum, int pageSize) {
    return masJobRecordService.getMasJobRecordByTargetId(targetId, state, path, pageNum, pageSize);
  }

  public JsonApiWrapper getOriginalDataForSpecimen(String targetId, String path)
      throws NotFoundException {
    var originalData = repository.getSpecimenOriginalData(targetId);
    if (originalData != null) {
      return new JsonApiWrapper(
          new JsonApiData(targetId, FdoType.DIGITAL_SPECIMEN.getName(), originalData),
          new JsonApiLinks(path));
    }
    log.warn("Unable to find specimen {}, can not retrieve original data", targetId);
    throw new NotFoundException(
        "Can not find specimen " + targetId + ", can not retrieve origina; data");
  }

  private JsonApiWrapper mapFullSpecimen(String id, String path, DigitalSpecimen specimen) {
    var digitalMedia = digitalMediaService.getFullDigitalMediaFromSpecimen(specimen);
    var annotation = annotationService.getAnnotationForTargetObject(id);
    var attributeNode = mapper.valueToTree(
        new DigitalSpecimenFull(specimen, digitalMedia, annotation));
    return new JsonApiWrapper(
        new JsonApiData(specimen.getDctermsIdentifier(), FdoType.DIGITAL_SPECIMEN.getName(),
            attributeNode),
        new JsonApiLinks(path));
  }

  public JsonApiWrapper getSpecimenByVersion(String id, int version, String path)
      throws JsonProcessingException, NotFoundException {
    var specimenNode = mongoRepository.getByVersion(id, version, MongoCollection.DIGITAL_SPECIMEN);
    var specimen = mapResultToSpecimen(specimenNode);
    var dataNode = new JsonApiData(specimen.getDctermsIdentifier(),
        FdoType.DIGITAL_SPECIMEN.getName(), specimen, mapper);
    return new JsonApiWrapper(dataNode, new JsonApiLinks(path));
  }

  public JsonApiWrapper getSpecimenVersions(String id, String path) throws NotFoundException {
    var versionsList = mongoRepository.getVersions(id, MongoCollection.DIGITAL_SPECIMEN);
    var versionNode = createVersionNode(versionsList, mapper);
    return new JsonApiWrapper(new JsonApiData(id, "digitalSpecimenVersions", versionNode),
        new JsonApiLinks(path));
  }

  public JsonApiListResponseWrapper getAnnotations(String id, String path) {
    return annotationService.getAnnotationForTarget(id, path);
  }

  public JsonApiListResponseWrapper getDigitalMedia(String id, String path)
      throws NotFoundException {
    var specimen = repository.getLatestSpecimenById(id);
    if (specimen == null) {
      log.warn("Unable to find specimen {}. Can not retrieve media", id);
      throw new NotFoundException("Unable to retrieve media. Specimen " + id + " does not exist");
    }
    var digitalMedias = digitalMediaService.getDigitalMediaFromSpecimen(specimen);
    var dataNodes =
        digitalMedias.stream()
            .map(media -> new JsonApiData(
                media.getId(), FdoType.DIGITAL_MEDIA.getName(), media, mapper
            ))
            .toList();
    return new JsonApiListResponseWrapper(dataNodes, new JsonApiLinksFull(path));
  }

  private DigitalSpecimen mapResultToSpecimen(JsonNode result)
      throws JsonProcessingException {
    return mapper.treeToValue(result, DigitalSpecimen.class);
  }

  private JsonApiListResponseWrapper wrapListResponseSearchResults(
      Pair<Long, List<DigitalSpecimen>> digitalSpecimenSearchResult,
      int pageNumber, int pageSize, String path) {
    var dataNodePlusOne = digitalSpecimenSearchResult.getRight().stream()
        .map(specimen -> new JsonApiData(specimen.getDctermsIdentifier(),
            FdoType.DIGITAL_SPECIMEN.getName(), specimen,
            mapper))
        .toList();
    boolean hasNext = dataNodePlusOne.size() > pageSize;
    var linksNode = new JsonApiLinksFull(pageNumber, pageSize, hasNext, path);
    var dataNode = hasNext ? dataNodePlusOne.subList(0, pageSize) : dataNodePlusOne;
    return new JsonApiListResponseWrapper(dataNode, linksNode,
        new JsonApiMeta(digitalSpecimenSearchResult.getLeft()));
  }

  public JsonApiListResponseWrapper search(MultiValueMap<String, String> params, String path)
      throws IOException, UnknownParameterException {
    var pageNumber = getIntParam("pageNumber", params, DEFAULT_PAGE_NUM);
    var pageSize = getIntParam("pageSize", params, DEFAULT_PAGE_SIZE);
    removePaginationParams(params);
    var mappedParams = mapParamsKeyword(params, getParamMapping());
    var map = mappedParams.entrySet().stream()
        .collect(Collectors.toMap(entry -> entry.getKey().fullName(), Entry::getValue));
    var specimenPlusOne = elasticRepository.search(map, pageNumber, pageSize);
    return wrapListResponseSearchResults(specimenPlusOne, pageNumber, pageSize, path);
  }

  private void removePaginationParams(MultiValueMap<String, String> params) {
    params.remove("pageSize");
    params.remove("pageNumber");
  }

  private int getIntParam(String paramName, MultiValueMap<String, String> params,
      String defaultValue) {
    var paramValue = params.getOrDefault(paramName, List.of(defaultValue));
    if (paramValue.size() > 1) {
      log.warn("Taking first value for param: {} values: {}", paramName, paramValue);
    }
    try {
      var intValue = Integer.parseInt(paramValue.get(0));
      if (intValue > 0) {
        return intValue;
      } else {
        log.warn("Provided value: {} is a negative value, falling back to default value", intValue);
        return Integer.parseInt(defaultValue);
      }
    } catch (NumberFormatException ex) {
      log.error("Param: {} cannot be parsed to a number, falling back to default", paramName, ex);
      return Integer.parseInt(defaultValue);
    }
  }

  private <T extends MappingTerm> Map<T, List<String>> mapParamsKeyword(
      MultiValueMap<String, String> requestParams,
      Map<String, T> acceptedParams)
      throws UnknownParameterException {
    var mappedParams = new HashMap<T, List<String>>();
    for (var entry : requestParams.entrySet()) {
      var mappedParam = acceptedParams.get(entry.getKey());
      if (mappedParam != null) {
        mappedParams.put(mappedParam, entry.getValue());
      } else {
        throw new UnknownParameterException("Parameter: " + entry.getKey() + " is not recognised");
      }
    }
    return mappedParams;
  }

  public JsonApiWrapper taxonAggregations(MultiValueMap<String, String> params,
      String path) throws UnknownParameterException, IOException {
    var mappedParams = mapParamsKeyword(params, TaxonMappingTerms.getTaxonMapping());
    var aggregateTerm = retrieveTaxRanks(mappedParams);
    var map = mappedParams.entrySet().stream()
        .collect(Collectors.toMap(entry -> entry.getKey().fullName(), Entry::getValue));
    var aggregations = elasticRepository.getAggregations(map, aggregateTerm, true);
    var dataNode = new JsonApiData(String.valueOf(params.hashCode()), AGGREGATIONS_TYPE,
        mapper.valueToTree(aggregations));
    return new JsonApiWrapper(dataNode, new JsonApiLinks(path));
  }

  public JsonApiWrapper aggregations(MultiValueMap<String, String> params, String path)
      throws IOException, UnknownParameterException {
    var mappedParams = mapParamsKeyword(params, getParamMapping());
    var map = mappedParams.entrySet().stream()
        .collect(Collectors.toMap(entry -> entry.getKey().fullName(), Entry::getValue));
    var aggregations = elasticRepository.getAggregations(map,
        DefaultMappingTerms.getAggregationSet(), false);
    var dataNode = new JsonApiData(String.valueOf(params.hashCode()), AGGREGATIONS_TYPE,
        mapper.valueToTree(aggregations));
    return new JsonApiWrapper(dataNode, new JsonApiLinks(path));
  }

  public JsonApiWrapper discipline(String path) throws IOException {
    var disciplineResult = elasticRepository.getAggregation(TOPIC_DISCIPLINE);
    var dataNode = new JsonApiData(String.valueOf(path.hashCode()), AGGREGATIONS_TYPE,
        mapper.valueToTree(disciplineResult.getRight()));
    return new JsonApiWrapper(dataNode,
        new JsonApiLinks(path), new JsonApiMeta(disciplineResult.getLeft()));
  }

  public JsonApiWrapper searchTermValue(String name, String value, String path, boolean sort)
      throws UnknownParameterException, IOException {
    var mappedTerm = getParamMapping().get(name);
    if (mappedTerm != null) {
      var aggregations = elasticRepository.aggregateTermValue(name, mappedTerm.fullName(), value,
          sort);
      var dataNode = new JsonApiData(String.valueOf((name + value).hashCode()), AGGREGATIONS_TYPE,
          mapper.valueToTree(aggregations));
      return new JsonApiWrapper(dataNode, new JsonApiLinks(path));
    } else {
      throw new UnknownParameterException("Parameter: " + name + " is not recognised");
    }
  }

  public JsonApiListResponseWrapper getMass(String id, String path) throws NotFoundException {
    var digitalSpecimen = repository.getLatestSpecimenById(id);
    if (digitalSpecimen != null) {
      var flattenAttributes = flattenAttributes(digitalSpecimen);
      return masService.getMassForObject(flattenAttributes, path);
    }
    log.warn("Specimen {} can not be found. Can not retrieve MASs", id);
    throw new NotFoundException("Unable to find specimen " + id + ". Can not retrieve MASs");

  }

  private JsonNode flattenAttributes(DigitalSpecimen digitalSpecimen) {
    return mapper.convertValue(digitalSpecimen, ObjectNode.class);
  }

  public JsonApiListResponseWrapper scheduleMass(String id, List<MasJobRequest> masRequests,
      String orcid, String path) throws MasSchedulingException {
    return masService.scheduleMas(id, masRequests, orcid, MjrTargetType.DIGITAL_SPECIMEN, path);
  }


}