package eu.dissco.backend.domain.elvis;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Suggestions for key identifiers")
public record InventoryNumberSuggestion(
    @Schema(description = "ods:physicalSpecimenId")
    String catalogNumber,
    @Schema(description = "dcterms:identifier")
    String inventoryNumber) {

}
