package eu.dissco.backend.repository;

import static eu.dissco.backend.database.jooq.Tables.ANNOTATION;
import static eu.dissco.backend.repository.RepositoryUtils.ONE_TO_CHECK_NEXT;
import static eu.dissco.backend.repository.RepositoryUtils.getOffset;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.exceptions.DisscoJsonBMappingException;
import eu.dissco.backend.schema.Annotation;
import java.util.List;
import java.util.Optional;
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

  public Annotation getAnnotation(String id) {
    return context.select(ANNOTATION.DATA)
        .from(ANNOTATION)
        .where(ANNOTATION.ID.eq(id))
        .fetchOne(this::mapToAnnotation);
  }

  public List<Annotation> getAnnotations(int pageNumber, int pageSize) {
    var offset = getOffset(pageNumber, pageSize);
    var pageSizePlusOne = pageSize + ONE_TO_CHECK_NEXT;
    return context.select(ANNOTATION.asterisk())
        .from(ANNOTATION)
        .orderBy(ANNOTATION.CREATED.desc())
        .limit(pageSizePlusOne).offset(offset).fetch(this::mapToAnnotation);
  }

  public Optional<Annotation> getActiveAnnotationForUser(String id, String userId) {
    return context.select(ANNOTATION.asterisk())
        .from(ANNOTATION)
        .where(ANNOTATION.ID.eq(id))
        .and(ANNOTATION.CREATOR_ID.eq(userId))
        .and(ANNOTATION.TOMBSTONED_ON.isNull())
        .fetchOptional(this::mapToAnnotation);
  }

  public List<Annotation> getForTarget(String id) {
    return context.select(ANNOTATION.DATA)
        .from(ANNOTATION)
        .where(ANNOTATION.TARGET_ID.eq(id))
        .and(ANNOTATION.TOMBSTONED_ON.isNull())
        .fetch(this::mapToAnnotation);
  }

  private Annotation mapToAnnotation(Record dbRecord) {
    try {
      return mapper.readValue(dbRecord.get(ANNOTATION.DATA).data(),
          Annotation.class);
    } catch (JsonProcessingException e) {
      log.error("Failed to get data from database, Unable to parse JSONB to JSON", e);
      throw new DisscoJsonBMappingException("Unable to convert jsonb to annotation", e);
    }
  }

}
