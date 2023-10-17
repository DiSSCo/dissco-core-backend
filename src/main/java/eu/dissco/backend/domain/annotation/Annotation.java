package eu.dissco.backend.domain.annotation;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Annotation {

  @JsonProperty("ods:id")
  private String odsId;
  @JsonProperty("rdf:type")
  private String rdfType = "Annotation";
  @JsonProperty("ods:version")
  private int odsVersion;
  @JsonProperty("oa:motivation")
  private Motivation oaMotivation;
  @JsonProperty("oa:motivatedBy")
  private String oaMotivatedBy;
  @JsonProperty("oa:target")
  private Target oaTarget;
  @JsonProperty("oa:body")
  private Body oaBody;
  @JsonProperty("oa:creator")
  private Creator oaCreator;
  @JsonProperty("dcterms:created")
  private Instant dcTermsCreated;
  @JsonProperty("ods:deletedOn")
  private Instant odsDeletedOn;
  @JsonProperty("as:generator")
  private Generator asGenerator;
  @JsonProperty("oa:generated")
  private Instant oaGenerated;
  @JsonProperty("ods:aggregateRating")
  private AggregateRating odsAggregateRating;

  public Annotation withOdsId(String odsId) {
    this.odsId = odsId;
    return this;
  }

  public Annotation withRdfType(String rdfType){
    this.rdfType = rdfType;
    return this;
  }

  public Annotation withOdsVersion(int odsVersion){
    this.odsVersion = odsVersion;
    return this;
  }

  public Annotation withOaMotivation(Motivation oaMotivation) {
    this.oaMotivation = oaMotivation;
    return this;
  }

  public Annotation withOaMotivatedBy(String oaMotivatedBy) {
    this.oaMotivatedBy = oaMotivatedBy;
    return this;
  }

  public Annotation withOaTarget(Target oaTarget) {
    this.oaTarget = oaTarget;
    return this;
  }

  public Annotation withOaBody(Body oaBody) {
    this.oaBody = oaBody;
    return this;
  }

  public Annotation withOaCreator(Creator oaCreator) {
    this.oaCreator = oaCreator;
    return this;
  }

  public Annotation withDcTermsCreated(Instant dcTermsCreated) {
    this.dcTermsCreated = dcTermsCreated;
    return this;
  }

  public Annotation withOdsDeletedOn(Instant odsDeletedOn) {
    this.odsDeletedOn = odsDeletedOn;
    return this;
  }

  public Annotation withAsGenerator(Generator asGenerator) {
    this.asGenerator = asGenerator;
    return this;
  }

  public Annotation withOaGenerated(Instant oaGenerated) {
    this.oaGenerated = oaGenerated;
    return this;
  }

  public Annotation withOdsAggregateRating(AggregateRating odsAggregateRating) {
    this.odsAggregateRating = odsAggregateRating;
    return this;
  }


}
