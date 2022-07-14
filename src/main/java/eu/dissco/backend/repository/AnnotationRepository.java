package eu.dissco.backend.repository;

import static eu.dissco.backend.database.jooq.Tables.ANNOTATION;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.domain.AnnotationRequest;
import eu.dissco.backend.domain.AnnotationResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class AnnotationRepository {

  private final DSLContext context;
  private final ObjectMapper mapper;

  public void saveAnnotation(AnnotationRequest annotation, String userId) {
    context.insertInto(ANNOTATION)
        .set(ANNOTATION.ID, "test/" + UUID.randomUUID().toString())
        .set(ANNOTATION.TYPE, annotation.type())
        .set(ANNOTATION.BODY, JSONB.jsonb(annotation.body().toString()))
        .set(ANNOTATION.TARGET, annotation.target())
        .set(ANNOTATION.CREATOR, userId)
        .set(ANNOTATION.LAST_UPDATED, Instant.now())
        .execute();
  }

  public List<AnnotationResponse> getAnnotationsForUser(String userId) {
    return context.select(ANNOTATION.asterisk()).from(ANNOTATION)
        .where(ANNOTATION.CREATOR.eq(userId)).fetch(this::mapToAnnotation);
  }

  public List<AnnotationResponse> getAnnotations(String id) {
    return context.select(ANNOTATION.asterisk()).from(ANNOTATION).where(ANNOTATION.TARGET.eq(id))
        .fetch(this::mapToAnnotation);
  }

  private AnnotationResponse mapToAnnotation(Record dbRecord) {
    try {
      return new AnnotationResponse(
          dbRecord.get(ANNOTATION.ID),
          dbRecord.get(ANNOTATION.TYPE),
          mapper.readTree(dbRecord.get(ANNOTATION.BODY).toString()),
          dbRecord.get(ANNOTATION.TARGET),
          dbRecord.get(ANNOTATION.LAST_UPDATED),
          dbRecord.get(ANNOTATION.CREATOR)
      );
    } catch (JsonProcessingException e) {
      log.error("Failed to parse annotation body to Json", e);
      return null;
    }
  }

  public void updateAnnotation(AnnotationRequest annotation, String userId) {
    context.update(ANNOTATION)
        .set(ANNOTATION.TYPE, annotation.type())
        .set(ANNOTATION.BODY, JSONB.jsonb(annotation.body().toString()))
        .set(ANNOTATION.TARGET, annotation.target())
        .set(ANNOTATION.CREATOR, userId)
        .set(ANNOTATION.LAST_UPDATED, Instant.now())
        .where(ANNOTATION.ID.eq(annotation.id()))
        .execute();
  }

  public AnnotationResponse getAnnotation(String id) {
    return context.select(ANNOTATION.asterisk()).from(ANNOTATION).where(ANNOTATION.ID.eq(id))
        .fetchOne(this::mapToAnnotation);
  }

  public void deleteAnnotation(String id) {
    context.delete(ANNOTATION).where(ANNOTATION.ID.eq(id)).execute();
  }
}
