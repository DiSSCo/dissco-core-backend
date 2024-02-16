package eu.dissco.backend.repository;

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
import co.elastic.clients.elasticsearch._types.aggregations.TermsAggregation.Builder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.util.NamedValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.dissco.backend.domain.DigitalSpecimenWrapper;
import eu.dissco.backend.domain.MappingTerm;
import eu.dissco.backend.domain.DefaultMappingTerms;
import eu.dissco.backend.domain.annotation.Annotation;
import eu.dissco.backend.exceptions.DiSSCoElasticMappingException;
import eu.dissco.backend.properties.ElasticSearchProperties;
import eu.dissco.backend.schema.DigitalSpecimen;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
@RequiredArgsConstructor
public class ElasticSearchRepository {

  private static final String FIELD_CREATED = "created";
  private static final String FIELD_CREATED_ANNOTATION = "dcterms:created";
  private static final String DIGITAL_SPECIMEN_WRAPPER = "digitalSpecimenWrapper";
  private static final String FIELD_GENERATED = "generated";
  private final ElasticsearchClient client;
  private final ObjectMapper mapper;
  private final ElasticSearchProperties properties;

  private static Builder getTerm(String field, Builder t, boolean alphabetical) {
    var term = t.field(field);
    if (alphabetical) {
      term.order(NamedValue.of("_key", SortOrder.Asc));
    }
    return term;
  }

  private List<Query> generateQueries(Map<String, List<String>> params) {
    var queries = new ArrayList<Query>();
    for (var entry : params.entrySet()) {
      for (var value : entry.getValue()) {
        Query query;
        if (Objects.equals(entry.getKey(), "q")) {
          var sanitisedValue = value.replace("/", "//");
          query = new Query.Builder().queryString(q -> q.query(sanitisedValue)).build();
        } else {
          query = new Query.Builder().term(
              t -> t.field(entry.getKey()).value(value).caseInsensitive(Boolean.TRUE)).build();
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
        .sort(s -> s.field(f -> f.field(FIELD_CREATED_ANNOTATION).order(SortOrder.Desc)))
        .from(offset)
        .size(pageSizePlusOne).build();
    return client.search(searchRequest, ObjectNode.class).hits().hits().stream().map(Hit::source)
        .map(this::mapToAnnotationResponse).toList();
  }

  private Annotation mapToAnnotationResponse(ObjectNode annotationNode) {
    try {
      var annotation = mapper.treeToValue(annotationNode, Annotation.class);
      return annotation.withOdsId(HANDLE_STRING + annotation.getOdsId());
    } catch (JsonProcessingException e) {
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

  public Pair<Long, List<Annotation>> getAnnotationsForCreator(String userId,
      int pageNumber, int pageSize) throws IOException {
    var fieldName = "oa:creator.ods:id";
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
      int pageNumber, int pageSize) throws IOException {
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

  public Map<String, Map<String, Long>> getAggregations(Map<String, List<String>> params,
      Set<MappingTerm> aggregationTerms, boolean isTaxonomyOnly)
      throws IOException {
    var aggregationQueries = new HashMap<String, Aggregation>();
    var queries = generateQueries(params);
    var size = isTaxonomyOnly ? Integer.MAX_VALUE : 10;
    for (var aggregationTerm : aggregationTerms) {
      aggregationQueries.put(aggregationTerm.requestName(),
          AggregationBuilders.terms()
              .field(aggregationTerm.fullName()).size(size).build()._toAggregation());
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
    var mapped = new LinkedHashMap<String, Map<String, Long>>();
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

  public Pair<Long, Map<String, Map<String, Long>>> getAggregation(DefaultMappingTerms mappingTerm)
      throws IOException {
    var aggregationRequest = new SearchRequest.Builder().index(properties.getDigitalSpecimenIndex())
        .trackTotalHits(t -> t.enabled(Boolean.TRUE))
        .aggregations(mappingTerm.requestName(),
            AggregationBuilders.terms().field(mappingTerm.fullName()).build()._toAggregation())
        .size(0)
        .build();
    var aggregation = client.search(aggregationRequest, ObjectNode.class);
    var totalRecords = aggregation.hits().total().value();
    var aggregationResult = collectResult(aggregation.aggregations());
    return Pair.of(totalRecords, aggregationResult);
  }

  public Map<String, Map<String, Long>> aggregateTermValue(String name, String field, String value,
      boolean sort)
      throws IOException {
    var searchQuery = new SearchRequest.Builder().index(properties.getDigitalSpecimenIndex())
        .query(q -> q.prefix(m -> m.field(field).value(value).caseInsensitive(Boolean.TRUE)))
        .aggregations(name,
            agg -> agg.terms(t -> getTerm(field, t, sort)))
        .size(0)
        .build();
    var aggregation = client.search(searchQuery, ObjectNode.class);
    return collectResult(aggregation.aggregations());
  }

}
