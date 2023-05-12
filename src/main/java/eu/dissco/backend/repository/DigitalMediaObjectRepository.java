package eu.dissco.backend.repository;

import static eu.dissco.backend.database.jooq.Tables.NEW_DIGITAL_MEDIA_OBJECT;
import static eu.dissco.backend.database.jooq.Tables.NEW_DIGITAL_SPECIMEN;
import static eu.dissco.backend.repository.RepositoryUtils.HANDLE_STRING;
import static eu.dissco.backend.repository.RepositoryUtils.getOffset;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.domain.DigitalMediaObject;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record1;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class DigitalMediaObjectRepository {

  private final ObjectMapper mapper;
  private final DSLContext context;

  public List<DigitalMediaObject> getDigitalMediaObjects(int pageNumber, int pageSize) {
    int offset = getOffset(pageNumber, pageSize);
    return context.select(NEW_DIGITAL_SPECIMEN.SPECIMEN_NAME, NEW_DIGITAL_SPECIMEN.VERSION,
            NEW_DIGITAL_SPECIMEN.ID, NEW_DIGITAL_MEDIA_OBJECT.asterisk()).from(NEW_DIGITAL_SPECIMEN)
        .join(NEW_DIGITAL_MEDIA_OBJECT)
        .on(NEW_DIGITAL_SPECIMEN.ID.eq(NEW_DIGITAL_MEDIA_OBJECT.DIGITAL_SPECIMEN_ID))
        .offset(offset).limit(pageSize).fetch(this::mapToMultiMediaObject);
  }

  public DigitalMediaObject getLatestDigitalMediaObjectById(String id) {
    return context.select(NEW_DIGITAL_SPECIMEN.SPECIMEN_NAME, NEW_DIGITAL_SPECIMEN.VERSION,
            NEW_DIGITAL_SPECIMEN.ID, NEW_DIGITAL_MEDIA_OBJECT.asterisk())
        .from(NEW_DIGITAL_SPECIMEN)
        .join(NEW_DIGITAL_MEDIA_OBJECT)
        .on(NEW_DIGITAL_SPECIMEN.ID.eq(NEW_DIGITAL_MEDIA_OBJECT.DIGITAL_SPECIMEN_ID))
        .where(NEW_DIGITAL_MEDIA_OBJECT.ID.eq(id))
        .fetchOne(this::mapToMultiMediaObject);
  }

  public List<DigitalMediaObject> getDigitalMediaForSpecimen(String id) {
    return context.select(NEW_DIGITAL_MEDIA_OBJECT.asterisk())
        .from(NEW_DIGITAL_MEDIA_OBJECT)
        .where(NEW_DIGITAL_MEDIA_OBJECT.DIGITAL_SPECIMEN_ID.eq(id))
        .fetch(this::mapToMultiMediaObject);
  }

  public List<String> getDigitalMediaIdsForSpecimen(String id) {
    return context.select(NEW_DIGITAL_MEDIA_OBJECT.ID)
        .from(NEW_DIGITAL_MEDIA_OBJECT)
        .where(NEW_DIGITAL_MEDIA_OBJECT.DIGITAL_SPECIMEN_ID.eq(id))
        .fetch(Record1::value1);
  }

  private DigitalMediaObject mapToMultiMediaObject(Record dbRecord) {
    try {
      return new DigitalMediaObject(
          HANDLE_STRING + dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.ID),
          dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.VERSION),
          dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.CREATED),
          dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.TYPE),
          HANDLE_STRING + dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.DIGITAL_SPECIMEN_ID),
          dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.MEDIA_URL),
          dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.FORMAT),
          HANDLE_STRING + dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.SOURCE_SYSTEM_ID),
          mapper.readTree(dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.DATA).data()),
          mapper.readTree(dbRecord.get(NEW_DIGITAL_MEDIA_OBJECT.ORIGINAL_DATA).data()));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

}
