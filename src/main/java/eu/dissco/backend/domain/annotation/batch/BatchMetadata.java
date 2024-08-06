package eu.dissco.backend.domain.annotation.batch;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Value;

@Value
public class BatchMetadata {
  Integer placeInBatch = 1;
  @JsonProperty("searchParams")
  List<SearchParam> searchParams;

  @JsonCreator
  public BatchMetadata(List<SearchParam> searchParams){
    this.searchParams = searchParams;
  }
}
