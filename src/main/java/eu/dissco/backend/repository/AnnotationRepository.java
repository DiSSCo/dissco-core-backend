package eu.dissco.backend.repository;

import static eu.dissco.backend.database.jooq.Tables.NEW_ANNOTATION;
import static eu.dissco.backend.repository.RepositoryUtils.getOffset;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.dissco.backend.domain.AnnotationResponse;
import eu.dissco.backend.domain.jsonapi.JsonApiData;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class AnnotationRepository {

  private final DSLContext context;
  private final ObjectMapper mapper;


  public List<AnnotationResponse> getAnnotationsForUserObject(String userId, int pageNumber,
      int pageSize) {
    int offset = getOffset(pageNumber, pageSize);

    return context.select(NEW_ANNOTATION.asterisk())
        .from(NEW_ANNOTATION)
        .where(NEW_ANNOTATION.CREATOR.eq(userId))
        .orderBy(NEW_ANNOTATION.CREATED)
        .limit(pageSize).offset(offset).fetch(this::mapToAnnotation);
  }

  public List<JsonApiData> getAnnotationsForUser(String userId, int pageNumber,
      int pageSize) {
    int offset = getOffset(pageNumber, pageSize);

    return context.select(NEW_ANNOTATION.asterisk())
        .from(NEW_ANNOTATION)
        .where(NEW_ANNOTATION.CREATOR.eq(userId))
        .orderBy(NEW_ANNOTATION.CREATED.desc())
        .limit(pageSize).offset(offset).fetch(this::mapToJsonApiData);
  }

  public JsonApiData getAnnotation(String id) {
    return context.select(NEW_ANNOTATION.asterisk())
        .from(NEW_ANNOTATION)
        .where(NEW_ANNOTATION.ID.eq(id))
        .fetchOne(this::mapToJsonApiData);
  }

  public List<AnnotationResponse> getAnnotationsObject(int pageNumber, int pageSize) {
    int offset = getOffset(pageNumber, pageSize);

    return context.select(NEW_ANNOTATION.asterisk())
        .from(NEW_ANNOTATION)
        .limit(pageSize)
        .offset(offset).fetch(this::mapToAnnotation);
  }

  public List<JsonApiData> getAnnotations(int pageNumber, int pageSize) {
    int offset = getOffset(pageNumber, pageSize);
    return context.select(NEW_ANNOTATION.asterisk())
        .from(NEW_ANNOTATION)
        .orderBy(NEW_ANNOTATION.CREATED.desc())
        .limit(pageSize).offset(offset).fetch(this::mapToJsonApiData);
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
    return context.select(NEW_ANNOTATION.asterisk())
        .from(NEW_ANNOTATION)
        .where(NEW_ANNOTATION.TARGET_ID.eq(id))
        .and(NEW_ANNOTATION.DELETED.isNull())
        .fetch(this::mapToAnnotation);
  }

  public int getAnnotationForUser(String id, String userId) {
    return context.select(NEW_ANNOTATION.ID)
        .from(NEW_ANNOTATION)
        .where(NEW_ANNOTATION.ID.eq(id))
        .and(NEW_ANNOTATION.CREATOR.eq(userId))
        .and(NEW_ANNOTATION.DELETED.isNull())
        .fetch().size();
  }
}
