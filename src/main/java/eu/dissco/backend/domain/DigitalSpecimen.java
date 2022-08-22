package eu.dissco.backend.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class DigitalSpecimen {

  @JsonProperty("@id")
  private String id;
  @JsonProperty("@type")
  private String type;
  @JsonProperty("ods:authoritative")
  private Authoritative authoritative;
  @JsonProperty("ods:images")
  private List<Image> images;
  @JsonProperty("ods:unmapped")
  private JsonNode unmapped;

}
