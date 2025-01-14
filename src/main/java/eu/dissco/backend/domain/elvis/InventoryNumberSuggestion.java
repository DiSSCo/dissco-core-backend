package eu.dissco.backend.domain.elvis;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema()
public record InventoryNumberSuggestion(String catalogNumber, String inventoryNumber, String identifier) {

}
