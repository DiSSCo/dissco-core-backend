package eu.dissco.backend.repository;

import static eu.dissco.backend.database.jooq.Tables.DIGITAL_SPECIMEN;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.exceptions.DisscoJsonBMappingException;
import org.jooq.Record;

public class RepositoryUtils {

  protected static final int ONE_TO_CHECK_NEXT = 1;

  private RepositoryUtils() {
    // Utility class
  }

  protected static int getOffset(int pageNumber, int pageSize) {
    int offset = 0;
    if (pageNumber > 1) {
      offset = offset + (pageSize * (pageNumber - 1));
    }
    return offset;
  }


  public static JsonNode mapOriginalDataToJson(Record result, ObjectMapper mapper) {
    var originalData = result.get(DIGITAL_SPECIMEN.ORIGINAL_DATA);
    try {
      return mapper.readTree(originalData.data());
    } catch (JsonProcessingException e) {
      throw new DisscoJsonBMappingException(
          "Failed to parse jsonb field to json: " + originalData.data(), e);
    }
  }

}
