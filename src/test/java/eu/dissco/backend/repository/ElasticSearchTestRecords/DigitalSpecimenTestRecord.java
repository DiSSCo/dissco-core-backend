package eu.dissco.backend.repository.ElasticSearchTestRecords;

import eu.dissco.backend.domain.DigitalSpecimen;
import java.time.Instant;

public record DigitalSpecimenTestRecord(
    String id,
    int midsLevel,
    int version,
    Instant created,
    DigitalSpecimen digitalSpecimen
) {

}
