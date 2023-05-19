package eu.dissco.backend.repository;

import static eu.dissco.backend.database.jooq.Tables.ORGANISATION_DO;
import static eu.dissco.backend.utils.OrganisationUtils.COUNTRY;
import static eu.dissco.backend.utils.OrganisationUtils.ORGANISATION;
import static org.assertj.core.api.Assertions.assertThat;

import eu.dissco.backend.domain.Country;
import eu.dissco.backend.domain.OrganisationTuple;
import java.util.ArrayList;
import java.util.List;
import org.jooq.Query;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrganisationRepositoryIT extends BaseRepositoryIT {

  private OrganisationRepository repository;

  @BeforeEach
  void setup() {
    repository = new OrganisationRepository(context);
  }

  @AfterEach
  void destroy() {
    context.truncate(ORGANISATION_DO).execute();
  }

  @Test
  void testGetOrganisationNames(){
    // Given
    var museum = new OrganisationTuple("Museum für Naturkunde", "2");
    List<OrganisationRecord> organisationRecords = List.of(
        new OrganisationRecord(ORGANISATION.ror(),ORGANISATION.name(), COUNTRY.country(), COUNTRY.countryCode()),
        new OrganisationRecord(museum.ror(), museum.name(), "germany", "DE")
    );
    postToDb(organisationRecords);
    var expected = List.of(ORGANISATION.name(), museum.name());

    // When
    var result = repository.getOrganisationNames(1,2);

    // Then
    assertThat(result).hasSameElementsAs(expected);
  }

  @Test
  void testGetOrganisations(){

    // Given
    var museum = new OrganisationTuple("Museum für Naturkunde", "2");
    var expected = List.of(ORGANISATION, museum);

    List<OrganisationRecord> organisationRecords = List.of(
        new OrganisationRecord(ORGANISATION.ror(),ORGANISATION.name(), COUNTRY.country(), COUNTRY.countryCode()),
        new OrganisationRecord(museum.ror(), museum.name(), "germany", "DE")
    );
    postToDb(organisationRecords);

    // When
    var result = repository.getOrganisations(1,2 );

    // Then
    assertThat(result).hasSameElementsAs(expected);
  }

  @Test
  void testGetCountries(){

    // Given
    Country germany = new Country("Germany", "DE");
    var expected = List.of(COUNTRY, germany);

    List<OrganisationRecord> organisationRecords = List.of(
        new OrganisationRecord(ORGANISATION.ror(),ORGANISATION.name(), COUNTRY.country(), COUNTRY.countryCode()),
        new OrganisationRecord("2", "Museum für Naturkunde", germany.country(), germany.countryCode())
    );
    postToDb(organisationRecords);

    // When
    var result = repository.getCountries(1,2);

    // Then
    assertThat(result).hasSameElementsAs(expected);
  }

  private void postToDb(List<OrganisationRecord> organisations) {
    List<Query> queryList = new ArrayList<>();
    for (var org : organisations) {
      var query = context.insertInto(ORGANISATION_DO)
          .set(ORGANISATION_DO.ID, org.id())
          .set(ORGANISATION_DO.ORGANISATION_NAME, org.orgName())
          .set(ORGANISATION_DO.COUNTRY, org.countryName())
          .set(ORGANISATION_DO.COUNTRY_CODE, org.countryCode());
      queryList.add(query);
    }
    context.batch(queryList).execute();
  }

  private record OrganisationRecord(String id, String orgName, String countryName, String countryCode) {}
}
