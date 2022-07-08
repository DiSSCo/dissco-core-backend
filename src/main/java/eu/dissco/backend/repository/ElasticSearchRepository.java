package eu.dissco.backend.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.search.Hit;
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
        .index("dissco")
        .q(query)
        .from(offset)
        .size(pageSize)
        .build();
    return client.search(searchRequest, DigitalSpecimen.class).hits().hits().stream()
        .map(Hit::source).toList();
  }
}
