package eu.dissco.backend.repository;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Projections.include;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoDatabase;
import eu.dissco.backend.exceptions.NotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MongoRepository {

  private static final String PROV_ENTITY = "prov:Entity";
  private static final String PROV_VALUE = "prov:value";
  public static final String ODS_VERSION = "ods:version";
  public static final String SCHEMA_VERSION = "schema:version";

  private final MongoDatabase database;
  private final ObjectMapper mapper;

  public JsonNode getByVersion(String id, int version, String collectionName)
      throws JsonProcessingException, NotFoundException {
    var collection = database.getCollection(collectionName);
    var versionId = id + '/' + version;
    var result = collection.find(eq("_id", versionId));
    if (result.first() == null) {
      throw new NotFoundException(
          "Could not find " + versionId + " in collection: " + collectionName);
    }
    return mapper.readValue(result.first().toJson(), JsonNode.class).get(PROV_ENTITY)
        .get(PROV_VALUE);
  }

  public List<Integer> getVersions(String id, String collectionName, String versionProperty) throws NotFoundException {
    var collection = database.getCollection(collectionName);
    var result = collection.find(eq(PROV_ENTITY + "." + PROV_VALUE + ".@id", id))
        .projection(include(PROV_ENTITY + "." + PROV_VALUE + "." + versionProperty));
    if (result.first() == null) {
      throw new NotFoundException("Could not find " + id + " in collection: " + collectionName);
    }
    var versions = new ArrayList<Integer>();
    result.forEach(document -> versions.add(
        document.getEmbedded(List.of(PROV_ENTITY, PROV_VALUE, versionProperty), Integer.class)));
    Collections.sort(versions);
    return versions;
  }
}
