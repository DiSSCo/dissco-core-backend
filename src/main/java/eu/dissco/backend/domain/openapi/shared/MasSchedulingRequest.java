package eu.dissco.backend.domain.openapi.shared;

import eu.dissco.backend.domain.MasJobRequest;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema
public record MasSchedulingRequest(
    MasSchedulingData data
) {

  @Schema
  public record MasSchedulingData(
      @Parameter(description = "Type of request, in this case \"MasRequest\"")
      String type,
      MasSchedulingAttributes attributes
  ) {

    @Schema
    public record MasSchedulingAttributes(
        List<MasJobRequest> mass
    ) {

    }

  }

}
