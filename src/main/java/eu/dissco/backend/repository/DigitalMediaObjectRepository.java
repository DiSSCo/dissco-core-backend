package eu.dissco.backend.repository;

import static eu.dissco.backend.database.jooq.Tables.NEW_DIGITAL_MEDIA_OBJECT;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.domain.MultiMediaObject;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DigitalMediaObjectRepository {

  private final ObjectMapper mapper;
  private final DSLContext context;

  public MultiMediaObject getLatestMultiMediaById(String id) {
    return context.select(NEW_DIGITAL_MEDIA_OBJECT.asterisk())
        .distinctOn(NEW_DIGITAL_MEDIA_OBJECT.ID)
        .from(NEW_DIGITAL_MEDIA_OBJECT)
        .where(NEW_DIGITAL_MEDIA_OBJECT.ID.eq(id))
        .orderBy(NEW_DIGITAL_MEDIA_OBJECT.ID, NEW_DIGITAL_MEDIA_OBJECT.VERSION.desc())
        .fetchOne(this::mapToMultiMediaObject);
  }

  private MultiMediaObject mapToMultiMediaObject(Record record) {
    try {
      return new MultiMediaObject(
          record.get(NEW_DIGITAL_MEDIA_OBJECT.ID),
          record.get(NEW_DIGITAL_MEDIA_OBJECT.VERSION),
          record.get(NEW_DIGITAL_MEDIA_OBJECT.TYPE),
          record.get(NEW_DIGITAL_MEDIA_OBJECT.DIGITAL_SPECIMEN_ID),
          record.get(NEW_DIGITAL_MEDIA_OBJECT.MEDIA_URL),
          record.get(NEW_DIGITAL_MEDIA_OBJECT.FORMAT),
          record.get(NEW_DIGITAL_MEDIA_OBJECT.SOURCE_SYSTEM_ID),
          mapper.readTree(record.get(NEW_DIGITAL_MEDIA_OBJECT.DATA).data()),
          mapper.readTree(record.get(NEW_DIGITAL_MEDIA_OBJECT.ORIGINAL_DATA).data())
      );
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
