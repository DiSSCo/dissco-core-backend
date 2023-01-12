package eu.dissco.backend.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.dissco.backend.domain.AnnotationResponse;
import eu.dissco.backend.domain.DigitalSpecimen;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ElasticSearchRepository {

  private static final String INDEX = "new-dissco";
  private static final String FIELD_CREATED = "created";

  private final ElasticsearchClient client;


  public List<DigitalSpecimen> search(String query, int pageNumber, int pageSize)
      throws IOException {
    var offset = 0;
    if (pageNumber > 1) {
      offset = offset + (pageSize * (pageNumber - 1));
    }
    var searchRequest = new SearchRequest.Builder()
        .index(INDEX)
        .q("_exists_:digitalSpecimen.physicalSpecimenId AND " + query)
        .from(offset)
        .size(pageSize)
        .build();
    return client.search(searchRequest, ObjectNode.class).hits().hits().stream()
        .map(Hit::source)
        .map(this::mapToDigitalSpecimen).toList();
  }

  public List<DigitalSpecimen> getLatestSpecimen() throws IOException {
    var searchRequest = new SearchRequest.Builder()
        .index(INDEX)
        .q("_exists_:digitalSpecimen.physicalSpecimenId ")
        .sort(s -> s.field(f -> f.field(FIELD_CREATED).order(SortOrder.Desc)))
        .size(10)
        .build();
    return client.search(searchRequest, ObjectNode.class).hits().hits().stream()
        .map(Hit::source)
        .map(this::mapToDigitalSpecimen).toList();
  }

  public List<AnnotationResponse> getLatestAnnotations (int pageNumber, int pageSize) throws IOException {
    var offset = 0;
    if (pageNumber > 1) {
      offset = offset + (pageSize * (pageNumber - 1));
    }

    var searchRequest = new SearchRequest.Builder()
        .index(INDEX)
        .q("_exists_:annotation.type ")
        .sort(s -> s.field(f -> f.field(FIELD_CREATED).order(SortOrder.Desc)))
        .from(offset)
        .size(pageSize)
        .build();
    return client.search(searchRequest, ObjectNode.class).hits().hits().stream()
        .map(Hit::source)
        .map(this::mapToAnnotationResponse).toList();
  }

  public List<AnnotationResponse> getLatestAnnotation() throws IOException {
    var searchRequest = new SearchRequest.Builder()
        .index(INDEX)
        .q("_exists_:annotation.type ")
        .sort(s -> s.field(f -> f.field(FIELD_CREATED).order(SortOrder.Desc)))
        .size(10)
        .build();
    return client.search(searchRequest, ObjectNode.class).hits().hits().stream()
        .map(Hit::source)
        .map(this::mapToAnnotationResponse).toList();
  }

  private DigitalSpecimen mapToDigitalSpecimen(ObjectNode json) {
    var digitalSpecimen = json.get("digitalSpecimen");
    return new DigitalSpecimen(
        json.get("id").asText(),
        json.get("midsLevel").asInt(),
        json.get("version").asInt(),
        Instant.ofEpochSecond(json.get(FIELD_CREATED).asLong()),
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
        getText(digitalSpecimen, "dwcaId")
    );
  }

  private AnnotationResponse mapToAnnotationResponse(ObjectNode json) {
    var annotation = json.get("annotation");
    return new AnnotationResponse(
        json.get("id").asText(),
        json.get("version").asInt(),
        getText(annotation, "type"),
        getText(annotation, "motivation"),
        annotation.get("target"),
        annotation.get("body"),
        annotation.get("preferenceScore").asInt(),
        getText(annotation, "creator"),
        Instant.ofEpochSecond(annotation.get(FIELD_CREATED).asLong()),
        annotation.get("generator"),
        Instant.ofEpochSecond(annotation.get("generated").asLong()),
        null
    );
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
