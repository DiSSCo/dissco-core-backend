package eu.dissco.backend.domain.annotation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class Creator {

  @JsonProperty("ods:type")
  private String odsType;
  @JsonProperty("foaf:name")
  private String foafName;
  @JsonProperty("ods:id")
  private String odsId;

  public Creator withOdsType(String odsType) {
    this.odsType = odsType;
    return this;
  }

  public Creator withFoafName(String foafName) {
    this.foafName = foafName;
    return this;
  }

  public Creator withOdsId(String odsId) {
    this.odsId = odsId;
    return this;
  }

}
