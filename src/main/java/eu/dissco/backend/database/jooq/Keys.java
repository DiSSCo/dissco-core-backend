/*
 * This file is generated by jOOQ.
 */
package eu.dissco.backend.database.jooq;


import eu.dissco.backend.database.jooq.tables.Annotation;
import eu.dissco.backend.database.jooq.tables.DigitalMediaObject;
import eu.dissco.backend.database.jooq.tables.DigitalSpecimen;
import eu.dissco.backend.database.jooq.tables.MachineAnnotationServices;
import eu.dissco.backend.database.jooq.tables.MasJobRecord;
import eu.dissco.backend.database.jooq.tables.NewUser;
import eu.dissco.backend.database.jooq.tables.records.AnnotationRecord;
import eu.dissco.backend.database.jooq.tables.records.DigitalMediaObjectRecord;
import eu.dissco.backend.database.jooq.tables.records.DigitalSpecimenRecord;
import eu.dissco.backend.database.jooq.tables.records.MachineAnnotationServicesRecord;
import eu.dissco.backend.database.jooq.tables.records.MasJobRecordRecord;
import eu.dissco.backend.database.jooq.tables.records.NewUserRecord;

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

    public static final UniqueKey<AnnotationRecord> ANNOTATION_PK = Internal.createUniqueKey(Annotation.ANNOTATION, DSL.name("annotation_pk"), new TableField[] { Annotation.ANNOTATION.ID }, true);
    public static final UniqueKey<DigitalMediaObjectRecord> DIGITAL_MEDIA_OBJECT_PK = Internal.createUniqueKey(DigitalMediaObject.DIGITAL_MEDIA_OBJECT, DSL.name("digital_media_object_pk"), new TableField[] { DigitalMediaObject.DIGITAL_MEDIA_OBJECT.ID }, true);
    public static final UniqueKey<DigitalSpecimenRecord> DIGITAL_SPECIMEN_PK = Internal.createUniqueKey(DigitalSpecimen.DIGITAL_SPECIMEN, DSL.name("digital_specimen_pk"), new TableField[] { DigitalSpecimen.DIGITAL_SPECIMEN.ID }, true);
    public static final UniqueKey<MachineAnnotationServicesRecord> MACHINE_ANNOTATION_SERVICES_PKEY = Internal.createUniqueKey(MachineAnnotationServices.MACHINE_ANNOTATION_SERVICES, DSL.name("machine_annotation_services_pkey"), new TableField[] { MachineAnnotationServices.MACHINE_ANNOTATION_SERVICES.ID }, true);
    public static final UniqueKey<MasJobRecordRecord> MAS_JOB_RECORD_PK = Internal.createUniqueKey(MasJobRecord.MAS_JOB_RECORD, DSL.name("mas_job_record_pk"), new TableField[] { MasJobRecord.MAS_JOB_RECORD.JOB_ID }, true);
    public static final UniqueKey<NewUserRecord> NEW_USER_PKEY = Internal.createUniqueKey(NewUser.NEW_USER, DSL.name("new_user_pkey"), new TableField[] { NewUser.NEW_USER.ID }, true);
}
