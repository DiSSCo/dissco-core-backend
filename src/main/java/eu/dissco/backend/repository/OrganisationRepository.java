package eu.dissco.backend.repository;

import static eu.dissco.backend.database.jooq.Tables.ORGANISATION_DO;
import static eu.dissco.backend.database.jooq.Tables.ORGANISATION_DOCUMENT;

import eu.dissco.backend.domain.OrganisationDocument;
import eu.dissco.backend.domain.OrganisationTuple;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.JSONB;
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

  public void saveNewDocument(OrganisationDocument document) {
    context.insertInto(ORGANISATION_DOCUMENT)
        .set(ORGANISATION_DOCUMENT.DOCUMENT_ID, document.getDocumentId())
        .set(ORGANISATION_DOCUMENT.ORGANISATION_ID, document.getOrganisationId())
        .set(ORGANISATION_DOCUMENT.DOCUMENT_TITLE, document.getDocumentTitle())
        .set(ORGANISATION_DOCUMENT.DOCUMENT_TYPE, document.getDocumentType())
        .set(ORGANISATION_DOCUMENT.DOCUMENT, JSONB.jsonb(document.getDocument().toString()))
        .onConflict(ORGANISATION_DOCUMENT.DOCUMENT_ID)
        .doUpdate()
        .set(ORGANISATION_DOCUMENT.ORGANISATION_ID, document.getOrganisationId())
        .set(ORGANISATION_DOCUMENT.DOCUMENT_TITLE, document.getDocumentTitle())
        .set(ORGANISATION_DOCUMENT.DOCUMENT_TYPE, document.getDocumentType())
        .set(ORGANISATION_DOCUMENT.DOCUMENT, JSONB.jsonb(document.getDocument().toString()))
        .execute();
  }
}
