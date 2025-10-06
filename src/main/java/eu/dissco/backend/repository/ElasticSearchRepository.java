package eu.dissco.backend.repository;

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
import co.elastic.clients.elasticsearch.core.CountRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.util.NamedValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.dissco.backend.domain.annotation.AnnotationTargetType;
import eu.dissco.backend.domain.annotation.batch.BatchMetadata;
import eu.dissco.backend.domain.elastic.DefaultMappingTerms;
import eu.dissco.backend.domain.elastic.MappingTerm;
import eu.dissco.backend.domain.elastic.MissingMappingTerms;
import eu.dissco.backend.exceptions.DiSSCoElasticMappingException;
import eu.dissco.backend.properties.ElasticSearchProperties;
import eu.dissco.backend.schema.Annotation;
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

  private static final String FIELD_CREATED = "dcterms:created";
  private final ElasticsearchClient client;
  private final ObjectMapper mapper;
  private final ElasticSearchProperties properties;
  private static final Set<MappingTerm> MISSING_MAPPING_TERMS = Set.of(
      MissingMappingTerms.values());

  private static Builder getTerm(String field, Builder t, boolean sort) {
    var term = t.field(field);
    if (sort) {
      term.order(NamedValue.of("_key", SortOrder.Asc));
    }
    return term;
  }

  private <T extends MappingTerm> List<Query> generateQueries(Map<T, List<String>> params) {
    var queries = new ArrayList<Query>();
    for (var entry : params.entrySet()) {
      for (var value : entry.getValue()) {
        Query query;
        if (MISSING_MAPPING_TERMS.contains(entry.getKey())) {
          query = generateExistsQuery(entry.getKey().fullName(),
              Boolean.parseBoolean(value));
        } else if (Objects.equals(entry.getKey(), DefaultMappingTerms.QUERY)) {
          query = generateStringQuery(value);
        } else {
          if (value.contains("*")) {
            query = generateWildcardQuery(entry.getKey().fullName(), value);
          } else {
            query = generateTermQuery(entry.getKey().fullName(), value);
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

  private static Query generateExistsQuery(String param, boolean exists) {
    return exists ? new Query.Builder().bool(b -> b.must(m -> m.exists(f -> f.field(param))))
        .build() :
        new Query.Builder().bool(b -> b.mustNot(m -> m.exists(f -> f.field(param)))).build();
  }

  private static Query generateTermQuery(String param, String value) {
    return new Query.Builder().term(
        t -> t.field(param).value(value)).build();
  }

  public Pair<Long, List<DigitalSpecimen>> getLatestSpecimen(int pageNumber, int pageSize)
      throws IOException {
    var offset = getOffset(pageNumber, pageSize);
    var pageSizePlusOne = pageSize + ONE_TO_CHECK_NEXT;
    var searchRequest = new SearchRequest.Builder().index(properties.getDigitalSpecimenIndex())
        .sort(s -> s.field(f -> f.field(FIELD_CREATED).order(SortOrder.Desc)))
        .from(offset)
        .size(pageSizePlusOne).build();
    return getDigitalSpecimenSearchResults(searchRequest);
  }

  private Annotation mapToAnnotationResponse(ObjectNode annotationNode) {
    try {
      return mapper.treeToValue(annotationNode, Annotation.class);
    } catch (JsonProcessingException e) {
      throw new DiSSCoElasticMappingException(e);
    }
  }

  private DigitalSpecimen mapToDigitalSpecimen(ObjectNode json) {
    try {
      return mapper.treeToValue(json, DigitalSpecimen.class);
    } catch (JsonProcessingException e) {
      log.error("Unable to parse digital specimen to json: {}", json);
      throw new DiSSCoElasticMappingException(e);
    }
  }

  public Pair<Long, List<Annotation>> getAnnotationsForCreator(String userId,
      int pageNumber, int pageSize) throws IOException {
    var fieldName = "dcterms:creator.@id.keyword";
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

  public <T extends MappingTerm> Pair<Long, List<DigitalSpecimen>> search(
      Map<T, List<String>> params,
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

  public <T extends MappingTerm> Pair<Long, List<DigitalSpecimen>> elvisSearch(
      Map<T, List<String>> params,
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

  public Pair<Long, List<DigitalSpecimen>> getSpecimens(int pageNumber, int pageSize)
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

  public <T extends MappingTerm> Map<String, Map<String, Long>> getAggregations(
      Map<T, List<String>> params,
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
    var missingDataAggregations = getMissingDataAggregation();
    return collectResult(aggregations, missingDataAggregations);
  }

  private Map<String, Long> getMissingDataAggregation() throws IOException {
    var result = new HashMap<String, Long>();
    for (var term : MissingMappingTerms.values()) {
      var query = generateExistsQuery(term.fullName(), false);
      var countRequest = new CountRequest.Builder()
          .index(properties.getDigitalSpecimenIndex())
          .query(query)
          .build();
      var count = client.count(countRequest).count();
      result.put(term.requestName().replace("has", "no"), count);
    }
    return result;
  }

  private Map<String, Map<String, Long>> collectResult(
      Map<String, Aggregate> aggregations, Map<String, Long> missingDataAggregation)
      throws IOException {
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
    if (!missingDataAggregation.isEmpty()) {
      mapped.put("missingData", getMissingDataAggregation());
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
    var missingDataAggregation = getMissingDataAggregation();
    var aggregationResult = collectResult(aggregation.aggregations(), missingDataAggregation);
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
    return collectResult(aggregation.aggregations(), Map.of());
  }

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
