package eu.dissco.backend.repository;

import com.google.gson.JsonObject;
import eu.dissco.backend.domain.OrganisationTuple;
import eu.dissco.backend.properties.CordraProperties;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
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

  public List<String> getOrganisationNames() throws CordraException {
    return getOrganisations().stream().map(this::mapOrganisationToName).toList();
  }

  public List<OrganisationTuple> getOrganisationTuple() throws CordraException {
    return getOrganisations().stream().map(this::mapOrganisationToTuple).toList();
  }

  private SearchResults<CordraObject> getOrganisations() throws CordraException {
    return cordraClient.search("type:\"" + properties.getOrganisationType() + "\"");
  }

  private OrganisationTuple mapOrganisationToTuple(CordraObject cordraObject) {
    var content = cordraObject.content.getAsJsonObject();
    var name = content.get("organisation_name").getAsString();
    String ror = getRor(content);
    return new OrganisationTuple(name, ror);
  }

  private String getRor(JsonObject content) {
    var identifiers = content.get("externalIdentifiers");
    if (identifiers != null && identifiers.getAsJsonObject().has("ROR")) {
      return identifiers.getAsJsonObject().get("ROR").getAsJsonObject().get("url").getAsString();
    } else {
      log.warn("No ROR present, should be included in the Organisation object");
      return null;
    }
  }

  private String mapOrganisationToName(CordraObject cordraObject) {
    return cordraObject.content.getAsJsonObject().get("organisation_name").getAsString();
  }
}
