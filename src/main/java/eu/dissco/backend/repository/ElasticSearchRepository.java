package eu.dissco.backend.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.dissco.backend.domain.DigitalSpecimen;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ElasticSearchRepository {

  private final ElasticsearchClient client;

  public List<DigitalSpecimen> search(String query, int pageNumber, int pageSize)
      throws IOException {
    var offset = 0;
    if (pageNumber > 1) {
      offset = offset + (pageSize * (pageNumber - 1));
    }
    SearchRequest searchRequest = new SearchRequest.Builder()
        .index("new-dissco")
        .q("digitalSpecimen :* and " + query)
        .from(offset)
        .size(pageSize)
        .build();
    return client.search(searchRequest, ObjectNode.class).hits().hits().stream().map(Hit::source)
        .map(this::mapToDigitalSpecimen).toList();
  }

  private DigitalSpecimen mapToDigitalSpecimen(ObjectNode json) {
    var digitalSpecimen = json.get("digitalSpecimen");
    return new DigitalSpecimen(
        json.get("id").asText(),
        json.get("midsLevel").asInt(),
        json.get("version").asInt(),
        digitalSpecimen.get("type").asText(),
        digitalSpecimen.get("physicalSpecimenId").asText(),
        digitalSpecimen.get("physicalSpecimenIdType").asText(),
        digitalSpecimen.get("specimenName").asText(),
        digitalSpecimen.get("organizationId").asText(),
        digitalSpecimen.get("datasetId").asText(),
        digitalSpecimen.get("physicalSpecimenCollection").asText(),
        digitalSpecimen.get("sourceSystemId").asText(),
        digitalSpecimen.get("data"),
        digitalSpecimen.get("originalData"),
        digitalSpecimen.get("dwcaId").asText()
    );
  }
}
