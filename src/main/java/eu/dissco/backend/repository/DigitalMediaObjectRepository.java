package eu.dissco.backend.repository;

import static eu.dissco.backend.database.jooq.Tables.NEW_DIGITAL_MEDIA_OBJECT;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.domain.DigitalMediaObject;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record1;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DigitalMediaObjectRepository {

  private final ObjectMapper mapper;
  private final DSLContext context;

  public DigitalMediaObject getLatestDigitalMediaById(String id) {
    return context.select(NEW_DIGITAL_MEDIA_OBJECT.asterisk())
        .distinctOn(NEW_DIGITAL_MEDIA_OBJECT.ID)
        .from(NEW_DIGITAL_MEDIA_OBJECT)
        .where(NEW_DIGITAL_MEDIA_OBJECT.ID.eq(id))
        .orderBy(NEW_DIGITAL_MEDIA_OBJECT.ID, NEW_DIGITAL_MEDIA_OBJECT.VERSION.desc())
        .fetchOne(this::mapToMultiMediaObject);
  }

  private DigitalMediaObject mapToMultiMediaObject(Record dbRecord) {
    try {
      return new DigitalMediaObject(
          dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.ID),
          dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.VERSION),
          dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.TYPE),
          dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.DIGITAL_SPECIMEN_ID),
          dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.MEDIA_URL),
          dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.FORMAT),
          dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.SOURCE_SYSTEM_ID),
          mapper.readTree(dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.DATA).data()),
          mapper.readTree(dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.ORIGINAL_DATA).data())
      );
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public List<DigitalMediaObject> getForDigitalSpecimen(String id) {
    return context.select(NEW_DIGITAL_MEDIA_OBJECT.asterisk())
        .distinctOn(NEW_DIGITAL_MEDIA_OBJECT.ID)
        .from(NEW_DIGITAL_MEDIA_OBJECT)
        .where(NEW_DIGITAL_MEDIA_OBJECT.DIGITAL_SPECIMEN_ID.eq(id))
        .orderBy(NEW_DIGITAL_MEDIA_OBJECT.ID, NEW_DIGITAL_MEDIA_OBJECT.VERSION.desc())
        .fetch(this::mapToMultiMediaObject);
  }

  public List<Integer> getDigitalMediaVersions(String id) {
    return context.select(NEW_DIGITAL_MEDIA_OBJECT.VERSION)
        .from(NEW_DIGITAL_MEDIA_OBJECT)
        .where(NEW_DIGITAL_MEDIA_OBJECT.ID.eq(id))
        .fetch(Record1::value1).stream().toList();
  }

  public DigitalMediaObject getDigitalMediaByVersion(String id, int version) {
    return context.select(NEW_DIGITAL_MEDIA_OBJECT.asterisk())
        .from(NEW_DIGITAL_MEDIA_OBJECT)
        .where(NEW_DIGITAL_MEDIA_OBJECT.ID.eq(id))
        .and(NEW_DIGITAL_MEDIA_OBJECT.VERSION.eq(version))
        .fetchOne(this::mapToMultiMediaObject);
  }

  public List<DigitalMediaObject> getDigitalMediaForSpecimen(String id) {
    return context.select(NEW_DIGITAL_MEDIA_OBJECT.asterisk())
        .distinctOn(NEW_DIGITAL_MEDIA_OBJECT.ID)
        .from(NEW_DIGITAL_MEDIA_OBJECT)
        .where(NEW_DIGITAL_MEDIA_OBJECT.DIGITAL_SPECIMEN_ID.eq(id))
        .orderBy(NEW_DIGITAL_MEDIA_OBJECT.ID, NEW_DIGITAL_MEDIA_OBJECT.VERSION.desc())
        .fetch(this::mapToMultiMediaObject);
  }

  public List<DigitalMediaObject> getDigitalMediaObject(int pageNumber, int pageSize) {
    var offset = 0;
    if (pageNumber > 1) {
      offset = offset + (pageSize * (pageNumber - 1));
    }
    return context.select(NEW_DIGITAL_MEDIA_OBJECT.asterisk())
        .from(NEW_DIGITAL_MEDIA_OBJECT)
        .offset(offset)
        .limit(pageSize)
        .fetch(this::mapToMultiMediaObject);
  }
}
