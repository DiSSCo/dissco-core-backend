package eu.dissco.backend.domain;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public enum FdoType {
  @JsonProperty("https://doi.org/21.T11148/894b1e6cad57e921764e")
  @JsonAlias("ods:DigitalSpecimen")
  DIGITAL_SPECIMEN("https://doi.org/21.T11148/894b1e6cad57e921764e", "ods:DigitalSpecimen"),

  @JsonProperty("https://doi.org/21.T11148/bbad8c4e101e8af01115")
  @JsonAlias("ods:DigitalMedia")
  DIGITAL_MEDIA("https://doi.org/21.T11148/bbad8c4e101e8af01115", "ods:DigitalMedia"),

  @JsonProperty("https://doi.org/21.T11148/cf458ca9ee1d44a5608f")
  @JsonAlias("ods:Annotation")
  ANNOTATION("https://doi.org/21.T11148/cf458ca9ee1d44a5608f", "ods:Annotation"),

  @JsonProperty("https://doi.org/21.T11148/417a4f472f60f7974c12")
  @JsonAlias("ods:SourceSystem")
  SOURCE_SYSTEM("https://doi.org/21.T11148/417a4f472f60f7974c12", "ods:SourceSystem"),

  @JsonProperty("https://doi.org/21.T11148/ce794a6f4df42eb7e77e")
  @JsonAlias("ods:DataMapping")
  DATA_MAPPING("https://doi.org/21.T11148/ce794a6f4df42eb7e77e", "ods:DataMapping"),

  @JsonProperty("https://doi.org/21.T11148/a369e128df5ef31044d4")
  @JsonAlias("ods:MachineAnnotationService")
  MAS("https://doi.org/21.T11148/a369e128df5ef31044d4", "ods:MachineAnnotationService"),

  @JsonProperty("https://doi.org/21.T11148/532ce6796e2828dd2be6")
  @JsonAlias("MachineAnnotationServiceJobRecord")
  MJR("https://doi.org/21.T11148/532ce6796e2828dd2be6", "MachineAnnotationServiceJobRecord");

  private final String pid;
  private final String name;


  FdoType(String pid, String name){
    this.pid = pid;
    this.name = name;
  }
}
