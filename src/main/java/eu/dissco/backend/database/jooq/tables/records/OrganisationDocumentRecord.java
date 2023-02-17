/*
 * This file is generated by jOOQ.
 */
package eu.dissco.backend.database.jooq.tables.records;


import eu.dissco.backend.database.jooq.tables.OrganisationDocument;
import org.jooq.Field;
import org.jooq.JSONB;
import org.jooq.Record1;
import org.jooq.Record5;
import org.jooq.Row5;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class OrganisationDocumentRecord extends UpdatableRecordImpl<OrganisationDocumentRecord> implements Record5<String, String, String, String, JSONB> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.organisation_document.organisation_id</code>.
     */
    public void setOrganisationId(String value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.organisation_document.organisation_id</code>.
     */
    public String getOrganisationId() {
        return (String) get(0);
    }

    /**
     * Setter for <code>public.organisation_document.document_id</code>.
     */
    public void setDocumentId(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.organisation_document.document_id</code>.
     */
    public String getDocumentId() {
        return (String) get(1);
    }

    /**
     * Setter for <code>public.organisation_document.document_title</code>.
     */
    public void setDocumentTitle(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.organisation_document.document_title</code>.
     */
    public String getDocumentTitle() {
        return (String) get(2);
    }

    /**
     * Setter for <code>public.organisation_document.document_type</code>.
     */
    public void setDocumentType(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.organisation_document.document_type</code>.
     */
    public String getDocumentType() {
        return (String) get(3);
    }

    /**
     * Setter for <code>public.organisation_document.document</code>.
     */
    public void setDocument(JSONB value) {
        set(4, value);
    }

    /**
     * Getter for <code>public.organisation_document.document</code>.
     */
    public JSONB getDocument() {
        return (JSONB) get(4);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<String> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record5 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row5<String, String, String, String, JSONB> fieldsRow() {
        return (Row5) super.fieldsRow();
    }

    @Override
    public Row5<String, String, String, String, JSONB> valuesRow() {
        return (Row5) super.valuesRow();
    }

    @Override
    public Field<String> field1() {
        return OrganisationDocument.ORGANISATION_DOCUMENT.ORGANISATION_ID;
    }

    @Override
    public Field<String> field2() {
        return OrganisationDocument.ORGANISATION_DOCUMENT.DOCUMENT_ID;
    }

    @Override
    public Field<String> field3() {
        return OrganisationDocument.ORGANISATION_DOCUMENT.DOCUMENT_TITLE;
    }

    @Override
    public Field<String> field4() {
        return OrganisationDocument.ORGANISATION_DOCUMENT.DOCUMENT_TYPE;
    }

    @Override
    public Field<JSONB> field5() {
        return OrganisationDocument.ORGANISATION_DOCUMENT.DOCUMENT;
    }

    @Override
    public String component1() {
        return getOrganisationId();
    }

    @Override
    public String component2() {
        return getDocumentId();
    }

    @Override
    public String component3() {
        return getDocumentTitle();
    }

    @Override
    public String component4() {
        return getDocumentType();
    }

    @Override
    public JSONB component5() {
        return getDocument();
    }

    @Override
    public String value1() {
        return getOrganisationId();
    }

    @Override
    public String value2() {
        return getDocumentId();
    }

    @Override
    public String value3() {
        return getDocumentTitle();
    }

    @Override
    public String value4() {
        return getDocumentType();
    }

    @Override
    public JSONB value5() {
        return getDocument();
    }

    @Override
    public OrganisationDocumentRecord value1(String value) {
        setOrganisationId(value);
        return this;
    }

    @Override
    public OrganisationDocumentRecord value2(String value) {
        setDocumentId(value);
        return this;
    }

    @Override
    public OrganisationDocumentRecord value3(String value) {
        setDocumentTitle(value);
        return this;
    }

    @Override
    public OrganisationDocumentRecord value4(String value) {
        setDocumentType(value);
        return this;
    }

    @Override
    public OrganisationDocumentRecord value5(JSONB value) {
        setDocument(value);
        return this;
    }

    @Override
    public OrganisationDocumentRecord values(String value1, String value2, String value3, String value4, JSONB value5) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached OrganisationDocumentRecord
     */
    public OrganisationDocumentRecord() {
        super(OrganisationDocument.ORGANISATION_DOCUMENT);
    }

    /**
     * Create a detached, initialised OrganisationDocumentRecord
     */
    public OrganisationDocumentRecord(String organisationId, String documentId, String documentTitle, String documentType, JSONB document) {
        super(OrganisationDocument.ORGANISATION_DOCUMENT);

        setOrganisationId(organisationId);
        setDocumentId(documentId);
        setDocumentTitle(documentTitle);
        setDocumentType(documentType);
        setDocument(document);
    }
}
