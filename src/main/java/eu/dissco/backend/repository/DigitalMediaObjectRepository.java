package eu.dissco.backend.repository;

import static eu.dissco.backend.database.jooq.Tables.NEW_ANNOTATION;
import static eu.dissco.backend.database.jooq.Tables.NEW_DIGITAL_MEDIA_OBJECT;
import static eu.dissco.backend.database.jooq.Tables.NEW_DIGITAL_SPECIMEN;
import static org.jooq.impl.DSL.select;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.dissco.backend.domain.DigitalMediaObject;
import eu.dissco.backend.domain.DigitalSpecimen;
import eu.dissco.backend.domain.JsonApiData;
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
        .distinctOn(NEW_DIGITAL_MEDIA_OBJECT.ID)
        .from(NEW_DIGITAL_MEDIA_OBJECT)
        .where(NEW_DIGITAL_MEDIA_OBJECT.ID.eq(id))
        .orderBy(NEW_DIGITAL_MEDIA_OBJECT.ID, NEW_DIGITAL_MEDIA_OBJECT.VERSION.desc())
        .fetchOne(this::mapToMultiMediaObject);
  }

  private DigitalMediaObject mapToMultiMediaObject(Record dbRecord) {
    try {
      return new DigitalMediaObject(
          dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.ID),
          dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.VERSION),
          dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.CREATED),
          dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.TYPE),
          dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.DIGITAL_SPECIMEN_ID),
          dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.MEDIA_URL),
          dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.FORMAT),
          dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.SOURCE_SYSTEM_ID),
          mapper.readTree(dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.DATA).data()),
          mapper.readTree(dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.ORIGINAL_DATA).data())
      );
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public List<DigitalMediaObject> getForDigitalSpecimen(String id) {
    return context.select(NEW_DIGITAL_MEDIA_OBJECT.asterisk())
        .distinctOn(NEW_DIGITAL_MEDIA_OBJECT.ID)
        .from(NEW_DIGITAL_MEDIA_OBJECT)
        .where(NEW_DIGITAL_MEDIA_OBJECT.DIGITAL_SPECIMEN_ID.eq(id))
        .orderBy(NEW_DIGITAL_MEDIA_OBJECT.ID, NEW_DIGITAL_MEDIA_OBJECT.VERSION.desc())
        .fetch(this::mapToMultiMediaObject);
  }

  public List<Integer> getDigitalMediaVersions(String id) {
    return context.select(NEW_DIGITAL_MEDIA_OBJECT.VERSION)
        .from(NEW_DIGITAL_MEDIA_OBJECT)
        .where(NEW_DIGITAL_MEDIA_OBJECT.ID.eq(id))
        .fetch(Record1::value1).stream().toList();
  }

  public DigitalMediaObject getDigitalMediaByVersion(String id, int version) {
    return context.select(NEW_DIGITAL_MEDIA_OBJECT.asterisk())
        .from(NEW_DIGITAL_MEDIA_OBJECT)
        .where(NEW_DIGITAL_MEDIA_OBJECT.ID.eq(id))
        .and(NEW_DIGITAL_MEDIA_OBJECT.VERSION.eq(version))
        .fetchOne(this::mapToMultiMediaObject);
  }

  public List<DigitalMediaObject> getDigitalMediaForSpecimen(String id) {
    return context.select(NEW_DIGITAL_MEDIA_OBJECT.asterisk())
        .distinctOn(NEW_DIGITAL_MEDIA_OBJECT.ID)
        .from(NEW_DIGITAL_MEDIA_OBJECT)
        .where(NEW_DIGITAL_MEDIA_OBJECT.DIGITAL_SPECIMEN_ID.eq(id))
        .orderBy(NEW_DIGITAL_MEDIA_OBJECT.ID, NEW_DIGITAL_MEDIA_OBJECT.VERSION.desc())
        .fetch(this::mapToMultiMediaObject);
  }

  public List<DigitalMediaObject> getDigitalMediaObject(int pageNumber, int pageSize) {
    var offset = 0;
    if (pageNumber > 1) {
      offset = offset + (pageSize * (pageNumber - 1));
    }
    return context.select(NEW_DIGITAL_MEDIA_OBJECT.asterisk())
        .from(NEW_DIGITAL_MEDIA_OBJECT)
        .offset(offset)
        .limit(pageSize)
        .fetch(this::mapToMultiMediaObject);
  }

  public List<JsonApiData> getDigitalMediaObjectJsonResponse(int pageNumber, int pageSize) {
    var offset = 0;
    if (pageNumber > 1) {
      offset = offset + (pageSize * (pageNumber - 1));
    }
    return context.select(NEW_DIGITAL_MEDIA_OBJECT.asterisk(), NEW_DIGITAL_SPECIMEN.SPECIMEN_NAME)
        .from(NEW_DIGITAL_MEDIA_OBJECT)
        .join(NEW_DIGITAL_SPECIMEN)
        .on(NEW_DIGITAL_SPECIMEN.ID.eq(NEW_DIGITAL_MEDIA_OBJECT.DIGITAL_SPECIMEN_ID))
        .where(NEW_DIGITAL_MEDIA_OBJECT.VERSION.eq(1))
        .offset(offset)
        .limit(pageSize)
        .fetch(this::mapToJsonApiData);
  }

  public int getMediaObjectCount(int pageSize){
    int totalRecords = context.selectCount()
        .from(NEW_DIGITAL_MEDIA_OBJECT)
        .fetchOne(0, int.class);
    return totalRecords/pageSize + ((totalRecords % pageSize == 0) ? 0 : 1);
  }

  private JsonApiData mapToJsonApiData(Record dbRecord){
    ObjectNode attributeNode = mapper.createObjectNode();
    try {
      attributeNode.put("id", dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.ID));
      attributeNode.put("version", dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.VERSION));
      attributeNode.put("type", dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.TYPE));
      attributeNode.put("created",String.valueOf(dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.CREATED)));
      attributeNode.put("digitalSpecimenId",String.valueOf(dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.DIGITAL_SPECIMEN_ID)));
      attributeNode.put("mediaUrl",String.valueOf(dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.MEDIA_URL)));
      attributeNode.put("format",String.valueOf(dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.FORMAT)));
      attributeNode.put("sourceSystemId",String.valueOf(dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.SOURCE_SYSTEM_ID)));
      attributeNode.set("data", mapper.readTree(dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.DATA).data()));
      attributeNode.set("originalData", mapper.readTree(dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.ORIGINAL_DATA).data()));
    } catch (JsonProcessingException e) {
      log.error("Failed to parse annotation body to Json", e);
      return null;
    }
    return new JsonApiData(dbRecord.get(NEW_ANNOTATION.ID),dbRecord.get(NEW_ANNOTATION.TYPE), attributeNode);
  }


  public List<String> getDigitalMediaIdsForSpecimen(String id) {
    return context.select(NEW_DIGITAL_MEDIA_OBJECT.ID)
        .distinctOn(NEW_DIGITAL_MEDIA_OBJECT.ID)
        .from(NEW_DIGITAL_MEDIA_OBJECT)
        .where(NEW_DIGITAL_MEDIA_OBJECT.DIGITAL_SPECIMEN_ID.eq(id))
        .orderBy(NEW_DIGITAL_MEDIA_OBJECT.ID, NEW_DIGITAL_MEDIA_OBJECT.VERSION.desc())
        .fetch(Record1::value1);
  }
}
