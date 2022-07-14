package eu.dissco.backend.repository;

import static eu.dissco.backend.database.jooq.Tables.ORGANISATION_DOCUMENT;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.domain.OrganisationDocument;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class OrganisationDocumentRepository {

  private final ObjectMapper mapper;
  private final DSLContext context;

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

  public OrganisationDocument getDocument(String id) {
    return context.select(ORGANISATION_DOCUMENT.asterisk()).from(ORGANISATION_DOCUMENT)
        .where(ORGANISATION_DOCUMENT.DOCUMENT_ID.eq(id)).fetchOne(this::mapToDocument);
  }

  private OrganisationDocument mapToDocument(Record dbRecord) {
    try {
      var document = new OrganisationDocument();
      document.setDocumentId(dbRecord.get(ORGANISATION_DOCUMENT.DOCUMENT_ID));
      document.setOrganisationId(dbRecord.get(ORGANISATION_DOCUMENT.ORGANISATION_ID));
      document.setDocumentTitle(dbRecord.get(ORGANISATION_DOCUMENT.DOCUMENT_TITLE));
      document.setDocumentType(dbRecord.get(ORGANISATION_DOCUMENT.DOCUMENT_TYPE));
      document.setDocument(mapper.readTree(dbRecord.get(ORGANISATION_DOCUMENT.DOCUMENT).data()));
      return document;
    } catch (JsonProcessingException e) {
      log.info("Failed to parse document: {}", dbRecord.get(ORGANISATION_DOCUMENT.DOCUMENT_ID), e);
      return null;
    }
  }

  public List<OrganisationDocument> getDocumentsForOrganisation(String ror) {
    return context.select(ORGANISATION_DOCUMENT.asterisk()).from(ORGANISATION_DOCUMENT)
        .where(ORGANISATION_DOCUMENT.ORGANISATION_ID.eq(ror)).fetch(this::mapToDocument);
  }
}
