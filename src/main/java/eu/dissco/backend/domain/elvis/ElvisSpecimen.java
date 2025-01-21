package eu.dissco.backend.domain.elvis;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = """
    Schema for ELViS specimen.
    If no value for a given term is present, an empty string is returned.
    Where possible, taxonomic terms come from the accepted identification. If there is no accepted identification, the first identification is used.
    Some identifications may have multiple taxonomic identifications. This occurs when a specimen contains multiple subjects (e.g. a rock with multiple fossils). 
    In this case, the first two taxonomies are presented, and the string "and x more" is appended. 
    """)
public record ElvisSpecimen(
    @Schema(description = "ods:physicalSpecimenId") String inventoryNumber,
    @Schema(description = "Concatenation of ods:specimenName physicalSpecimenID, ods:OrganisationName") String title,
    @Schema(description = "dwc:collectionCode") String collectionCode,
    @Schema(description = "dwc:physicalSpecimenID") String catalogNumber,
    @Schema(description = "ods:organisationCode") String institutionCode,
    @Schema(description = "dwc:basisOfRecord") String basisOfRecord,
    @JsonProperty(value = "URI") @Schema(description = "DOI that resolves to human-readable specimen landing page on DiSSCo") String uri,
    @Schema(description = "dwc:scientificName") String scientificName,
    @Schema(description = "dwc:scientificNameAuthorship") String scientificNameAuthorship,
    @Schema(description = "dwc:specificEptitet") String specificEpithet,
    @Schema(description = "dwc:family") String family,
    @Schema(description = "dwc:genus") String genus,
    @Schema(description = "dwc:vernacularName") String vernacularName) {

}
