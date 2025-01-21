package eu.dissco.backend.domain.elvis;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema
public record InventoryNumberSuggestion(
    @Schema(description = "ods:physicalSpecimenId")
    String catalogNumber,
    @Schema(description = "dcterms:identifier")
    String inventoryNumber) {

}
