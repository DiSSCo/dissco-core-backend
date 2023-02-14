package eu.dissco.backend.repository;

import static eu.dissco.backend.database.jooq.Tables.NEW_ANNOTATION;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.dissco.backend.domain.AnnotationResponse;
import eu.dissco.backend.domain.JsonApiData;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record1;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class AnnotationRepository {

  private final DSLContext context;
  private final ObjectMapper mapper;

  public List<AnnotationResponse> getAnnotationsForUser(String userId, int pageNumber,
      int pageSize) {
    int offset = 0;
    if (pageNumber > 1) {
      offset = offset + (pageSize * (pageNumber - 1));
    }

    return context.select(NEW_ANNOTATION.asterisk()).distinctOn(NEW_ANNOTATION.ID)
        .from(NEW_ANNOTATION).where(NEW_ANNOTATION.CREATOR.eq(userId))
        .orderBy(NEW_ANNOTATION.ID, NEW_ANNOTATION.VERSION.desc(), NEW_ANNOTATION.CREATED)
        .limit(pageSize).offset(offset).fetch(this::mapToAnnotation);
  }

  public List<JsonApiData> getAnnotationsForUserJsonResponse(String userId, int pageNumber,
      int pageSize) {
    int offset = 0;
    if (pageNumber > 1) {
      offset = offset + (pageSize * (pageNumber - 1));
    }

    return context.select(NEW_ANNOTATION.asterisk()).distinctOn(NEW_ANNOTATION.ID)
        .from(NEW_ANNOTATION).where(NEW_ANNOTATION.CREATOR.eq(userId))
        .orderBy(NEW_ANNOTATION.ID, NEW_ANNOTATION.VERSION.desc(), NEW_ANNOTATION.CREATED)
        .limit(pageSize).offset(offset).fetch(this::mapToJsonApiData);
  }

  public AnnotationResponse getAnnotation(String id) {
    return context.select(NEW_ANNOTATION.asterisk()).distinctOn(NEW_ANNOTATION.ID)
        .from(NEW_ANNOTATION).where(NEW_ANNOTATION.ID.eq(id))
        .orderBy(NEW_ANNOTATION.ID, NEW_ANNOTATION.VERSION.desc()).fetchOne(this::mapToAnnotation);
  }

  public List<AnnotationResponse> getAnnotations(int pageNumber, int pageSize) {
    int offset = 0;
    if (pageNumber > 1) {
      offset = offset + (pageSize * (pageNumber - 1));
    }

    return context.select(NEW_ANNOTATION.asterisk()).from(NEW_ANNOTATION).limit(pageSize)
        .offset(offset).fetch(this::mapToAnnotation);
  }

  public List<JsonApiData> getAnnotationsJsonResponse(int pageNumber, int pageSize) {
    int offset = 0;
    if (pageNumber > 1) {
      offset = offset + (pageSize * (pageNumber - 1));
    }
    return context.select(NEW_ANNOTATION.asterisk()).distinctOn(NEW_ANNOTATION.ID)
        .from(NEW_ANNOTATION)
        .orderBy(NEW_ANNOTATION.ID, NEW_ANNOTATION.VERSION.desc(), NEW_ANNOTATION.CREATED)
        .limit(pageSize).offset(offset).fetch(this::mapToJsonApiData);
  }

  public Integer getAnnotationsCountForUser(String userId, int pageSize) {
    int totalRecords = context.selectCount().from(NEW_ANNOTATION)
        .where(NEW_ANNOTATION.CREATOR.eq(userId)).fetchOne(0, int.class);
    return totalRecords / pageSize + ((totalRecords % pageSize == 0) ? 0 : 1);
  }

  public Integer getAnnotationsCountGlobal(int pageSize) {
    int totalRecords = context.selectCount().from(NEW_ANNOTATION).fetchOne(0, int.class);
    return totalRecords / pageSize + ((totalRecords % pageSize == 0) ? 0 : 1);
  }

  public AnnotationResponse getAnnotationVersion(String id, int version) {
    return context.select(NEW_ANNOTATION.asterisk()).from(NEW_ANNOTATION)
        .where(NEW_ANNOTATION.ID.eq(id)).and(NEW_ANNOTATION.VERSION.eq(version))
        .fetchOne(this::mapToAnnotation);
  }

  private AnnotationResponse mapToAnnotation(Record dbRecord) {
    try {
      return new AnnotationResponse(dbRecord.get(NEW_ANNOTATION.ID),
          dbRecord.get(NEW_ANNOTATION.VERSION), dbRecord.get(NEW_ANNOTATION.TYPE),
          dbRecord.get(NEW_ANNOTATION.MOTIVATION),
          mapper.readTree(dbRecord.get(NEW_ANNOTATION.TARGET_BODY).data()),
          mapper.readTree(dbRecord.get(NEW_ANNOTATION.BODY).data()),
          dbRecord.get(NEW_ANNOTATION.PREFERENCE_SCORE), dbRecord.get(NEW_ANNOTATION.CREATOR),
          dbRecord.get(NEW_ANNOTATION.CREATED),
          mapper.readTree(dbRecord.get(NEW_ANNOTATION.GENERATOR_BODY).data()),
          dbRecord.get(NEW_ANNOTATION.GENERATED), dbRecord.get(NEW_ANNOTATION.DELETED));
    } catch (JsonProcessingException e) {
      log.error("Failed to parse annotation body to Json", e);
      return null;
    }
  }

  private JsonApiData mapToJsonApiData(Record dbRecord) {
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

  public List<AnnotationResponse> getForTarget(String id) {
    return context.select(NEW_ANNOTATION.asterisk()).distinctOn(NEW_ANNOTATION.ID)
        .from(NEW_ANNOTATION).where(NEW_ANNOTATION.TARGET_ID.eq(id))
        .and(NEW_ANNOTATION.DELETED.isNull())
        .orderBy(NEW_ANNOTATION.ID, NEW_ANNOTATION.VERSION.desc()).fetch(this::mapToAnnotation);
  }

  public List<Integer> getAnnotationVersions(String id) {
    return context.select(NEW_ANNOTATION.VERSION).from(NEW_ANNOTATION)
        .where(NEW_ANNOTATION.ID.eq(id)).fetch(Record1::value1).stream().toList();
  }

  public int getAnnotationForUser(String id, String userId) {
    return context.select(NEW_ANNOTATION.ID).distinctOn(NEW_ANNOTATION.ID).from(NEW_ANNOTATION)
        .where(NEW_ANNOTATION.ID.eq(id)).and(NEW_ANNOTATION.CREATOR.eq(userId))
        .and(NEW_ANNOTATION.DELETED.isNull())
        .orderBy(NEW_ANNOTATION.ID, NEW_ANNOTATION.VERSION.desc()).fetch().size();
  }
}
