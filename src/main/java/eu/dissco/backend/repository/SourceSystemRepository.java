package eu.dissco.backend.repository;

import static eu.dissco.backend.database.jooq.Tables.NEW_SOURCE_SYSTEM;

import eu.dissco.backend.database.jooq.Tables;
import eu.dissco.backend.domain.SourceSystemRecord;
import java.time.Instant;
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

  private SourceSystemRecord mapToSourceSystemRecord(Record record) {
    return new SourceSystemRecord(
        record.get(NEW_SOURCE_SYSTEM.ID),
        record.get(NEW_SOURCE_SYSTEM.CREATED),
        record.get(NEW_SOURCE_SYSTEM.NAME),
        record.get(NEW_SOURCE_SYSTEM.ENDPOINT),
        record.get(NEW_SOURCE_SYSTEM.DESCRIPTION),
        record.get(NEW_SOURCE_SYSTEM.MAPPING_ID)
    );
  }
}
