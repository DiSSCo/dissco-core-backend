package eu.dissco.backend.domain.annotation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Generator {

  @JsonProperty("ods:type")
  private String odsType;
  @JsonProperty("foaf:name")
  private String foafName;
  @JsonProperty("ods:id")
  private String odsId;

  public Generator withOdsType(String odsType) {
    this.odsType = odsType;
    return this;
  }

  public Generator withFoafName(String foafName) {
    this.foafName = foafName;
    return this;
  }

  public Generator withOdsId(String odsId) {
    this.odsId = odsId;
    return this;
  }


}
