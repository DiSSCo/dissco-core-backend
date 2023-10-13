package eu.dissco.backend.domain.annotation;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Motivation {

  @JsonProperty("ods:adding") ADDING,
  @JsonProperty("oa:assessing") ASSESSING,
  @JsonProperty("oa:editing") EDITING,
  @JsonProperty("oa:commenting") COMMENTING;

}
