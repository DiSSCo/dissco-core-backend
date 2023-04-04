package eu.dissco.backend.repository;

import static eu.dissco.backend.domain.MappingTerms.aggregationList;
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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.dissco.backend.domain.AnnotationResponse;
import eu.dissco.backend.domain.DigitalSpecimen;
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
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
@RequiredArgsConstructor
public class ElasticSearchRepository {

  private static final String DIGITAL_SPECIMEN_INDEX = "digital-specimen";
  private static final String ANNOTATION_INDEX = "annotation";
  private static final String FIELD_CREATED = "created";
  private static final String FIELD_GENERATED = "generated";
  private final ElasticsearchClient client;

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

  public List<DigitalSpecimen> getLatestSpecimen(int pageNumber, int pageSize) throws IOException {
    var offset = getOffset(pageNumber, pageSize);
    var searchRequest = new SearchRequest.Builder().index(DIGITAL_SPECIMEN_INDEX)
        .sort(s -> s.field(f -> f.field(FIELD_CREATED).order(SortOrder.Desc))).from(offset)
        .size(pageSize).build();
    return client.search(searchRequest, ObjectNode.class).hits().hits().stream().map(Hit::source)
        .map(this::mapToDigitalSpecimen).toList();
  }

  public List<AnnotationResponse> getLatestAnnotations(int pageNumber, int pageSize)
      throws IOException {
    var offset = getOffset(pageNumber, pageSize);

    var searchRequest = new SearchRequest.Builder()
        .index(ANNOTATION_INDEX)
        .sort(s -> s.field(f -> f.field(FIELD_CREATED).order(SortOrder.Desc))).from(offset)
        .size(pageSize).build();
    return client.search(searchRequest, ObjectNode.class).hits().hits().stream().map(Hit::source)
        .map(this::mapToAnnotationResponse).toList();
  }

  private AnnotationResponse mapToAnnotationResponse(ObjectNode json) {
    var annotation = json.get("annotation");
    var createdOn = parseDate(annotation.get(FIELD_CREATED));
    var generatedOn = parseDate(annotation.get(FIELD_GENERATED));
    return new AnnotationResponse(json.get("id").asText(), json.get("version").asInt(),
        getText(annotation, "type"), getText(annotation, "motivation"), annotation.get("target"),
        annotation.get("body"), annotation.get("preferenceScore").asInt(),
        getText(annotation, "creator"), createdOn, annotation.get("generator"), generatedOn, null);
  }

  private DigitalSpecimen mapToDigitalSpecimen(ObjectNode json) {
    var digitalSpecimen = json.get("digitalSpecimen");
    var attributes = digitalSpecimen.get("ods:attributes");
    var createdOn = parseDate(json.get(FIELD_CREATED));
    return new DigitalSpecimen(
        json.get("id").asText(),
        json.get("midsLevel").asInt(),
        json.get("version").asInt(),
        createdOn,
        getText(digitalSpecimen, "ods:type"),
        getText(digitalSpecimen, "ods:physicalSpecimenId"),
        getText(attributes, "ods:physicalSpecimenIdType"),
        getText(attributes, "ods:specimenName"),
        getText(attributes, "ods:organisationId"),
        getText(attributes, "ods:datasetId"),
        getText(attributes, "ods:physicalSpecimenCollection"),
        getText(attributes, "ods:sourceSystemId"),
        attributes,
        digitalSpecimen.get("ods:originalAttributes"),
        getText(attributes, "dwca:id"));
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

  private String getText(JsonNode digitalSpecimen, String element) {
    var jsonNode = digitalSpecimen.get(element);
    if (jsonNode != null) {
      return jsonNode.asText();
    } else {
      return null;
    }
  }

  public List<DigitalSpecimen> search(Map<String, List<String>> params, int pageNumber,
      int pageSize)
      throws IOException {
    var offset = getOffset(pageNumber, pageSize);
    var queries = generateQueries(params);
    var searchRequest = new SearchRequest.Builder().index(DIGITAL_SPECIMEN_INDEX)
        .query(
            q -> q.bool(b -> b.should(queries).minimumShouldMatch(String.valueOf(params.size()))))
        .from(offset)
        .size(pageSize).build();
    return client.search(searchRequest, ObjectNode.class).hits().hits().stream().map(Hit::source)
        .map(this::mapToDigitalSpecimen).toList();
  }

  public Map<String, Map<String, Long>> getAggregations(Map<String, List<String>> params) throws IOException {
    var aggregationQueries = new HashMap<String, Aggregation>();
    var queries = generateQueries(params);
    for (var aggregationTerm : aggregationList) {
      aggregationQueries.put(aggregationTerm.getName(), AggregationBuilders.terms()
          .field(aggregationTerm.getFullName()).build()._toAggregation());
    }
    var aggregationRequest = new SearchRequest.Builder().index(DIGITAL_SPECIMEN_INDEX)
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
          aggregation.put(stringTermsBucket.key(), stringTermsBucket.docCount());
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
}
