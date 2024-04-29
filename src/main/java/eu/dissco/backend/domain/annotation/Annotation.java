package eu.dissco.backend.domain.annotation;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Builder
public class Annotation {

  @JsonProperty("ods:id")
  String odsId;
  @JsonProperty("rdf:type")
  @Builder.Default
  String rdfType = "Annotation";
  @JsonProperty("ods:version")
  Integer odsVersion;
  @JsonProperty("oa:motivation")
  Motivation oaMotivation;
  @JsonProperty("oa:motivatedBy")
  String oaMotivatedBy;
  @JsonProperty("oa:target")
  Target oaTarget;
  @JsonProperty("oa:body")
  Body oaBody;
  @JsonProperty("oa:creator")
  Creator oaCreator;
  @JsonProperty("dcterms:created")
  Instant dcTermsCreated;
  @JsonProperty("ods:deletedOn")
  Instant odsDeletedOn;
  @JsonProperty("as:generator")
  Generator asGenerator;
  @JsonProperty("oa:generated")
  Instant oaGenerated;
  @JsonProperty("schema.org:aggregateRating")
  AggregateRating odsAggregateRating;

}
