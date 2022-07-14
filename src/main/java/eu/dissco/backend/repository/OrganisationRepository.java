package eu.dissco.backend.repository;

import static eu.dissco.backend.database.jooq.Tables.ORGANISATION_DO;

import eu.dissco.backend.domain.OrganisationTuple;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Record2;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class OrganisationRepository {

  private final DSLContext context;

  public List<String> getOrganisationNames() {
    return context.select(ORGANISATION_DO.ORGANISATION_NAME).from(ORGANISATION_DO)
        .fetch(Record1::value1);
  }

  public List<OrganisationTuple> getOrganisationTuple() {
    return context.select(ORGANISATION_DO.ID, ORGANISATION_DO.ORGANISATION_NAME)
        .from(ORGANISATION_DO).fetch(this::mapToOrganisationTuple);
  }

  private OrganisationTuple mapToOrganisationTuple(Record2<String, String> dbRecord) {
    return new OrganisationTuple(
        dbRecord.get(ORGANISATION_DO.ORGANISATION_NAME),
        dbRecord.get(ORGANISATION_DO.ID));
  }

}
