package eu.dissco.backend.domain.openapi.specimen;

import eu.dissco.backend.domain.jsonapi.JsonApiLinksFull;
import eu.dissco.backend.domain.jsonapi.JsonApiMeta;
import eu.dissco.backend.schema.Annotation;
import eu.dissco.backend.schema.DigitalMedia;
import eu.dissco.backend.schema.DigitalSpecimen;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record DigitalSpecimenResponseFull(
    DigitalSpecimenResponseDataFull data,
    @Schema(description = "Links object, for pagination") JsonApiLinksFull links,
    @Schema(description = "Response metadata") JsonApiMeta meta
) {

  private record DigitalSpecimenResponseDataFull(
      DigitalSpecimenResponseAttributesFull attributes
  ) {

    private record DigitalSpecimenResponseAttributesFull(
        DigitalSpecimen digitalSpecimen,
        List<DigitalMedia> digitalMedia,
        List<Annotation> annotations
    ) {

    }
  }


}
