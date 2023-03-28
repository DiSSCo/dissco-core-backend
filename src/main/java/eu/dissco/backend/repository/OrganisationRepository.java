package eu.dissco.backend.repository;

import static eu.dissco.backend.database.jooq.Tables.ORGANISATION_DO;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.domain.Country;
import eu.dissco.backend.domain.OrganisationTuple;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record1;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class OrganisationRepository {

  private final DSLContext context;
  private final ObjectMapper mapper;

  public List<String> getOrganisationNames() {
    return context.select(ORGANISATION_DO.ORGANISATION_NAME).from(ORGANISATION_DO)
        .fetch(Record1::value1);
  }

  public List<OrganisationTuple> getOrganisations() {
    return context.select(ORGANISATION_DO.ID, ORGANISATION_DO.ORGANISATION_NAME)
        .from(ORGANISATION_DO).fetch(this::mapToOrganisation);
  }

  private OrganisationTuple mapToOrganisation(Record dbRecord) {
    return new OrganisationTuple(dbRecord.get(ORGANISATION_DO.ORGANISATION_NAME), dbRecord.get(ORGANISATION_DO.ID));
  }

  public List<Country> getCountries() {
    return context.selectDistinct(ORGANISATION_DO.COUNTRY, ORGANISATION_DO.COUNTRY_CODE).from(ORGANISATION_DO).groupBy(ORGANISATION_DO.COUNTRY,
        ORGANISATION_DO.COUNTRY_CODE).fetch(this::mapCountry);
  }


  private Country mapCountry(Record dbRecord) {
    return new Country(dbRecord.get(ORGANISATION_DO.COUNTRY), dbRecord.get(ORGANISATION_DO.COUNTRY_CODE));
  }
}
