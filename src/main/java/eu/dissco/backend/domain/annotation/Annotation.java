package eu.dissco.backend.domain.annotation;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Annotation {

  @JsonProperty("ods:id")
  private String odsId;
  @JsonProperty("rdf:type")
  private static final String RDF_TYPE = "Annotation";
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
  private Date dcTermsCreated;
  @JsonProperty("ods:deletedOn")
  private Date odsDeletedOn;
  @JsonProperty("as:generator")
  private Generator asGenerator;
  @JsonProperty("ods:aggregateRating")
  private AggregateRating odsAggregateRating;

  public Annotation withOdsId(String odsId) {
    this.odsId = odsId;
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

  public Annotation withDcTermsCreated(Date dcTermsCreated) {
    this.dcTermsCreated = dcTermsCreated;
    return this;
  }

  public Annotation withOdsDeletedOn(Date odsDeletedOn) {
    this.odsDeletedOn = odsDeletedOn;
    return this;
  }

  public Annotation withAsGenerator(Generator asGenerator) {
    this.asGenerator = asGenerator;
    return this;
  }

  public Annotation withOdsAggregateRating(AggregateRating odsAggregateRating) {
    this.odsAggregateRating = odsAggregateRating;
    return this;
  }


}
