package eu.dissco.backend.repository;

import static eu.dissco.backend.database.jooq.Tables.NEW_DIGITAL_MEDIA_OBJECT;
import static eu.dissco.backend.database.jooq.Tables.NEW_DIGITAL_SPECIMEN;
import static eu.dissco.backend.repository.RepositoryUtils.getOffset;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.dissco.backend.domain.DigitalMediaObject;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record1;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class DigitalMediaObjectRepository {

  private final ObjectMapper mapper;
  private final DSLContext context;

  public DigitalMediaObject getLatestDigitalMediaById(String id) {
    return context.select(NEW_DIGITAL_MEDIA_OBJECT.asterisk())
        .from(NEW_DIGITAL_MEDIA_OBJECT)
        .where(NEW_DIGITAL_MEDIA_OBJECT.ID.eq(id))
        .fetchOne(this::mapToMultiMediaObject);
  }

  public JsonApiData getLatestDigitalMediaObjectByIdJsonResponse(String id) {
    return context.select(NEW_DIGITAL_SPECIMEN.SPECIMEN_NAME, NEW_DIGITAL_SPECIMEN.VERSION,
            NEW_DIGITAL_SPECIMEN.ID, NEW_DIGITAL_MEDIA_OBJECT.asterisk())
        .from(NEW_DIGITAL_SPECIMEN)
        .join(NEW_DIGITAL_MEDIA_OBJECT)
        .on(NEW_DIGITAL_SPECIMEN.ID.eq(NEW_DIGITAL_MEDIA_OBJECT.DIGITAL_SPECIMEN_ID))
        .where(NEW_DIGITAL_MEDIA_OBJECT.ID.eq(id))
        .fetchOne(this::mapToJsonApiData);
  }

  public List<DigitalMediaObject> getDigitalMediaForSpecimen(String id) {
    return context.select(NEW_DIGITAL_MEDIA_OBJECT.asterisk())
        .from(NEW_DIGITAL_MEDIA_OBJECT)
        .where(NEW_DIGITAL_MEDIA_OBJECT.DIGITAL_SPECIMEN_ID.eq(id))
        .fetch(this::mapToMultiMediaObject);
  }

  public List<DigitalMediaObject> getDigitalMediaObject(int pageNumber, int pageSize) {
    int offset = getOffset(pageNumber, pageSize);
    return context.select(NEW_DIGITAL_MEDIA_OBJECT.asterisk())
        .from(NEW_DIGITAL_MEDIA_OBJECT)
        .offset(offset).limit(pageSize).fetch(this::mapToMultiMediaObject);
  }

  public List<JsonApiData> getDigitalMediaObjectJsonResponse(int pageNumber, int pageSize) {
    int offset = getOffset(pageNumber, pageSize);
    return context.select(NEW_DIGITAL_SPECIMEN.SPECIMEN_NAME, NEW_DIGITAL_SPECIMEN.VERSION,
            NEW_DIGITAL_SPECIMEN.ID, NEW_DIGITAL_MEDIA_OBJECT.asterisk()).from(NEW_DIGITAL_SPECIMEN)
        .join(NEW_DIGITAL_MEDIA_OBJECT)
        .on(NEW_DIGITAL_SPECIMEN.ID.eq(NEW_DIGITAL_MEDIA_OBJECT.DIGITAL_SPECIMEN_ID))
        .offset(offset).limit(pageSize).fetch(this::mapToJsonApiData);
  }

  public List<String> getDigitalMediaIdsForSpecimen(String id) {
    return context.select(NEW_DIGITAL_MEDIA_OBJECT.ID)
        .from(NEW_DIGITAL_MEDIA_OBJECT)
        .where(NEW_DIGITAL_MEDIA_OBJECT.DIGITAL_SPECIMEN_ID.eq(id))
        .fetch(Record1::value1);
  }

  private DigitalMediaObject mapToMultiMediaObject(Record dbRecord) {
    try {
      return new DigitalMediaObject(dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.ID),
          dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.VERSION),
          dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.CREATED),
          dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.TYPE),
          dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.DIGITAL_SPECIMEN_ID),
          dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.MEDIA_URL),
          dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.FORMAT),
          dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.SOURCE_SYSTEM_ID),
          mapper.readTree(dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.DATA).data()),
          mapper.readTree(dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.ORIGINAL_DATA).data()));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  private JsonApiData mapToJsonApiData(Record dbRecord) {
    ObjectNode attributeNode = mapper.createObjectNode();
    ObjectNode specimenNode = mapper.createObjectNode();
    try {
      attributeNode.put("id", dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.ID));
      attributeNode.put("version", dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.VERSION));
      attributeNode.put("type", dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.TYPE));
      attributeNode.put("created", String.valueOf(dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.CREATED)));
      attributeNode.put("digitalSpecimenId",
          String.valueOf(dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.DIGITAL_SPECIMEN_ID)));
      attributeNode.put("mediaUrl",
          String.valueOf(dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.MEDIA_URL)));
      attributeNode.put("format", String.valueOf(dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.FORMAT)));
      attributeNode.put("sourceSystemId",
          String.valueOf(dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.SOURCE_SYSTEM_ID)));
      attributeNode.set("data",
          mapper.readTree(dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.DATA).data()));
      attributeNode.set("originalData",
          mapper.readTree(dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.ORIGINAL_DATA).data()));
      if (dbRecord.field(NEW_DIGITAL_SPECIMEN.SPECIMEN_NAME) != null) {
        specimenNode.put("digitalSpecimenName", dbRecord.get(NEW_DIGITAL_SPECIMEN.SPECIMEN_NAME));
        specimenNode.put("digitalSpecimenVersion", dbRecord.get(NEW_DIGITAL_SPECIMEN.VERSION));
        attributeNode.set("digitalSpecimen", specimenNode);
      }
    } catch (JsonProcessingException e) {
      log.error("Failed to parse annotation body to Json", e);
      return null;
    }
    return new JsonApiData(attributeNode.get("id").asText(), attributeNode.get("type").asText(),
        attributeNode);
  }
}
