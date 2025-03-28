package eu.dissco.backend.domain.elvis;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = """
    Schema for ELViS specimen.
    
    If no value for a given term is present, an empty string is returned.
    
    Where possible, taxonomic terms come from the "accepted Identification". If there is no accepted Identification, the first Identification is used.
    Some Identifications contain have multiple taxonomies. This occurs when a specimen contains multiple subjects (e.g. a rock with multiple fossils).
    In this case, the response includes the first two taxonomies then "and x more" is appended.
    """)
public record ElvisSpecimen(
    @Schema(description = "dcterms:identifier, the DOI of the specimen") String inventoryNumber,
    @Schema(description = "Concatenation of ods:specimenName physicalSpecimenID, ods:OrganisationName") String title,
    @Schema(description = "dwc:collectionCode") String collectionCode,
    @Schema(description = "dwc:physicalSpecimenID") String catalogNumber,
    @Schema(description = "ods:organisationCode") String institutionCode,
    @Schema(description = "dwc:basisOfRecord") String basisOfRecord,
    @JsonProperty(value = "URI") @Schema(description = "DOI that resolves to human-readable specimen landing page on DiSSCo") String uri,
    @Schema(description = "dwc:scientificName") String scientificName,
    @Schema(description = "dwc:scientificNameAuthorship") String scientificNameAuthorship,
    @Schema(description = "dwc:specificEpithet") String specificEpithet,
    @Schema(description = "dwc:family") String family,
    @Schema(description = "dwc:genus") String genus,
    @Schema(description = "dwc:vernacularName") String vernacularName) {
}
