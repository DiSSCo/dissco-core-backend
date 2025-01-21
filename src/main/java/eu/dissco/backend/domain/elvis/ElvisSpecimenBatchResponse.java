package eu.dissco.backend.domain.elvis;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema
public record ElvisSpecimenBatchResponse(
    @Schema(example = "3")
    Long total,
    List<ElvisSpecimen> specimens
) {

}
