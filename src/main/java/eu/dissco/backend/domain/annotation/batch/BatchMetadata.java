package eu.dissco.backend.domain.annotation.batch;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;


@Value
public class BatchMetadata {
  Integer placeInBatch = 1;
  @JsonProperty("searchParams")
  List<SearchParam> searchParams;
}
