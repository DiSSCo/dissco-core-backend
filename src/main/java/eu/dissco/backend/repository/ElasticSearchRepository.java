package eu.dissco.backend.repository;

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
import co.elastic.clients.elasticsearch.core.CountRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.util.NamedValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.dissco.backend.domain.DefaultMappingTerms;
import eu.dissco.backend.domain.MappingTerm;
import eu.dissco.backend.domain.annotation.AnnotationTargetType;
import eu.dissco.backend.domain.annotation.batch.BatchMetadata;
import eu.dissco.backend.exceptions.DiSSCoElasticMappingException;
import eu.dissco.backend.properties.ElasticSearchProperties;
import eu.dissco.backend.schema.Annotation;
import eu.dissco.backend.schema.DigitalSpecimen;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
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

  private static final String FIELD_CREATED = "dcterms:created";
  private static final String FIELD_CREATOR = "dcterms:creator.@id.keyword";
  private static final String FIELD_ID = "dcterms:identifier.keyword";
  private final ElasticsearchClient client;
  private final ObjectMapper mapper;
  private final ElasticSearchProperties properties;

  /* Query Generation */

  private List<Query> generateQueries(Map<String, List<String>> params) {
    var queries = new ArrayList<Query>();
    for (var entry : params.entrySet()) {
      for (var value : entry.getValue()) {
        Query query;
        if (Objects.equals(entry.getKey(), "q")) {
          query = generateStringQuery(value);
        } else {
          if (value.contains("*")) {
            query = generateWildcardQuery(entry.getKey(), value);
          } else {
            query = generateTermQuery(entry.getKey(), value);
          }
        }
        queries.add(query);
      }
    }
    return queries;
  }

  private static Query generateStringQuery(String value) {
    var sanitisedValue = value.replace("/", "//");
    return new Query.Builder().queryString(q -> q.query(sanitisedValue)).build();
  }

  private static Query generateWildcardQuery(String param, String value) {
    return new Query.Builder().wildcard(
        w -> w.field(param).value(value).caseInsensitive(true)
    ).build();
  }

  private static Query generateTermQuery(String param, String value) {
    return new Query.Builder().term(
        t -> t.field(param).value(value)).build();
  }

  /* Searching */

  public Pair<Long, List<Annotation>> getAnnotationsForCreator(String userId,
      int pageNumber, int pageSize) throws IOException {
    var queries = List.of(
        new Query.Builder().match(t -> t.field(FIELD_CREATOR).query(userId)).build());
    var searchResults = cycleThroughItems(pageNumber, pageSize, queries,
        properties.getAnnotationIndex());
    var annotations = searchResults.getRight().stream()
        .map(this::mapToAnnotation)
        .toList();
    return Pair.of(searchResults.getLeft(), annotations);
  }

  public Pair<Long, List<DigitalSpecimen>> getLatestSpecimens(int pageNumber, int pageSize)
      throws IOException {
    var searchResults = cycleThroughItems(pageNumber, pageSize, List.of(),
        properties.getDigitalSpecimenIndex());
    return getSpecimensFromSearchResults(searchResults);
  }

  public Pair<Long, List<DigitalSpecimen>> search(Map<String, List<String>> params,
      int pageNumber, int pageSize) throws IOException {
    var queries = generateQueries(params);
    var searchResults = cycleThroughItems(pageNumber, pageSize, queries,
        properties.getDigitalSpecimenIndex());
    return getSpecimensFromSearchResults(searchResults);
  }

  private Pair<Long, List<ObjectNode>> cycleThroughItems(int pageNumber, int pageSize,
      List<Query> queries, String index) throws IOException {
    var curPage = 1;
    Pair<Long, String> searchAfter = null;
    var totalHits = 0L;
    var pitResponse = client.openPointInTime(op -> op.index(index).keepAlive(t -> t.time("1m")));
    var pitId = pitResponse.id();
    while (curPage <= pageNumber) {
      var searchRequest = buildPaginatedSearchRequest(searchAfter, pageSize, queries,
          curPage == 1, pitId);
      var elasticResults = client.search(searchRequest, ObjectNode.class);
      var objectResults = elasticResults.hits().hits().stream()
          .map(Hit::source).toList();
      if (elasticResults.hits().total() == null) {
        client.closePointInTime(c -> c.id(pitId));
        return Pair.of(0L, objectResults);
      }
      if (curPage == 1) {
        totalHits = elasticResults.hits().total().value();
      }
      if (objectResults.isEmpty() || curPage == pageNumber) {
        client.closePointInTime(c -> c.id(pitId));
        return Pair.of(totalHits, objectResults);
      } else {
        searchAfter = Pair.of(
            mapper.treeToValue(objectResults.getLast().get(FIELD_CREATED), Date.class)
                .getTime(),
            objectResults.getLast().get("dcterms:identifier").asText());
        log.info("Reached page {} of {}", curPage, pageNumber);
        curPage++;
      }
    }
    client.closePointInTime(c -> c.id(pitId));
    return Pair.of(0L, List.of(mapper.createObjectNode()));
  }

  private SearchRequest buildPaginatedSearchRequest(Pair<Long, String> searchAfter, int pageSize,
      List<Query> queries, boolean trackTotalHits, String pitId) {
    var searchRequestBuilder = new SearchRequest.Builder()
        .size(pageSize)
        .sort(s -> s
            .field(f -> f
                .field(FIELD_CREATED)
                .order(SortOrder.Desc)))

        .sort(s -> s
            .field(f -> f
                .field(FIELD_ID) // tie breaker
                .order(SortOrder.Asc)))
        .pit(p -> p.id(pitId).keepAlive(t -> t.time("1m")));
    if (!queries.isEmpty()) {
      searchRequestBuilder.query(
          q -> q.bool(b -> b.must(queries)));
    }
    if (searchAfter != null) {
      searchRequestBuilder
          .searchAfter(sa -> sa.longValue(searchAfter.getLeft()))
          .searchAfter(sa -> sa.stringValue(searchAfter.getRight()));
    }
    if (trackTotalHits) {
      searchRequestBuilder.trackTotalHits(t -> t.enabled(Boolean.TRUE));
    }
    return searchRequestBuilder.build();
  }

  private DigitalSpecimen mapToDigitalSpecimen(ObjectNode json) {
    try {
      return mapper.treeToValue(json, DigitalSpecimen.class);
    } catch (JsonProcessingException e) {
      log.error("Unable to parse digital specimen to json: {}", json);
      throw new DiSSCoElasticMappingException(e);
    }
  }

  private Annotation mapToAnnotation(ObjectNode annotationNode) {
    try {
      return mapper.treeToValue(annotationNode, Annotation.class);
    } catch (JsonProcessingException e) {
      throw new DiSSCoElasticMappingException(e);
    }
  }

  private Pair<Long, List<DigitalSpecimen>> getSpecimensFromSearchResults(
      Pair<Long, List<ObjectNode>> searchResults) {
    var specimens = searchResults.getRight().stream()
        .map(this::mapToDigitalSpecimen)
        .toList();
    return Pair.of(searchResults.getLeft(), specimens);
  }

  /* ELViS */

  public Pair<Long, List<DigitalSpecimen>> elvisSearch(Map<String, List<String>> params,
      int pageNumber, int pageSize) throws IOException {
    var offset = getOffset(pageNumber, pageSize);
    var queries = generateQueries(params);
    var searchRequest = new SearchRequest.Builder().index(properties.getDigitalSpecimenIndex())
        .query(
            q -> q.bool(b -> b.should(queries).minimumShouldMatch("1")))
        .trackTotalHits(t -> t.enabled(Boolean.TRUE))
        .from(offset)
        .size(pageSize).build();
    return getDigitalSpecimenSearchResults(searchRequest);
  }

  private Pair<Long, List<DigitalSpecimen>> getDigitalSpecimenSearchResults(
      SearchRequest searchRequest) throws IOException {
    var searchResult = client.search(searchRequest, ObjectNode.class);
    var specimens = searchResult.hits().hits().stream()
        .map(Hit::source)
        .map(this::mapToDigitalSpecimen).toList();
    if (searchResult.hits().total() == null) {
      return Pair.of(0L, specimens);
    } else {
      var totalHits = searchResult.hits().total().value();
      return Pair.of(totalHits, specimens);
    }
  }

  /* Aggregations */

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

  private static Builder getTerm(String field, Builder t, boolean sort) {
    var term = t.field(field);
    if (sort) {
      term.order(NamedValue.of("_key", SortOrder.Asc));
    }
    return term;
  }

  /* Batch Annotations */

  public long getCountForBatchAnnotations(BatchMetadata batchMetadata,
      AnnotationTargetType targetType)
      throws IOException {
    var index =
        targetType.equals(AnnotationTargetType.DIGITAL_SPECIMEN)
            ? properties.getDigitalSpecimenIndex()
            : properties.getDigitalMediaObjectIndex();
    var query = generateBatchAnnotationQuery(batchMetadata);
    var countRequest = new CountRequest.Builder()
        .index(index)
        .query(
            q -> q.bool(b -> b.must(query)))
        .build();
    return client
        .count(countRequest)
        .count();
  }

  private List<Query> generateBatchAnnotationQuery(BatchMetadata batchMetadata) {
    var qList = new ArrayList<Query>();
    for (var searchParam : batchMetadata.getSearchParams()) {
      var key = searchParam.inputField().replaceAll("\\[[^]]*]", "") + ".keyword";
      var val = searchParam.inputValue();
      if (!val.isBlank()) {
        qList.add(
            new Query.Builder().term(t -> t.field(key).value(val).caseInsensitive(true)).build());
      } else {
        qList.add(
            new Query.Builder().bool(b -> b.mustNot(q -> q.exists(e -> e.field(key)))).build());
      }
    }
    return qList;
  }

}
