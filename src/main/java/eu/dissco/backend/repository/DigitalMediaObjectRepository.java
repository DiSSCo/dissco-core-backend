package eu.dissco.backend.repository;

import static eu.dissco.backend.database.jooq.Tables.DIGITAL_MEDIA_OBJECT;
import static eu.dissco.backend.database.jooq.Tables.DIGITAL_SPECIMEN;
import static eu.dissco.backend.repository.RepositoryUtils.DOI_STRING;
import static eu.dissco.backend.repository.RepositoryUtils.ONE_TO_CHECK_NEXT;
import static eu.dissco.backend.repository.RepositoryUtils.getOffset;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.domain.DigitalMediaObjectWrapper;
import eu.dissco.backend.exceptions.DisscoJsonBMappingException;
import eu.dissco.backend.schema.DigitalEntity;
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

  public List<DigitalMediaObjectWrapper> getDigitalMediaObjects(int pageNumber, int pageSize) {
    int offset = getOffset(pageNumber, pageSize);
    var pageSizePlusOne = pageSize + ONE_TO_CHECK_NEXT;
    return context.select(DIGITAL_MEDIA_OBJECT.asterisk())
        .from(DIGITAL_MEDIA_OBJECT)
        .offset(offset).limit(pageSizePlusOne).fetch(this::mapToMultiMediaObject);
  }

  public DigitalMediaObjectWrapper getLatestDigitalMediaObjectById(String id) {
    return context.select(DIGITAL_MEDIA_OBJECT.asterisk())
        .from(DIGITAL_MEDIA_OBJECT)
        .where(DIGITAL_MEDIA_OBJECT.ID.eq(id))
        .fetchOne(this::mapToMultiMediaObject);
  }

  public List<DigitalMediaObjectWrapper> getDigitalMediaForSpecimen(String id) {
    return context.select(DIGITAL_MEDIA_OBJECT.asterisk())
        .from(DIGITAL_MEDIA_OBJECT)
        .where(DIGITAL_MEDIA_OBJECT.DIGITAL_SPECIMEN_ID.eq(id))
        .fetch(this::mapToMultiMediaObject);
  }

  public List<String> getDigitalMediaIdsForSpecimen(String id) {
    return context.select(DIGITAL_MEDIA_OBJECT.ID)
        .from(DIGITAL_MEDIA_OBJECT)
        .where(DIGITAL_MEDIA_OBJECT.DIGITAL_SPECIMEN_ID.eq(id))
        .fetch(Record1::value1);
  }

  private DigitalMediaObjectWrapper mapToMultiMediaObject(Record dbRecord) {
    try {
      var digitalMediaObject = mapper.readValue(dbRecord.get(DIGITAL_MEDIA_OBJECT.DATA).data(),
          DigitalEntity.class)
          .withOdsId(DOI_STRING + dbRecord.get(DIGITAL_MEDIA_OBJECT.ID))
          .withOdsType(dbRecord.get(DIGITAL_MEDIA_OBJECT.TYPE))
          .withOdsCreated(dbRecord.get(DIGITAL_MEDIA_OBJECT.CREATED).toString())
          .withOdsVersion(dbRecord.get(DIGITAL_MEDIA_OBJECT.VERSION));
      return new DigitalMediaObjectWrapper(
          digitalMediaObject,
          mapper.readTree(dbRecord.get(DIGITAL_MEDIA_OBJECT.ORIGINAL_DATA).data()));
    } catch (JsonProcessingException e) {
      throw new DisscoJsonBMappingException(
          "Failed to parse jsonb field to json: " + dbRecord.get(DIGITAL_MEDIA_OBJECT.DATA).data(), e);
    }
  }

}
