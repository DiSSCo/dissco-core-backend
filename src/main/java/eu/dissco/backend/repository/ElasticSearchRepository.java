package eu.dissco.backend.repository;

import static eu.dissco.backend.repository.RepositoryUtils.getOffset;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

  public List<JsonApiData> getLatestAnnotations(int pageNumber, int pageSize)
      throws IOException {
    var offset = getOffset(pageNumber, pageSize);

    var searchRequest = new SearchRequest.Builder().index(INDEX).q(ANNOTATION_EXISTS_QUERY)
        .sort(s -> s.field(f -> f.field(FIELD_CREATED).order(SortOrder.Desc))).from(offset)
        .size(pageSize).build();
    return client.search(searchRequest, ObjectNode.class).hits().hits().stream().map(Hit::source)
        .map(this::mapAnnotationToJsonApiData).toList();
  }

  private JsonApiData mapDigitalSpecimenToJsonApiData(ObjectNode json){
    var digitalSpecimen = (ObjectNode) json.get("digitalSpecimen");
    return  new JsonApiData(digitalSpecimen.get("id").asText(), digitalSpecimen.get("type").asText(), digitalSpecimen );
  }

  private JsonApiData mapAnnotationToJsonApiData(ObjectNode json) {
    ObjectNode annotation = (ObjectNode) json.get("annotation");
    var created = parseDate(annotation.get(FIELD_CREATED));
    var generated = parseDate(annotation.get(FIELD_GENERATED));
    annotation.put("id", json.get("id").asText());
    annotation.put(FIELD_CREATED, String.valueOf(created));
    annotation.put(FIELD_GENERATED, String.valueOf(generated));
    return new JsonApiData(json.get("id").asText(), annotation.get("type").asText(), annotation);
  }

  private Instant parseDate(JsonNode created){
    if (created.asLong() > 0){
      return Instant.ofEpochSecond(created.asLong());
    }
    return Instant.parse(created.asText());
  }

}
