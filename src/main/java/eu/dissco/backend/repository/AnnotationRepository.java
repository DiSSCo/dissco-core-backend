package eu.dissco.backend.repository;

import static eu.dissco.backend.database.jooq.Tables.ANNOTATION;
import static eu.dissco.backend.repository.RepositoryUtils.ONE_TO_CHECK_NEXT;
import static eu.dissco.backend.repository.RepositoryUtils.getOffset;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.domain.annotation.AggregateRating;
import eu.dissco.backend.domain.annotation.Annotation;
import eu.dissco.backend.domain.annotation.Body;
import eu.dissco.backend.domain.annotation.Creator;
import eu.dissco.backend.domain.annotation.Generator;
import eu.dissco.backend.domain.annotation.Motivation;
import eu.dissco.backend.domain.annotation.Target;
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

  public Annotation getAnnotation(String id) {
    return context.select(ANNOTATION.asterisk())
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

  public int getAnnotationForUser(String id, String userId) {
    return context.select(ANNOTATION.ID)
        .from(ANNOTATION)
        .where(ANNOTATION.ID.eq(id))
        .and(ANNOTATION.CREATOR_ID.eq(userId))
        .and(ANNOTATION.DELETED_ON.isNull())
        .fetch().size();
  }

  public List<Annotation> getForTarget(String id) {
    return context.select(ANNOTATION.asterisk())
        .from(ANNOTATION)
        .where(ANNOTATION.TARGET_ID.eq(id))
        .and(ANNOTATION.DELETED_ON.isNull())
        .fetch(this::mapToAnnotation);
  }

  private Annotation mapToAnnotation(Record dbRecord) {
    try {
      return Annotation.builder()
          .odsId(dbRecord.get(ANNOTATION.ID))
          .odsVersion(dbRecord.get(ANNOTATION.VERSION))
          .oaMotivation(Motivation.fromString(dbRecord.get(ANNOTATION.MOTIVATION)))
          .oaMotivatedBy(dbRecord.get(ANNOTATION.MOTIVATED_BY))
          .oaTarget(mapper.readValue(dbRecord.get(ANNOTATION.TARGET).data(), Target.class))
          .oaBody(mapper.readValue(dbRecord.get(ANNOTATION.BODY).data(), Body.class))
          .oaCreator(mapper.readValue(dbRecord.get(ANNOTATION.CREATOR).data(), Creator.class))
          .dcTermsCreated(dbRecord.get(ANNOTATION.CREATED))
          .odsDeletedOn(dbRecord.get(ANNOTATION.DELETED_ON))
          .asGenerator(
              mapper.readValue(dbRecord.get(ANNOTATION.GENERATOR).data(), Generator.class))
          .oaGenerated(dbRecord.get(ANNOTATION.GENERATED))
          .odsAggregateRating(mapper.readValue(dbRecord.get(ANNOTATION.AGGREGATE_RATING).data(),
              AggregateRating.class))
          .odsBatchId(dbRecord.get(ANNOTATION.BATCH_ID))
          .build();
    } catch (JsonProcessingException e) {
      log.error("Failed to parse annotations body to Json", e);
      return null;
    }
  }

}
