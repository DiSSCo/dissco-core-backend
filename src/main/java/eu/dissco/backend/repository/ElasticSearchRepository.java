package eu.dissco.backend.repository;

import static eu.dissco.backend.domain.MappingTerms.getAggregationList;
import static eu.dissco.backend.repository.RepositoryUtils.DOI_STRING;
import static eu.dissco.backend.repository.RepositoryUtils.HANDLE_STRING;
import static eu.dissco.backend.repository.RepositoryUtils.ONE_TO_CHECK_NEXT;
import static eu.dissco.backend.repository.RepositoryUtils.getOffset;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.AggregationBuilders;
import co.elastic.clients.elasticsearch._types.aggregations.LongTermsAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.LongTermsBucket;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.dissco.backend.domain.DigitalSpecimenWrapper;
import eu.dissco.backend.domain.MappingTerms;
import eu.dissco.backend.domain.annotation.AggregateRating;
import eu.dissco.backend.domain.annotation.Annotation;
import eu.dissco.backend.domain.annotation.Body;
import eu.dissco.backend.domain.annotation.Creator;
import eu.dissco.backend.domain.annotation.Generator;
import eu.dissco.backend.domain.annotation.Motivation;
import eu.dissco.backend.domain.annotation.Target;
import eu.dissco.backend.exceptions.DiSSCoElasticMappingException;
import eu.dissco.backend.properties.ElasticSearchProperties;
import eu.dissco.backend.schema.DigitalSpecimen;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
@RequiredArgsConstructor
public class ElasticSearchRepository {

  private static final String FIELD_CREATED = "created";
  private static final String DIGITAL_SPECIMEN_WRAPPER = "digitalSpecimenWrapper";
  private static final String FIELD_GENERATED = "generated";
  private final ElasticsearchClient client;
  private final ObjectMapper mapper;
  private final ElasticSearchProperties properties;

  private List<Query> generateQueries(Map<String, List<String>> params) {
    var queries = new ArrayList<Query>();
    for (var entry : params.entrySet()) {
      for (var value : entry.getValue()) {
        Query query;
        if (Objects.equals(entry.getKey(), "q")) {
          var sanitisedValue = value.replace("/", "//");
          query = new Query.Builder().queryString(q -> q.query(sanitisedValue)).build();
        } else {
          query = new Query.Builder().term(t -> t.field(entry.getKey()).value(value)).build();
        }
        queries.add(query);
      }
    }
    return queries;
  }

  public Pair<Long, List<DigitalSpecimenWrapper>> getLatestSpecimen(int pageNumber, int pageSize)
      throws IOException {
    var offset = getOffset(pageNumber, pageSize);
    var pageSizePlusOne = pageSize + ONE_TO_CHECK_NEXT;
    var searchRequest = new SearchRequest.Builder().index(properties.getDigitalSpecimenIndex())
        .sort(s -> s.field(f -> f.field(FIELD_CREATED).order(SortOrder.Desc)))
        .from(offset)
        .size(pageSizePlusOne).build();
    return getDigitalSpecimenSearchResults(searchRequest);
  }

  public List<Annotation> getLatestAnnotations(int pageNumber, int pageSize)
      throws IOException {
    var offset = getOffset(pageNumber, pageSize);
    var pageSizePlusOne = pageSize + ONE_TO_CHECK_NEXT;
    var searchRequest = new SearchRequest.Builder()
        .index(properties.getAnnotationIndex())
        .sort(s -> s.field(f -> f.field(FIELD_CREATED).order(SortOrder.Desc))).from(offset)
        .size(pageSizePlusOne).build();
    return client.search(searchRequest, ObjectNode.class).hits().hits().stream().map(Hit::source)
        .map(this::mapToAnnotationResponse).toList();
  }

  private Annotation mapToAnnotationResponse(ObjectNode json) {
    var annotation = json.get("annotation");
    var createdOn = parseDate(annotation.get(FIELD_CREATED));
    var generatedOn = parseDate(annotation.get(FIELD_GENERATED));
    AggregateRating aggregateRating = null;
    try {
      if (annotation.get("ods:aggregateRating") != null) {
        aggregateRating = mapper.treeToValue(annotation.get("ods:aggregateRating"),
            AggregateRating.class);
      }
      return new Annotation()
          .withOdsId(HANDLE_STRING + annotation.get("ods:id").asText())
          .withRdfType(annotation.get("rdf:type").asText())
          .withOdsVersion(annotation.get("ods:version").asInt())
          .withOaMotivation(mapper.treeToValue(json.get("oa:motivation"), Motivation.class))
          .withOaMotivatedBy(getText(annotation, "oa:motivatedBy"))
          .withOaTarget(mapper.treeToValue(json.get("oa:target"), Target.class))
          .withOaBody(mapper.treeToValue(json.get("oa:body"), Body.class))
          .withOaCreator(mapper.treeToValue(json.get("oa:creator"), Creator.class))
          .withDcTermsCreated(createdOn)
          .withOdsDeletedOn(null)
          .withAsGenerator(mapper.treeToValue(json.get("as:generator"), Generator.class))
          .withOaGenerated(generatedOn)
          .withOdsAggregateRating(aggregateRating);
    } catch (JsonProcessingException e){
      throw new DiSSCoElasticMappingException(e);
    }


  }

  private DigitalSpecimenWrapper mapToDigitalSpecimenWrapper(ObjectNode json) {
    try {
      var digitalSpecimenWrapper = mapper.treeToValue(
          json.get(DIGITAL_SPECIMEN_WRAPPER).get("ods:attributes"),
          DigitalSpecimen.class);
      return new DigitalSpecimenWrapper(
          digitalSpecimenWrapper.withOdsId(DOI_STRING + json.get("id").asText())
              .withOdsType(json.get(DIGITAL_SPECIMEN_WRAPPER).get("ods:type").asText())
              .withOdsMidsLevel(json.get("midsLevel").asInt())
              .withOdsCreated(json.get(FIELD_CREATED).asText())
              .withOdsVersion(json.get("version").asInt()),
          json.get(DIGITAL_SPECIMEN_WRAPPER).get("ods:originalAttributes"));
    } catch (JsonProcessingException e) {
      log.error("Unable to parse digital specimen to json: {}", json);
      throw new DiSSCoElasticMappingException(e);
    }
  }

  private Instant parseDate(JsonNode instantNode) {
    if (instantNode.isTextual()) {
      return Instant.parse(instantNode.asText());
    } else if (instantNode.isDouble()) {
      return Instant.ofEpochSecond((long) instantNode.asDouble());
    }
    log.error("Cannot parse timestamp of: {}", instantNode);
    return null;
  }

  private String getText(JsonNode annotation, String element) {
    var jsonNode = annotation.get(element);
    if (jsonNode != null) {
      return jsonNode.asText();
    } else {
      return null;
    }
  }

  public Pair<Long, List<Annotation>> getAnnotationsForCreator(String userId,
      int pageNumber, int pageSize) throws IOException {
    var fieldName = "annotation.creator";
    var offset = getOffset(pageNumber, pageSize);
    var pageSizePlusOne = pageSize + ONE_TO_CHECK_NEXT;

    var searchRequest = SearchRequest.of(sr ->
        sr.index(properties.getAnnotationIndex())
            .query(q -> q
                .match(t -> t
                    .field(fieldName)
                    .query(userId)))
            .trackTotalHits(t -> t.enabled(Boolean.TRUE))
            .from(offset)
            .size(pageSizePlusOne));
    var searchResult = client.search(searchRequest, ObjectNode.class);
    if (searchResult.hits().total() != null) {
      var totalHits = searchResult.hits().total().value();
      var annotations = searchResult.hits().hits().stream().map(Hit::source)
          .map(this::mapToAnnotationResponse).toList();
      return Pair.of(totalHits, annotations);
    }
    return Pair.of(0L, new ArrayList<>());
  }

  public Pair<Long, List<DigitalSpecimenWrapper>> search(Map<String, List<String>> params,
      int pageNumber,
      int pageSize)
      throws IOException {
    var offset = getOffset(pageNumber, pageSize);
    var pageSizePlusOne = pageSize + ONE_TO_CHECK_NEXT;
    var queries = generateQueries(params);
    var searchRequest = new SearchRequest.Builder().index(properties.getDigitalSpecimenIndex())
        .query(
            q -> q.bool(b -> b.should(queries).minimumShouldMatch(String.valueOf(params.size()))))
        .trackTotalHits(t -> t.enabled(Boolean.TRUE))
        .from(offset)
        .size(pageSizePlusOne).build();
    return getDigitalSpecimenSearchResults(searchRequest);
  }

  public Pair<Long, List<DigitalSpecimenWrapper>> getSpecimens(int pageNumber, int pageSize)
      throws IOException {
    var offset = getOffset(pageNumber, pageSize);
    var pageSizePlusOne = pageSize + ONE_TO_CHECK_NEXT;
    var searchRequest = new SearchRequest.Builder()
        .index(properties.getDigitalSpecimenIndex())
        .trackTotalHits(t -> t.enabled(Boolean.TRUE))
        .from(offset)
        .size(pageSizePlusOne)
        .build();
    return getDigitalSpecimenSearchResults(searchRequest);
  }

  private Pair<Long, List<DigitalSpecimenWrapper>> getDigitalSpecimenSearchResults(
      SearchRequest searchRequest) throws IOException {
    var searchResult = client.search(searchRequest, ObjectNode.class);
    var specimens = searchResult.hits().hits().stream()
        .map(Hit::source)
        .map(this::mapToDigitalSpecimenWrapper).toList();
    if (searchResult.hits().total() == null) {
      return Pair.of(0L, specimens);
    } else {
      var totalHits = searchResult.hits().total().value();
      return Pair.of(totalHits, specimens);
    }
  }

  public Map<String, Map<String, Long>> getAggregations(Map<String, List<String>> params)
      throws IOException {
    var aggregationQueries = new HashMap<String, Aggregation>();
    var queries = generateQueries(params);
    for (var aggregationTerm : getAggregationList()) {
      aggregationQueries.put(aggregationTerm.getName(), AggregationBuilders.terms()
          .field(aggregationTerm.getFullName()).build()._toAggregation());
    }
    var aggregationRequest = new SearchRequest.Builder().index(properties.getDigitalSpecimenIndex())
        .query(
            q -> q.bool(b -> b.should(queries).minimumShouldMatch(String.valueOf(params.size()))))
        .aggregations(aggregationQueries).build();
    var aggregations = client.search(aggregationRequest, ObjectNode.class).aggregations();
    return collectResult(aggregations);
  }

  private Map<String, Map<String, Long>> collectResult(
      Map<String, Aggregate> aggregations) {
    var mapped = new HashMap<String, Map<String, Long>>();
    for (var entry : aggregations.entrySet()) {
      var aggregation = new LinkedHashMap<String, Long>();
      if (entry.getValue()._get() instanceof StringTermsAggregate value) {
        for (StringTermsBucket stringTermsBucket : value.buckets().array()) {
          aggregation.put(stringTermsBucket.key().stringValue(), stringTermsBucket.docCount());
        }
      } else if (entry.getValue()._get() instanceof LongTermsAggregate value) {
        for (LongTermsBucket stringTermsBucket : value.buckets().array()) {
          aggregation.put(String.valueOf(stringTermsBucket.key()), stringTermsBucket.docCount());
        }
      }
      mapped.put(entry.getKey(), aggregation);
    }
    return mapped;
  }

  public Pair<Long, Map<String, Map<String, Long>>> getAggregation(MappingTerms mappingTerm)
      throws IOException {
    var aggregationRequest = new SearchRequest.Builder().index(properties.getDigitalSpecimenIndex())
        .trackTotalHits(t -> t.enabled(Boolean.TRUE))
        .aggregations(mappingTerm.getName(),
            AggregationBuilders.terms().field(mappingTerm.getFullName()).build()._toAggregation())
        .size(0)
        .build();
    var aggregation = client.search(aggregationRequest, ObjectNode.class);
    var totalRecords = aggregation.hits().total().value();
    var aggregationResult = collectResult(aggregation.aggregations());
    return Pair.of(totalRecords, aggregationResult);
  }

  public Map<String, Map<String, Long>> searchTermValue(String name, String field, String value)
      throws IOException {
    var searchQuery = new SearchRequest.Builder().index(properties.getDigitalSpecimenIndex())
        .query(q -> q.prefix(m -> m.field(field).value(value)))
        .aggregations(name, agg -> agg.terms(t -> t.field(field)))
        .size(0)
        .build();
    var aggregation = client.search(searchQuery, ObjectNode.class);
    return collectResult(aggregation.aggregations());
  }
}
