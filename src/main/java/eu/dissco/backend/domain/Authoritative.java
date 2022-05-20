package eu.dissco.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Authoritative {

  @JsonIgnore
  String id;
  @JsonProperty("ods:midsLevel")
  int midslevel;
  @JsonProperty("ods:curatedObjectID")
  String curatedObjectID;
  @JsonProperty("ods:physicalSpecimenId")
  String physicalSpecimenId;
  @JsonProperty("ods:institution")
  String institution;
  @JsonProperty("ods:institutionCode")
  String institutionCode;
  @JsonProperty("ods:materialType")
  String materialType;
  @JsonProperty("ods:name")
  String name;
}
