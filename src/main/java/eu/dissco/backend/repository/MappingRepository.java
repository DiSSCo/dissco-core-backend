package eu.dissco.backend.repository;

import static eu.dissco.backend.database.jooq.Tables.NEW_MAPPING;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.domain.MappingRecord;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MappingRepository {

  private final ObjectMapper mapper;
  private final DSLContext context;

  public MappingRecord getMappingById(String id) {
    return context.select(NEW_MAPPING.asterisk())
        .distinctOn(NEW_MAPPING.ID)
        .from(NEW_MAPPING)
        .where(NEW_MAPPING.ID.eq(id))
        .and(NEW_MAPPING.DELETED.isNull())
        .orderBy(NEW_MAPPING.ID, NEW_MAPPING.VERSION.desc())
        .fetchOne(this::mapToMappingRecord);
  }

  private MappingRecord mapToMappingRecord(Record record) {
    try {
      return new MappingRecord(
          record.get(NEW_MAPPING.ID),
          record.get(NEW_MAPPING.VERSION),
          record.get(NEW_MAPPING.CREATED),
          record.get(NEW_MAPPING.CREATOR),
          record.get(NEW_MAPPING.NAME),
          record.get(NEW_MAPPING.DESCRIPTION),
          mapper.readTree(record.get(NEW_MAPPING.MAPPING).data())
      );
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

  }
}
