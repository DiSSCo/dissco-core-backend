package eu.dissco.backend.repository;

import eu.dissco.backend.properties.CordraProperties;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cnri.cordra.api.CordraClient;
import net.cnri.cordra.api.CordraException;
import net.cnri.cordra.api.CordraObject;
import net.cnri.cordra.api.QueryParams;
import net.cnri.cordra.api.SearchResults;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CordraRepository {

  private final CordraClient cordraClient;
  private final CordraProperties properties;

  public SearchResults<CordraObject> getSpecimen(int pageNumber, int pageSize)
      throws CordraException {
    var queryParams = new QueryParams(pageNumber, pageSize);
    return cordraClient.search("type:\"" + properties.getType() + "\"", queryParams);
  }

  public CordraObject getSpecimenById(String id) throws CordraException {
    return cordraClient.get(id);
  }

  public SearchResults<CordraObject> search(String query, int pageNumber, int pageSize)
      throws CordraException {
    var queryParams = new QueryParams(pageNumber, pageSize);
    var encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
    return cordraClient.search(encodedQuery, queryParams);
  }

}
