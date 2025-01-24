package eu.dissco.backend.controller;

import static eu.dissco.backend.controller.BaseController.DEFAULT_PAGE_NUM;
import static eu.dissco.backend.controller.BaseController.PAGE_NUM_OAS;
import static eu.dissco.backend.controller.BaseController.PAGE_SIZE_OAS;
import static eu.dissco.backend.controller.BaseController.PREFIX_OAS;
import static eu.dissco.backend.controller.BaseController.SUFFIX_OAS;

import eu.dissco.backend.domain.elvis.ElvisSpecimen;
import eu.dissco.backend.domain.elvis.InventoryNumberSuggestionResponse;
import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.service.ElvisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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

  @Operation(summary = "Get specimen from DiSSCo DOI", description = """
      Given a DOI, retrieves specimen information according to ELViS parameters.  
      """)
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Specimen successfully retrieved", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ElvisSpecimen.class))
      })
  })
  @GetMapping(value = "/specimen/{prefix}/{suffix}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ElvisSpecimen> getSpecimenByDoi(
      @Parameter(description = PREFIX_OAS) @PathVariable("prefix") String prefix,
      @Parameter(description = SUFFIX_OAS) @PathVariable("suffix") String suffix) throws NotFoundException  {
    var id = prefix + "/" + suffix;
    return ResponseEntity.ok().body(elvisService.searchByDoi(id));
  }

  @Operation(summary = "Searches DiSSCo specimens for relevant identifiers", description = """
      Searches DiSSCo specimens for relevant identifiers. The inputted searchValue is checked against the following ODS terms:
      - ods:physicalSpecimenId, the local collection number, which is mapped to catalogNumber in the response
      - dcterms:identifier, the DiSSCo DOI, which is mapped to inventoryNumber in the response
      
      This endpoint searches both above fields for desired inputted value. It accepts partial matches, implementing a wildcard search.
      Note: When searching, do not include the DOI proxy (i.e. "https://doi.org/") in the input
      """)
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Specimen successfully retrieved", content = {
          @Content(mediaType = "application/json",
              array = @ArraySchema(schema = @Schema(implementation = InventoryNumberSuggestionResponse.class)))
      })
  })
  @GetMapping(value = "/specimen/suggest/inventoryNumber", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<InventoryNumberSuggestionResponse> suggestInventoryNumber(
      @Parameter(description = "searchValue") @RequestParam("searchValue") String searchValue,
      @Parameter(description = PAGE_NUM_OAS) @RequestParam(defaultValue = DEFAULT_PAGE_NUM) int pageNumber,
      @Parameter(description = PAGE_SIZE_OAS) @RequestParam(defaultValue = "20") int pageSize
  ) throws IOException, NotFoundException {
    return ResponseEntity.ok()
        .body(elvisService.suggestInventoryNumber(searchValue, pageNumber, pageSize));
  }

}
