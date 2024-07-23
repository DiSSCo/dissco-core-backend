package eu.dissco.backend.domain;

import lombok.Getter;

@Getter
public enum OdsType {
  DIGITAL_SPECIMEN("https://hdl.handle.net/21.T11148/894b1e6cad57e921764e"),
  DIGITAL_MEDIA("https://hdl.handle.net/21.T11148/bbad8c4e101e8af01115"),
  ANNOTATION("https://hdl.handle.net/21.T11148/cf458ca9ee1d44a5608f"),
  SOURCE_SYSTEM("https://hdl.handle.net/21.T11148/417a4f472f60f7974c12"),
  DATA_MAPPING("https://hdl.handle.net/21.T11148/ce794a6f4df42eb7e77e"),
  MAS("https://hdl.handle.net/21.T11148/22e71a0015cbcfba8ffa");

  private final String pid;

  OdsType(String pid){
    this.pid = pid;
  }
}
