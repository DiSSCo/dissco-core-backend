package eu.dissco.backend.repository.ElasticSearchTestRecords;

import eu.dissco.backend.domain.DigitalSpecimen;
import java.util.List;

public record DigitalSpecimenTestEvent(
    List<String> enrichmentList,
    DigitalSpecimen digitalSpecimen
) {

}
