package eu.dissco.backend.repository;

import static eu.dissco.backend.database.jooq.Tables.ORGANISATION_DO;
import static eu.dissco.backend.database.jooq.Tables.ORGANISATION_DOCUMENT;
import static eu.dissco.backend.util.TestUtils.ORGANISATION_NAME;
import static eu.dissco.backend.util.TestUtils.ORGANISATION_ROR;
import static eu.dissco.backend.util.TestUtils.givenOrganisationDocument;
import static org.assertj.core.api.Assertions.assertThat;

import eu.dissco.backend.domain.OrganisationTuple;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrganisationRepositoryIT extends BaseRepositoryIT {

  private OrganisationRepository repository;

  @BeforeEach
  void prepareTests() {
    this.repository = new OrganisationRepository(context);
  }

  @AfterEach
  void cleanupTests() {
    context.truncate(ORGANISATION_DO).cascade().execute();
  }

  @Test
  void testGetOrganisationNames() {
    // Given
    fillDatabase();

    // When
    var result = repository.getOrganisationNames();

    // Then
    assertThat(result).isEqualTo(List.of(ORGANISATION_NAME));
  }

  @Test
  void testGetOrganisationTuple() {
    // Given
    fillDatabase();

    // When
    var result = repository.getOrganisationTuple();

    // Then
    assertThat(result).isEqualTo(
        List.of(new OrganisationTuple(ORGANISATION_NAME, ORGANISATION_ROR)));
  }

  @Test
  void testSaveNewDocument() {
    // Given
    fillDatabase();

    // When
    repository.saveNewDocument(givenOrganisationDocument());

    // Then
    var result = context.select(ORGANISATION_DOCUMENT.asterisk())
        .from(ORGANISATION_DOCUMENT)
        .fetch().stream().toList();
    assertThat(result).hasSize(1);
  }

  private void fillDatabase() {
    context.insertInto(ORGANISATION_DO)
        .set(ORGANISATION_DO.ID, ORGANISATION_ROR)
        .set(ORGANISATION_DO.ORGANISATION_NAME, ORGANISATION_NAME)
        .execute();
  }


}
