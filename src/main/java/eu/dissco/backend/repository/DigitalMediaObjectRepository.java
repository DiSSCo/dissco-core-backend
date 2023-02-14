package eu.dissco.backend.repository;

import static eu.dissco.backend.database.jooq.Tables.NEW_ANNOTATION;
import static eu.dissco.backend.database.jooq.Tables.NEW_DIGITAL_MEDIA_OBJECT;
import static eu.dissco.backend.database.jooq.Tables.NEW_DIGITAL_SPECIMEN;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.dissco.backend.domain.DigitalMediaObject;
import eu.dissco.backend.domain.JsonApiData;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class DigitalMediaObjectRepository {

  private final ObjectMapper mapper;
  private final DSLContext context;

  public DigitalMediaObject getLatestDigitalMediaById(String id) {
    return context.select(NEW_DIGITAL_MEDIA_OBJECT.asterisk())
        .distinctOn(NEW_DIGITAL_MEDIA_OBJECT.ID).from(NEW_DIGITAL_MEDIA_OBJECT)
        .where(NEW_DIGITAL_MEDIA_OBJECT.ID.eq(id))
        .orderBy(NEW_DIGITAL_MEDIA_OBJECT.ID, NEW_DIGITAL_MEDIA_OBJECT.VERSION.desc())
        .fetchOne(this::mapToMultiMediaObject);
  }

  public JsonApiData getLatestDigitalMediaObjectByIdJsonResponse(String id) {

    Field<Integer> maxSpeciesVersion = DSL.max(NEW_DIGITAL_SPECIMEN.VERSION)
        .as("maxSpeciesVersion");

    var specimenRecentVersions = context.select(NEW_DIGITAL_SPECIMEN.ID, maxSpeciesVersion)
        .from(NEW_DIGITAL_SPECIMEN).groupBy(NEW_DIGITAL_SPECIMEN.ID).asTable();

    return context.select(NEW_DIGITAL_SPECIMEN.SPECIMEN_NAME, NEW_DIGITAL_SPECIMEN.VERSION,
            NEW_DIGITAL_SPECIMEN.ID, NEW_DIGITAL_MEDIA_OBJECT.asterisk()).from(NEW_DIGITAL_SPECIMEN)
        .join(specimenRecentVersions)
        .on(NEW_DIGITAL_SPECIMEN.ID.eq(specimenRecentVersions.field(NEW_DIGITAL_SPECIMEN.ID)))
        .and(NEW_DIGITAL_SPECIMEN.VERSION.eq(specimenRecentVersions.field(maxSpeciesVersion)))
        .join(NEW_DIGITAL_MEDIA_OBJECT)
        .on(NEW_DIGITAL_SPECIMEN.ID.eq(NEW_DIGITAL_MEDIA_OBJECT.DIGITAL_SPECIMEN_ID))
        .where(NEW_DIGITAL_MEDIA_OBJECT.ID.eq(id))
        .orderBy(NEW_DIGITAL_MEDIA_OBJECT.ID, NEW_DIGITAL_MEDIA_OBJECT.VERSION.desc()).limit(1)
        .fetchOne(this::mapToJsonApiData);
  }

  public List<JsonApiData> getAnnotationsOnDigitalMediaObject(String mediaId){
    return context.select(NEW_ANNOTATION.asterisk())
        .from(NEW_ANNOTATION)
        .where(NEW_ANNOTATION.TARGET_ID.eq(mediaId))
        .fetch(this::mapToAnnotationJsonApiData);
  }

  public int getAnnotationPageCountOnMediaObject(String mediaId, int pageSize){
    int totalRecords = context.selectCount()
        .from(NEW_ANNOTATION)
        .where(NEW_ANNOTATION.TARGET_ID.eq(mediaId))
        .fetchOne(0, int.class);
    return totalRecords / pageSize + ((totalRecords % pageSize == 0) ? 0 : 1);
  }


  public List<DigitalMediaObject> getDigitalMediaForSpecimen(String id) {
    return context.select(NEW_DIGITAL_MEDIA_OBJECT.asterisk())
        .distinctOn(NEW_DIGITAL_MEDIA_OBJECT.ID).from(NEW_DIGITAL_MEDIA_OBJECT)
        .where(NEW_DIGITAL_MEDIA_OBJECT.DIGITAL_SPECIMEN_ID.eq(id))
        .orderBy(NEW_DIGITAL_MEDIA_OBJECT.ID, NEW_DIGITAL_MEDIA_OBJECT.VERSION.desc())
        .fetch(this::mapToMultiMediaObject);
  }

  public List<Integer> getDigitalMediaVersions(String id) {
    return context.select(NEW_DIGITAL_MEDIA_OBJECT.VERSION).from(NEW_DIGITAL_MEDIA_OBJECT)
        .where(NEW_DIGITAL_MEDIA_OBJECT.ID.eq(id)).fetch(Record1::value1).stream().toList();
  }

  public DigitalMediaObject getDigitalMediaByVersion(String id, int version) {
    return context.select(NEW_DIGITAL_MEDIA_OBJECT.asterisk()).from(NEW_DIGITAL_MEDIA_OBJECT)
        .where(NEW_DIGITAL_MEDIA_OBJECT.ID.eq(id)).and(NEW_DIGITAL_MEDIA_OBJECT.VERSION.eq(version))
        .fetchOne(this::mapToMultiMediaObject);
  }

  public JsonApiData getDigitalMediaByVersionJsonResponse(String id, int version) {

    Field<Integer> maxSpeciesVersion = DSL.max(NEW_DIGITAL_SPECIMEN.VERSION)
        .as("maxSpeciesVersion");
    var specimenRecentVersions = context.select(NEW_DIGITAL_SPECIMEN.ID, maxSpeciesVersion)
        .from(NEW_DIGITAL_SPECIMEN).groupBy(NEW_DIGITAL_SPECIMEN.ID).asTable();

    return context.select(NEW_DIGITAL_SPECIMEN.SPECIMEN_NAME, NEW_DIGITAL_SPECIMEN.VERSION,
            NEW_DIGITAL_SPECIMEN.ID, NEW_DIGITAL_MEDIA_OBJECT.asterisk()).from(NEW_DIGITAL_SPECIMEN)
        .join(specimenRecentVersions)
        .on(NEW_DIGITAL_SPECIMEN.ID.eq(specimenRecentVersions.field(NEW_DIGITAL_SPECIMEN.ID)))
        .and(NEW_DIGITAL_SPECIMEN.VERSION.eq(specimenRecentVersions.field(maxSpeciesVersion)))
        .join(NEW_DIGITAL_MEDIA_OBJECT)
        .on(NEW_DIGITAL_SPECIMEN.ID.eq(NEW_DIGITAL_MEDIA_OBJECT.DIGITAL_SPECIMEN_ID))
        .where(NEW_DIGITAL_MEDIA_OBJECT.ID.eq(id)).and(NEW_DIGITAL_MEDIA_OBJECT.VERSION.eq(version))
        .fetchOne(this::mapToJsonApiData);
  }

  public List<DigitalMediaObject> getDigitalMediaObject(int pageNumber, int pageSize) {
    var offset = 0;
    if (pageNumber > 1) {
      offset = offset + (pageSize * (pageNumber - 1));
    }
    return context.select(NEW_DIGITAL_MEDIA_OBJECT.asterisk()).from(NEW_DIGITAL_MEDIA_OBJECT)
        .offset(offset).limit(pageSize).fetch(this::mapToMultiMediaObject);
  }

  public List<JsonApiData> getDigitalMediaObjectJsonResponse(int pageNumber, int pageSize) {
    var offset = 0;
    if (pageNumber > 1) {
      offset = offset + (pageSize * (pageNumber - 1));
    }

    Field<Integer> maxVersion = DSL.max(NEW_DIGITAL_SPECIMEN.VERSION).as("maxVersion");

    var specimenRecentVersions = context.select(NEW_DIGITAL_SPECIMEN.ID, maxVersion)
        .from(NEW_DIGITAL_SPECIMEN).groupBy(NEW_DIGITAL_SPECIMEN.ID).asTable();

    return context.select(NEW_DIGITAL_SPECIMEN.SPECIMEN_NAME, NEW_DIGITAL_SPECIMEN.VERSION,
            NEW_DIGITAL_SPECIMEN.ID, NEW_DIGITAL_MEDIA_OBJECT.asterisk()).from(NEW_DIGITAL_SPECIMEN)
        .join(specimenRecentVersions)
        .on(NEW_DIGITAL_SPECIMEN.ID.eq(specimenRecentVersions.field(NEW_DIGITAL_SPECIMEN.ID)))
        .and(NEW_DIGITAL_SPECIMEN.VERSION.eq(specimenRecentVersions.field(maxVersion)))
        .join(NEW_DIGITAL_MEDIA_OBJECT)
        .on(NEW_DIGITAL_SPECIMEN.ID.eq(NEW_DIGITAL_MEDIA_OBJECT.DIGITAL_SPECIMEN_ID)).offset(offset)
        .limit(pageSize).fetch(this::mapToJsonApiData);
  }

  public int getMediaObjectCount(int pageSize) {
    int totalRecords = context.selectCount().from(NEW_DIGITAL_MEDIA_OBJECT).fetchOne(0, int.class);
    return totalRecords / pageSize + ((totalRecords % pageSize == 0) ? 0 : 1);
  }

  public List<String> getDigitalMediaIdsForSpecimen(String id) {
    return context.select(NEW_DIGITAL_MEDIA_OBJECT.ID).distinctOn(NEW_DIGITAL_MEDIA_OBJECT.ID)
        .from(NEW_DIGITAL_MEDIA_OBJECT).where(NEW_DIGITAL_MEDIA_OBJECT.DIGITAL_SPECIMEN_ID.eq(id))
        .orderBy(NEW_DIGITAL_MEDIA_OBJECT.ID, NEW_DIGITAL_MEDIA_OBJECT.VERSION.desc())
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

  private JsonApiData mapToAnnotationJsonApiData(Record dbRecord) {
    ObjectNode attributeNode = mapper.createObjectNode();
    try {
      attributeNode.put("id", dbRecord.get(NEW_ANNOTATION.ID));
      attributeNode.put("version", dbRecord.get(NEW_ANNOTATION.VERSION));
      attributeNode.put("type", dbRecord.get(NEW_ANNOTATION.TYPE));
      attributeNode.put("motivation", dbRecord.get(NEW_ANNOTATION.MOTIVATION));
      attributeNode.set("target", mapper.readTree(dbRecord.get(NEW_ANNOTATION.TARGET_BODY).data()));
      attributeNode.set("body", mapper.readTree(dbRecord.get(NEW_ANNOTATION.BODY).data()));
      attributeNode.put("preferenceScore", dbRecord.get(NEW_ANNOTATION.PREFERENCE_SCORE));
      attributeNode.put("creator", dbRecord.get(NEW_ANNOTATION.CREATOR));
      attributeNode.put("created", String.valueOf(dbRecord.get(NEW_ANNOTATION.CREATED)));
      attributeNode.set("generator",
          mapper.readTree(dbRecord.get(NEW_ANNOTATION.GENERATOR_BODY).data()));
      attributeNode.put("generated", String.valueOf(dbRecord.get(NEW_ANNOTATION.GENERATED)));
      attributeNode.put("deleted", String.valueOf(dbRecord.get(NEW_ANNOTATION.DELETED)));
    } catch (JsonProcessingException e) {
      log.error("Failed to parse annotation body to Json", e);
      return null;
    }
    return new JsonApiData(dbRecord.get(NEW_ANNOTATION.ID), dbRecord.get(NEW_ANNOTATION.TYPE),
        attributeNode);
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
