package eu.dissco.backend.domain.openapi.specimen;

import com.fasterxml.jackson.databind.JsonNode;
import eu.dissco.backend.domain.jsonapi.JsonApiLinks;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema
public record AggregationResponse(
    AggregationResponseData data,
    @Schema(description = "Links object, self-referencing") JsonApiLinks links
) {

  @Schema
  private record AggregationResponseData(
      @Parameter(description = "ID of the response") String id,
      @Parameter(description = "Type of response, in this case \"aggregations\"") String type,
      @Parameter(description = "Aggregation terms", example = """
          "country": {
                          "Netherlands": 314879,
                          "Indonesia": 207148,
                          "Estonia": 164357,
                          "Greece": 111511,
                          "Germany": 45251,
                          "Denmark": 26121,
                          "El Salvador": 23224,
                          "Russia": 21921,
                          "Tanzania": 18061,
                          "France": 15391
                      }
          """) JsonNode attributes
  ){
  }

}
