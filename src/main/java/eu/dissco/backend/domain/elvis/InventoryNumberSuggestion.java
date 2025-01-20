package eu.dissco.backend.domain.elvis;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema
public record InventoryNumberSuggestion(
    @Schema(description = "dwc:collectionID")
    String catalogNumber,
    @Schema(description = "ods:physicalSpecimenId")
    String inventoryNumber,
    @Schema(description = "dcterms:identifier")
    String identifier) {

}
