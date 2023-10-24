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
import eu.dissco.backend.exceptions.DatabaseException;
import java.time.Instant;
import java.util.List;
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

  public void createAnnotationRecord(Annotation annotation) {
    try {
      context.insertInto(ANNOTATION).set(ANNOTATION.ID, annotation.getOdsId())
          .set(ANNOTATION.VERSION, annotation.getOdsVersion())
          .set(ANNOTATION.TYPE, annotation.getRdfType())
          .set(ANNOTATION.MOTIVATION, annotation.getOaMotivation().toString())
          .set(ANNOTATION.MOTIVATED_BY, annotation.getOaMotivatedBy())
          .set(ANNOTATION.TARGET_ID, annotation.getOaTarget().getOdsId())
          .set(ANNOTATION.TARGET, JSONB.jsonb(mapper.writeValueAsString(annotation.getOaTarget())))
          .set(ANNOTATION.BODY, JSONB.jsonb(mapper.writeValueAsString(annotation.getOaBody())))
          .set(ANNOTATION.AGGREGATE_RATING,
              JSONB.jsonb(mapper.writeValueAsString(annotation.getOdsAggregateRating())))
          .set(ANNOTATION.CREATOR,
              JSONB.jsonb(mapper.writeValueAsString(annotation.getOaCreator())))
          .set(ANNOTATION.CREATOR_ID, annotation.getOaCreator().getOdsId())
          .set(ANNOTATION.CREATED, annotation.getDcTermsCreated())
          .set(ANNOTATION.GENERATOR,
              JSONB.jsonb(mapper.writeValueAsString(annotation.getAsGenerator())))
          .set(ANNOTATION.GENERATED, annotation.getOaGenerated())
          .set(ANNOTATION.LAST_CHECKED, annotation.getDcTermsCreated())
          .onConflict(ANNOTATION.ID).doUpdate()
          .set(ANNOTATION.VERSION, annotation.getOdsVersion())
          .set(ANNOTATION.TYPE, annotation.getRdfType())
          .set(ANNOTATION.MOTIVATION, annotation.getOaMotivation().toString())
          .set(ANNOTATION.MOTIVATED_BY, annotation.getOaMotivatedBy())
          .set(ANNOTATION.TARGET_ID, annotation.getOaTarget().getOdsId())
          .set(ANNOTATION.TARGET, JSONB.jsonb(mapper.writeValueAsString(annotation.getOaTarget())))
          .set(ANNOTATION.BODY, JSONB.jsonb(mapper.writeValueAsString(annotation.getOaBody())))
          .set(ANNOTATION.AGGREGATE_RATING,
              JSONB.jsonb(mapper.writeValueAsString(annotation.getOdsAggregateRating())))
          .set(ANNOTATION.CREATOR,
              JSONB.jsonb(mapper.writeValueAsString(annotation.getOaCreator())))
          .set(ANNOTATION.CREATOR_ID, annotation.getOaCreator().getOdsId())
          .set(ANNOTATION.CREATED, annotation.getDcTermsCreated())
          .set(ANNOTATION.GENERATOR,
              JSONB.jsonb(mapper.writeValueAsString(annotation.getAsGenerator())))
          .set(ANNOTATION.GENERATED, annotation.getOaGenerated())
          .set(ANNOTATION.LAST_CHECKED, annotation.getDcTermsCreated())
          .execute();
    } catch (JsonProcessingException e) {
      log.error("Failed to post data to database, unable to parse JSON to JSONB", e);
      throw new DatabaseException(e.getMessage());
    }
  }

  public int updateLastChecked(Annotation currentAnnotation) {
    return context.update(ANNOTATION)
        .set(ANNOTATION.LAST_CHECKED, Instant.now())
        .where(ANNOTATION.ID.eq(currentAnnotation.getOdsId()))
        .execute();
  }

  public void archiveAnnotation(String id) {
    context.update(ANNOTATION)
        .set(ANNOTATION.DELETED_ON, Instant.now())
        .where(ANNOTATION.ID.eq(id))
        .execute();
  }

  public void rollbackAnnotation(String id) {
    context.delete(ANNOTATION).where(ANNOTATION.ID.eq(id)).execute();
  }

  private Annotation mapToAnnotation(Record dbRecord) {
    try {
      return new Annotation()
          .withOdsId(dbRecord.get(ANNOTATION.ID))
          .withRdfType(dbRecord.get(ANNOTATION.TYPE))
          .withOdsVersion(dbRecord.get(ANNOTATION.VERSION))
          .withOaMotivation(Motivation.fromString(dbRecord.get(ANNOTATION.MOTIVATION)))
          .withOaMotivatedBy(dbRecord.get(ANNOTATION.MOTIVATED_BY))
          .withOaTarget(mapper.readValue(dbRecord.get(ANNOTATION.TARGET).data(), Target.class))
          .withOaBody(mapper.readValue(dbRecord.get(ANNOTATION.BODY).data(), Body.class))
          .withOaCreator(mapper.readValue(dbRecord.get(ANNOTATION.CREATOR).data(), Creator.class))
          .withDcTermsCreated(dbRecord.get(ANNOTATION.CREATED))
          .withOdsDeletedOn(dbRecord.get(ANNOTATION.DELETED_ON))
          .withAsGenerator(
              mapper.readValue(dbRecord.get(ANNOTATION.GENERATOR).data(), Generator.class))
          .withOaGenerated(dbRecord.get(ANNOTATION.GENERATED))
          .withOdsAggregateRating(mapper.readValue(dbRecord.get(ANNOTATION.AGGREGATE_RATING).data(),
              AggregateRating.class));
    } catch (JsonProcessingException e) {
      log.error("Failed to parse annotation body to Json", e);
      return null;
    }
  }

}
