package eu.dissco.backend.repository;

import static eu.dissco.backend.database.jooq.Tables.NEW_SOURCE_SYSTEM;

import eu.dissco.backend.domain.SourceSystemRecord;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SourceSystemRepository {

  private final DSLContext context;

  public SourceSystemRecord getSourceSystemById(String id) {
    return context.select(NEW_SOURCE_SYSTEM.asterisk())
        .from(NEW_SOURCE_SYSTEM)
        .where(NEW_SOURCE_SYSTEM.ID.eq(id))
        .fetchOne(this::mapToSourceSystemRecord);
  }

  private SourceSystemRecord mapToSourceSystemRecord(Record dbRecord) {
    return new SourceSystemRecord(
        dbRecord.get(NEW_SOURCE_SYSTEM.ID),
        dbRecord.get(NEW_SOURCE_SYSTEM.CREATED),
        "SourceSystem",
        dbRecord.get(NEW_SOURCE_SYSTEM.NAME),
        dbRecord.get(NEW_SOURCE_SYSTEM.ENDPOINT),
        dbRecord.get(NEW_SOURCE_SYSTEM.DESCRIPTION),
        dbRecord.get(NEW_SOURCE_SYSTEM.MAPPING_ID)
    );
  }
}
