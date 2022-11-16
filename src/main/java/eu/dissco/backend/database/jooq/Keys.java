/*
 * This file is generated by jOOQ.
 */
package eu.dissco.backend.database.jooq;


import eu.dissco.backend.database.jooq.tables.NewAnnotation;
import eu.dissco.backend.database.jooq.tables.NewDigitalMediaObject;
import eu.dissco.backend.database.jooq.tables.NewDigitalSpecimen;
import eu.dissco.backend.database.jooq.tables.NewMapping;
import eu.dissco.backend.database.jooq.tables.NewSourceSystem;
import eu.dissco.backend.database.jooq.tables.NewUser;
import eu.dissco.backend.database.jooq.tables.OrganisationDo;
import eu.dissco.backend.database.jooq.tables.OrganisationDocument;
import eu.dissco.backend.database.jooq.tables.records.NewAnnotationRecord;
import eu.dissco.backend.database.jooq.tables.records.NewDigitalMediaObjectRecord;
import eu.dissco.backend.database.jooq.tables.records.NewDigitalSpecimenRecord;
import eu.dissco.backend.database.jooq.tables.records.NewMappingRecord;
import eu.dissco.backend.database.jooq.tables.records.NewSourceSystemRecord;
import eu.dissco.backend.database.jooq.tables.records.NewUserRecord;
import eu.dissco.backend.database.jooq.tables.records.OrganisationDoRecord;
import eu.dissco.backend.database.jooq.tables.records.OrganisationDocumentRecord;

import org.jooq.ForeignKey;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;


/**
 * A class modelling foreign key relationships and constraints of tables in 
 * public.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<NewAnnotationRecord> NEW_ANNOTATION_PKEY = Internal.createUniqueKey(NewAnnotation.NEW_ANNOTATION, DSL.name("new_annotation_pkey"), new TableField[] { NewAnnotation.NEW_ANNOTATION.ID, NewAnnotation.NEW_ANNOTATION.VERSION }, true);
    public static final UniqueKey<NewDigitalMediaObjectRecord> NEW_DIGITAL_MEDIA_OBJECT_PKEY = Internal.createUniqueKey(NewDigitalMediaObject.NEW_DIGITAL_MEDIA_OBJECT, DSL.name("new_digital_media_object_pkey"), new TableField[] { NewDigitalMediaObject.NEW_DIGITAL_MEDIA_OBJECT.ID, NewDigitalMediaObject.NEW_DIGITAL_MEDIA_OBJECT.VERSION }, true);
    public static final UniqueKey<NewDigitalSpecimenRecord> NEW_DIGITAL_SPECIMEN_PKEY = Internal.createUniqueKey(NewDigitalSpecimen.NEW_DIGITAL_SPECIMEN, DSL.name("new_digital_specimen_pkey"), new TableField[] { NewDigitalSpecimen.NEW_DIGITAL_SPECIMEN.ID, NewDigitalSpecimen.NEW_DIGITAL_SPECIMEN.VERSION }, true);
    public static final UniqueKey<NewMappingRecord> NEW_MAPPING_PK = Internal.createUniqueKey(NewMapping.NEW_MAPPING, DSL.name("new_mapping_pk"), new TableField[] { NewMapping.NEW_MAPPING.ID, NewMapping.NEW_MAPPING.VERSION }, true);
    public static final UniqueKey<NewSourceSystemRecord> NEW_SOURCE_SYSTEM_ENDPOINT_KEY = Internal.createUniqueKey(NewSourceSystem.NEW_SOURCE_SYSTEM, DSL.name("new_source_system_endpoint_key"), new TableField[] { NewSourceSystem.NEW_SOURCE_SYSTEM.ENDPOINT }, true);
    public static final UniqueKey<NewSourceSystemRecord> NEW_SOURCE_SYSTEM_PKEY = Internal.createUniqueKey(NewSourceSystem.NEW_SOURCE_SYSTEM, DSL.name("new_source_system_pkey"), new TableField[] { NewSourceSystem.NEW_SOURCE_SYSTEM.ID }, true);
    public static final UniqueKey<NewUserRecord> NEW_USER_ORCID_KEY = Internal.createUniqueKey(NewUser.NEW_USER, DSL.name("new_user_orcid_key"), new TableField[] { NewUser.NEW_USER.ORCID }, true);
    public static final UniqueKey<NewUserRecord> NEW_USER_PKEY = Internal.createUniqueKey(NewUser.NEW_USER, DSL.name("new_user_pkey"), new TableField[] { NewUser.NEW_USER.ID }, true);
    public static final UniqueKey<OrganisationDoRecord> ORGANISATION_DO_PKEY = Internal.createUniqueKey(OrganisationDo.ORGANISATION_DO, DSL.name("organisation_do_pkey"), new TableField[] { OrganisationDo.ORGANISATION_DO.ID }, true);
    public static final UniqueKey<OrganisationDocumentRecord> ORGANISATION_DOCUMENT_PKEY = Internal.createUniqueKey(OrganisationDocument.ORGANISATION_DOCUMENT, DSL.name("organisation_document_pkey"), new TableField[] { OrganisationDocument.ORGANISATION_DOCUMENT.DOCUMENT_ID }, true);

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------

    public static final ForeignKey<OrganisationDocumentRecord, OrganisationDoRecord> ORGANISATION_DOCUMENT__FK_ORGANISATION = Internal.createForeignKey(OrganisationDocument.ORGANISATION_DOCUMENT, DSL.name("fk_organisation"), new TableField[] { OrganisationDocument.ORGANISATION_DOCUMENT.ORGANISATION_ID }, Keys.ORGANISATION_DO_PKEY, new TableField[] { OrganisationDo.ORGANISATION_DO.ID }, true);
}
