package eu.dissco.backend.repository;

import static eu.dissco.backend.repository.RepositoryUtils.HANDLE_STRING;
import static eu.dissco.backend.repository.RepositoryUtils.ONE_TO_CHECK_NEXT;
import static eu.dissco.backend.repository.RepositoryUtils.getOffset;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.dissco.backend.domain.annotation.Annotation;
import eu.dissco.backend.exceptions.DiSSCoElasticMappingException;
import eu.dissco.backend.properties.ElasticSearchProperties;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
@RequiredArgsConstructor
public class ElasticSearchAnnotationRepository {

  private static final String FIELD_CREATED_ANNOTATION = "dcterms:created";
  private final ElasticsearchClient client;
  private final ObjectMapper mapper;
  private final ElasticSearchProperties properties;

  public List<Annotation> getLatestAnnotations(int pageNumber, int pageSize)
      throws IOException {
    var offset = getOffset(pageNumber, pageSize);
    var pageSizePlusOne = pageSize + ONE_TO_CHECK_NEXT;
    var searchRequest = new SearchRequest.Builder()
        .index(properties.getAnnotationIndex())
        .sort(s -> s.field(f -> f.field(FIELD_CREATED_ANNOTATION).order(SortOrder.Desc))).from(offset)
        .size(pageSizePlusOne).build();
    return client.search(searchRequest, ObjectNode.class).hits().hits().stream().map(Hit::source)
        .map(this::mapToAnnotationResponse).toList();
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

  private Annotation mapToAnnotationResponse(ObjectNode annotationNode) {
    try {
      var annotation = mapper.treeToValue(annotationNode, Annotation.class);
      return annotation.withOdsId(HANDLE_STRING + annotation.getOdsId());
    } catch (JsonProcessingException e) {
      throw new DiSSCoElasticMappingException(e);
    }
  }


}
