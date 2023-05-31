package eu.dissco.backend.repository;

import static eu.dissco.backend.database.jooq.Tables.NEW_ANNOTATION;
import static eu.dissco.backend.repository.RepositoryUtils.ONE_TO_CHECK_NEXT;
import static eu.dissco.backend.repository.RepositoryUtils.getOffset;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.domain.AnnotationResponse;
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

  public AnnotationResponse getAnnotation(String id) {
    return context.select(NEW_ANNOTATION.asterisk())
        .from(NEW_ANNOTATION)
        .where(NEW_ANNOTATION.ID.eq(id))
        .fetchOne(this::mapToAnnotation);
  }

  public List<AnnotationResponse> getAnnotations(int pageNumber, int pageSize) {
    var offset = getOffset(pageNumber, pageSize);
    var pageSizePlusOne = pageSize + ONE_TO_CHECK_NEXT;
    return context.select(NEW_ANNOTATION.asterisk())
        .from(NEW_ANNOTATION)
        .orderBy(NEW_ANNOTATION.CREATED.desc())
        .limit(pageSizePlusOne).offset(offset).fetch(this::mapToAnnotation);
  }

  public int getAnnotationForUser(String id, String userId) {
    return context.select(NEW_ANNOTATION.ID)
        .from(NEW_ANNOTATION)
        .where(NEW_ANNOTATION.ID.eq(id))
        .and(NEW_ANNOTATION.CREATOR.eq(userId))
        .and(NEW_ANNOTATION.DELETED.isNull())
        .fetch().size();
  }

  public List<AnnotationResponse> getAnnotationsForUser(String userId, int pageNumber,
      int pageSize) {
    int offset = getOffset(pageNumber, pageSize);
    var pageSizePlusOne = pageSize + ONE_TO_CHECK_NEXT;
    return context.select(NEW_ANNOTATION.asterisk())
        .from(NEW_ANNOTATION)
        .where(NEW_ANNOTATION.CREATOR.eq(userId))
        .orderBy(NEW_ANNOTATION.CREATED)
        .limit(pageSizePlusOne).offset(offset).fetch(this::mapToAnnotation);
  }

  public List<AnnotationResponse> getForTarget(String id) {
    return context.select(NEW_ANNOTATION.asterisk())
        .from(NEW_ANNOTATION)
        .where(NEW_ANNOTATION.TARGET_ID.eq(id))
        .and(NEW_ANNOTATION.DELETED.isNull())
        .fetch(this::mapToAnnotation);
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

}
