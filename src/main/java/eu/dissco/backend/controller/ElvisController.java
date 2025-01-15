package eu.dissco.backend.controller;

import static eu.dissco.backend.controller.BaseController.DEFAULT_PAGE_NUM;
import static eu.dissco.backend.controller.BaseController.DEFAULT_PAGE_SIZE;
import static eu.dissco.backend.controller.BaseController.PAGE_NUM_OAS;
import static eu.dissco.backend.controller.BaseController.PAGE_SIZE_OAS;
import static eu.dissco.backend.controller.BaseController.PREFIX_OAS;
import static eu.dissco.backend.controller.BaseController.SUFFIX_OAS;

import com.fasterxml.jackson.databind.JsonNode;
import eu.dissco.backend.domain.elvis.ElvisSpecimen;
import eu.dissco.backend.service.ElvisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@RestController
@RequestMapping("/elvis")
@RequiredArgsConstructor
public class ElvisController {

  private final ElvisService elvisService;


  @Operation(summary = "Searches DiSSCo specimens by inventory number (also known as physical specimen ID)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Specimen successfully retrieved", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ElvisSpecimen.class))
      })
  })
  @GetMapping(value = "/inventory", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonNode> getSpecimenByInventoryNumber(
      @Parameter(description = "Inventory number (physical specimen id}") @RequestParam("inventoryNumber") String inventoryNumber,
      @Parameter(description = PAGE_NUM_OAS) @RequestParam(defaultValue = DEFAULT_PAGE_NUM) int pageNumber,
      @Parameter(description = PAGE_SIZE_OAS) @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int pageSize
  ) throws IOException {
    return ResponseEntity.ok()
        .body(elvisService.searchBySpecimenId(inventoryNumber, pageNumber, pageSize));
  }

  @Operation(summary = "Get specimen from DiSSCo DOI")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Specimen successfully retrieved", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ElvisSpecimen.class))
      })
  })
  @GetMapping(value = "/doi/{prefix}/{suffix}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ElvisSpecimen> getSpecimenByDoi(
      @Parameter(description = PREFIX_OAS) @PathVariable("prefix") String prefix,
      @Parameter(description = SUFFIX_OAS) @PathVariable("suffix") String suffix) {
    var id = prefix + "/" + suffix;
    return ResponseEntity.ok().body(elvisService.searchByDoi(id));
  }

  @Operation(summary = "Searches DiSSCo specimens by inventory number (also known as physical specimen ID)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Specimen successfully retrieved", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ElvisSpecimen.class))
      })
  })
  @GetMapping(value = "/suggest", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonNode> suggestInventoryNumber(
      @Parameter(description = "Inventory number (physical specimen id}") @RequestParam("inventoryNumber") String inventoryNumber,
      @Parameter(description = PAGE_NUM_OAS) @RequestParam(defaultValue = DEFAULT_PAGE_NUM) int pageNumber,
      @Parameter(description = PAGE_SIZE_OAS) @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int pageSize
  ) throws IOException {
    return ResponseEntity.ok()
        .body(elvisService.suggestInventoryNumber(inventoryNumber, pageNumber, pageSize));
  }

}
