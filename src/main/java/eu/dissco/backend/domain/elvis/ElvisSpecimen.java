package eu.dissco.backend.domain.elvis;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Schema for ELViS specimen. Note: where possible, taxonomic terms come from the accepted identification. If there is no accepted identification, the first identification is used.")
public record ElvisSpecimen(
    @Schema(description = "ods:physicalSpecimenId") String inventoryNumber,
    @Schema(description = "ods:specimenName") String title,
    @Schema(description = "dcterms:identifier") String identifier,
    @Schema(description = "dwc:collectionCode") String collectionCode,
    @Schema(description = "dwc:collectionID") String catalogNumber,
    @Schema(description = "ods:organisationID") String institutionId,
    @Schema(description = "ods:organisationCode") String institutionCode,
    @Schema(description = "dwc:basisOfRecord") String basisOfRecord,
    @JsonProperty(value = "URI") @Schema(description = "Resolves to human-readable specimen landing page on DiSSCo") String uri,
    @Schema(description = "dwc:scientificName") String scientificName,
    @Schema(description = "dwc:scientificNameAuthorship") String scientificNameAuthorship,
    @Schema(description = "dwc:specificEptitet") String specificEpithet,
    @Schema(description = "dwc:family") String family,
    @Schema(description = "dwc:genus") String genus,
    @Schema(description = "dwc:vernacularName") String vernacularName) {

}
