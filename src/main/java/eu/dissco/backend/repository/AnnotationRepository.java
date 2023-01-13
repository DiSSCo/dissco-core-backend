package eu.dissco.backend.repository;

import static eu.dissco.backend.database.jooq.Tables.NEW_ANNOTATION;
import static eu.dissco.backend.database.jooq.Tables.NEW_DIGITAL_SPECIMEN;
import static org.jooq.impl.DSL.count;
import static org.jooq.impl.DSL.val;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.dissco.backend.domain.AnnotationResponse;
import eu.dissco.backend.domain.JsonApiData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

  public List<AnnotationResponse> getAnnotationsForUser(String userId, int pageNumber, int pageSize) {
    return context.select(NEW_ANNOTATION.asterisk())
        .distinctOn(NEW_ANNOTATION.ID)
        .from(NEW_ANNOTATION)
        .where(NEW_ANNOTATION.CREATOR.eq(userId))
        .orderBy(NEW_ANNOTATION.ID, NEW_ANNOTATION.VERSION.desc(), NEW_ANNOTATION.CREATED)
        .limit(pageSize)
        .offset(pageNumber)
        .fetch(this::mapToAnnotation);
  }

  public Map<Integer, List<JsonApiData>>  getAnnotationsForUserWithCount (String userId, int pageNumber, int pageSize) {
    int totalRecords = context.selectCount()
        .from(NEW_ANNOTATION)
        .fetchOne(0, int.class);
    Integer pageCount = totalRecords/pageSize + ((totalRecords % pageSize == 0) ? 0 : 1);

    var annotations = context.select(NEW_ANNOTATION.asterisk())
        .distinctOn(NEW_ANNOTATION.ID)
        .from(NEW_ANNOTATION)
        .where(NEW_ANNOTATION.CREATOR.eq(userId))
        .orderBy(NEW_ANNOTATION.ID, NEW_ANNOTATION.VERSION.desc(), NEW_ANNOTATION.CREATED)
        .limit(pageSize)
        .offset(pageNumber)
        .fetch(this::mapToJsonApiData);
    HashMap<Integer, List<JsonApiData>> pair = new HashMap<>();

    pair.put(pageCount, annotations);
    return pair;
  }


  public AnnotationResponse getAnnotation(String id) {
    return context.select(NEW_ANNOTATION.asterisk())
        .distinctOn(NEW_ANNOTATION.ID)
        .from(NEW_ANNOTATION)
        .where(NEW_ANNOTATION.ID.eq(id))
        .orderBy(NEW_ANNOTATION.ID, NEW_ANNOTATION.VERSION.desc())
        .fetchOne(this::mapToAnnotation);
  }

  public JsonApiData getAnnotationWithSpeciesName(String id) {

    // This doesn't work because we don't know if we need to query the digital_specimen table or the media_object table
    log.info("incoming");
    return context.select(NEW_ANNOTATION.asterisk())
        .distinctOn(NEW_ANNOTATION.ID)
        .from(NEW_ANNOTATION)
        .join(NEW_DIGITAL_SPECIMEN).on((NEW_ANNOTATION.TARGET_ID).contains(NEW_DIGITAL_SPECIMEN.ID))
        .where(NEW_ANNOTATION.ID.eq(id))
        .orderBy(NEW_ANNOTATION.ID, NEW_ANNOTATION.VERSION.desc())
        .fetchOne(this::mapToJsonApiData);
  }

  public List<AnnotationResponse> getAnnotations(int pageNumber, int pageSize) {
    return context.select(NEW_ANNOTATION.asterisk())
        .from(NEW_ANNOTATION)
        .limit(pageSize)
        .offset(pageNumber)
        .fetch(this::mapToAnnotation);
  }

  public Map<Integer, List<JsonApiData>> getAnnotationsWithCount(int pageNumber, int pageSize) {
    int totalRecords = context.selectCount()
        .from(NEW_ANNOTATION)
        .fetchOne(0, int.class);
    Integer pageCount = totalRecords/pageSize + ((totalRecords % pageSize == 0) ? 0 : 1);

    var annotations = context.select(NEW_ANNOTATION.asterisk())
        .from(NEW_ANNOTATION)
        .limit(pageSize)
        .offset(pageNumber)
        .fetch(this::mapToJsonApiData);

    HashMap<Integer, List<JsonApiData>> pair = new HashMap<>();

    pair.put(pageCount, annotations);
    return pair;
  }


  public AnnotationResponse getAnnotationVersion(String id, int version) {
    return context.select(NEW_ANNOTATION.asterisk())
        .from(NEW_ANNOTATION)
        .where(NEW_ANNOTATION.ID.eq(id))
        .and(NEW_ANNOTATION.VERSION.eq(version))
        .fetchOne(this::mapToAnnotation);
  }


  private AnnotationResponse mapToAnnotation(Record dbRecord) {
    try {
      return new AnnotationResponse(
          dbRecord.get(NEW_ANNOTATION.ID),
          dbRecord.get(NEW_ANNOTATION.VERSION),
          dbRecord.get(NEW_ANNOTATION.TYPE),
          dbRecord.get(NEW_ANNOTATION.MOTIVATION),
          mapper.readTree(dbRecord.get(NEW_ANNOTATION.TARGET_BODY).data()),
          mapper.readTree(dbRecord.get(NEW_ANNOTATION.BODY).data()),
          dbRecord.get(NEW_ANNOTATION.PREFERENCE_SCORE),
          dbRecord.get(NEW_ANNOTATION.CREATOR),
          dbRecord.get(NEW_ANNOTATION.CREATED),
          mapper.readTree(dbRecord.get(NEW_ANNOTATION.GENERATOR_BODY).data()),
          dbRecord.get(NEW_ANNOTATION.GENERATED),
          dbRecord.get(NEW_ANNOTATION.DELETED)
      );
    } catch (JsonProcessingException e) {
      log.error("Failed to parse annotation body to Json", e);
      return null;
    }
  }

  private JsonApiData mapToJsonApiData(Record dbRecord){
    ObjectNode dataNode = mapper.createObjectNode();
    log.info(dbRecord.toString());
    try {
      dataNode.put("id", dbRecord.get(NEW_ANNOTATION.ID));
      dataNode.put("version", dbRecord.get(NEW_ANNOTATION.VERSION));
      dataNode.put("type", dbRecord.get(NEW_ANNOTATION.TYPE));
      dataNode.put("motivation", dbRecord.get(NEW_ANNOTATION.MOTIVATION));
      dataNode.set("target", mapper.readTree(dbRecord.get(NEW_ANNOTATION.TARGET_BODY).data()));
      dataNode.set("body", mapper.readTree(dbRecord.get(NEW_ANNOTATION.BODY).data()));
      dataNode.put("preferenceScore", dbRecord.get(NEW_ANNOTATION.PREFERENCE_SCORE));
      dataNode.put("creator", dbRecord.get(NEW_ANNOTATION.CREATOR));
      dataNode.put("created",String.valueOf(dbRecord.get(NEW_ANNOTATION.CREATED)));
      dataNode.put("created", String.valueOf(dbRecord.get(NEW_ANNOTATION.CREATED)));
      dataNode.set("generator", mapper.readTree(dbRecord.get(NEW_ANNOTATION.GENERATOR_BODY).data()));
      dataNode.put("generated", String.valueOf(dbRecord.get(NEW_ANNOTATION.GENERATED)));
      dataNode.put("deleted", String.valueOf(dbRecord.get(NEW_ANNOTATION.DELETED)));
      //dataNode.put("specimenName", dbRecord.get(NEW_DIGITAL_SPECIMEN.SPECIMEN_NAME));
    } catch (JsonProcessingException e) {
      log.error("Failed to parse annotation body to Json", e);
      return null;
    }
    return new JsonApiData(dbRecord.get(NEW_ANNOTATION.ID), dbRecord.get(NEW_ANNOTATION.TYPE), dataNode);
  }



  public List<AnnotationResponse> getForTarget(String id) {
    return context.select(NEW_ANNOTATION.asterisk())
        .distinctOn(NEW_ANNOTATION.ID)
        .from(NEW_ANNOTATION)
        .where(NEW_ANNOTATION.TARGET_ID.eq(id))
        .and(NEW_ANNOTATION.DELETED.isNull())
        .orderBy(NEW_ANNOTATION.ID, NEW_ANNOTATION.VERSION.desc())
        .fetch(this::mapToAnnotation);
  }

  public List<Integer> getAnnotationVersions(String id) {
    return context.select(NEW_ANNOTATION.VERSION).from(NEW_ANNOTATION)
        .where(NEW_ANNOTATION.ID.eq(id)).fetch(
            Record1::value1).stream().toList();
  }

  public int getAnnotationForUser(String id, String userId) {
    return context.select(NEW_ANNOTATION.ID)
        .distinctOn(NEW_ANNOTATION.ID)
        .from(NEW_ANNOTATION)
        .where(NEW_ANNOTATION.ID.eq(id))
        .and(NEW_ANNOTATION.CREATOR.eq(userId))
        .and(NEW_ANNOTATION.DELETED.isNull())
        .orderBy(NEW_ANNOTATION.ID, NEW_ANNOTATION.VERSION.desc())
        .fetch().size();
  }
}
