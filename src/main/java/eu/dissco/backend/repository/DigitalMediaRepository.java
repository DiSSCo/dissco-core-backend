package eu.dissco.backend.repository;

import static eu.dissco.backend.database.jooq.Tables.DIGITAL_MEDIA_OBJECT;
import static eu.dissco.backend.repository.RepositoryUtils.DOI_STRING;
import static eu.dissco.backend.repository.RepositoryUtils.ONE_TO_CHECK_NEXT;
import static eu.dissco.backend.repository.RepositoryUtils.getOffset;
import static eu.dissco.backend.repository.RepositoryUtils.mapOriginalDataToJson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.exceptions.DisscoJsonBMappingException;
import eu.dissco.backend.schema.DigitalMedia;
import java.util.Date;
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
public class DigitalMediaRepository {

  private final ObjectMapper mapper;
  private final DSLContext context;

  public List<DigitalMedia> getDigitalMediaObjects(int pageNumber, int pageSize) {
    int offset = getOffset(pageNumber, pageSize);
    var pageSizePlusOne = pageSize + ONE_TO_CHECK_NEXT;
    return context.select(DIGITAL_MEDIA_OBJECT.asterisk())
        .from(DIGITAL_MEDIA_OBJECT)
        .offset(offset).limit(pageSizePlusOne).fetch(this::mapToMultiMediaObject);
  }

  public DigitalMedia getLatestDigitalMediaObjectById(String id) {
    return context.select(DIGITAL_MEDIA_OBJECT.asterisk())
        .from(DIGITAL_MEDIA_OBJECT)
        .where(DIGITAL_MEDIA_OBJECT.ID.eq(id))
        .fetchOne(this::mapToMultiMediaObject);
  }

  public List<DigitalMedia> getDigitalMediaForSpecimen(String id) {
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

  public JsonNode getMediaOriginalData(String id) {
    return context.select(DIGITAL_MEDIA_OBJECT.ORIGINAL_DATA)
        .from(DIGITAL_MEDIA_OBJECT)
        .where(DIGITAL_MEDIA_OBJECT.ID.eq(id))
        .fetchOne(data -> mapOriginalDataToJson(data, mapper));
  }

  private DigitalMedia mapToMultiMediaObject(Record dbRecord) {
    try {
      return mapper.readValue(dbRecord.get(DIGITAL_MEDIA_OBJECT.DATA).data(),
              DigitalMedia.class)
          .withId(DOI_STRING + dbRecord.get(DIGITAL_MEDIA_OBJECT.ID))
          .withOdsID(DOI_STRING + dbRecord.get(DIGITAL_MEDIA_OBJECT.ID))
          .withOdsType(dbRecord.get(DIGITAL_MEDIA_OBJECT.TYPE))
          .withDctermsCreated(Date.from(dbRecord.get(DIGITAL_MEDIA_OBJECT.CREATED)))
          .withOdsVersion(dbRecord.get(DIGITAL_MEDIA_OBJECT.VERSION));
    } catch (JsonProcessingException e) {
      throw new DisscoJsonBMappingException(
          "Failed to parse jsonb field to json: " + dbRecord.get(DIGITAL_MEDIA_OBJECT.DATA).data(),
          e);
    }
  }

}
