package eu.dissco.backend.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class OrganisationDocument {

  @JsonProperty("organisation_id")
  private String organisationId;
  @JsonProperty("document_id")
  private String documentId;
  @JsonProperty("document_title")
  private String documentTitle;
  @JsonProperty("document_type")
  private String documentType;
  @JsonProperty("document")
  private JsonNode document;

}
