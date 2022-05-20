package eu.dissco.backend.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class Image {

  @JsonProperty("ods:imageURI")
  String imageUri;
  @JsonProperty("additional_info")
  List<JsonNode> additionalInfo;
}
