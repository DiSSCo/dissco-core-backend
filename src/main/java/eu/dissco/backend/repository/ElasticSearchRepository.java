package eu.dissco.backend.repository;

import static eu.dissco.backend.repository.RepositoryUtils.getOffset;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.dissco.backend.domain.AnnotationResponse;
import eu.dissco.backend.domain.DigitalSpecimen;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ElasticSearchRepository {

  private static final String INDEX = "new-dissco";
  private static final String ANNOTATION_EXISTS_QUERY = "_exists_:annotation.type ";
  private static final String FIELD_CREATED = "created";
  private static final String FIELD_GENERATED = "generated";
  private final ElasticsearchClient client;

  public List<JsonApiData> search(String query, int pageNumber, int pageSize)
      throws IOException {
    query = query.replace("/", "//");

    var offset = getOffset(pageNumber, pageSize);

    var searchRequest = new SearchRequest.Builder().index(INDEX)
        .q("_exists_:digitalSpecimen.physicalSpecimenId AND " + query).from(offset).size(pageSize)
        .build();
    return client.search(searchRequest, ObjectNode.class).hits().hits().stream().map(Hit::source)
        .map(this::mapDigitalSpecimenToJsonApiData).toList();
  }

  public List<JsonApiData> getLatestSpecimen(int pageNumber, int pageSize) throws IOException {
    var offset = getOffset(pageNumber, pageSize);
    var searchRequest = new SearchRequest.Builder().index(INDEX)
        .q("_exists_:digitalSpecimen.physicalSpecimenId ")
        .sort(s -> s.field(f -> f.field(FIELD_CREATED).order(SortOrder.Desc))).from(offset)
        .size(pageSize).build();
    return client.search(searchRequest, ObjectNode.class).hits().hits().stream().map(Hit::source)
        .map(this::mapDigitalSpecimenToJsonApiData).toList();
  }

  public List<AnnotationResponse> getLatestAnnotations(int pageNumber, int pageSize)
      throws IOException {
    var offset = getOffset(pageNumber, pageSize);

    var searchRequest = new SearchRequest.Builder().index(INDEX).q(ANNOTATION_EXISTS_QUERY)
        .sort(s -> s.field(f -> f.field(FIELD_CREATED).order(SortOrder.Desc))).from(offset)
        .size(pageSize).build();
    return client.search(searchRequest, ObjectNode.class).hits().hits().stream().map(Hit::source)
        .map(this::mapToAnnotationResponse).toList();
  }

  private JsonApiData mapDigitalSpecimenToJsonApiData(ObjectNode json){
    var digitalSpecimen = (ObjectNode) json.get("digitalSpecimen");
    return  new JsonApiData(digitalSpecimen.get("id").asText(), digitalSpecimen.get("type").asText(), digitalSpecimen );
  }

  private AnnotationResponse mapToAnnotationResponse(ObjectNode json) {
    var annotation = json.get("annotation");
    return new AnnotationResponse(json.get("id").asText(), json.get("version").asInt(),
        getText(annotation, "type"), getText(annotation, "motivation"), annotation.get("target"),
        annotation.get("body"), annotation.get("preferenceScore").asInt(),
        getText(annotation, "creator"),
        Instant.ofEpochSecond(annotation.get(FIELD_CREATED).asLong()), annotation.get("generator"),
        Instant.ofEpochSecond(annotation.get(FIELD_GENERATED).asLong()), null);
  }

  private DigitalSpecimen mapToDigitalSpecimen(ObjectNode json) {

    var digitalSpecimen = json.get("digitalSpecimen");
    return new DigitalSpecimen(json.get("id").asText(),
        json.get("midsLevel").asInt(),
        json.get("version").asInt(), Instant.ofEpochSecond(json.get(FIELD_CREATED).asLong()),
        getText(digitalSpecimen, "type"),
        getText(digitalSpecimen, "physicalSpecimenId"),
        getText(digitalSpecimen, "physicalSpecimenIdType"),
        getText(digitalSpecimen, "specimenName"),
        getText(digitalSpecimen, "organizationId"),
        getText(digitalSpecimen, "datasetId"),
        getText(digitalSpecimen, "physicalSpecimenCollection"),
        getText(digitalSpecimen, "sourceSystemId"),
        digitalSpecimen.get("data"),
        digitalSpecimen.get("originalData"),
        getText(digitalSpecimen, "dwcaId"));
  }

  private String getText(JsonNode digitalSpecimen, String element) {
    var jsonNode = digitalSpecimen.get(element);
    if (jsonNode != null) {
      return jsonNode.asText();
    } else {
      return null;
    }
  }
}
