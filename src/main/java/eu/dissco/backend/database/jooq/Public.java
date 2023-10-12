/*
 * This file is generated by jOOQ.
 */
package eu.dissco.backend.database.jooq;


import eu.dissco.backend.database.jooq.tables.DigitalMediaObject;
import eu.dissco.backend.database.jooq.tables.DigitalSpecimen;
import eu.dissco.backend.database.jooq.tables.MachineAnnotationServices;
import eu.dissco.backend.database.jooq.tables.MasJobRecord;
import eu.dissco.backend.database.jooq.tables.NewAnnotation;
import eu.dissco.backend.database.jooq.tables.NewUser;

import java.util.Arrays;
import java.util.List;

import org.jooq.Catalog;
import org.jooq.Table;
import org.jooq.impl.SchemaImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Public extends SchemaImpl {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public</code>
     */
    public static final Public PUBLIC = new Public();

    /**
     * The table <code>public.digital_media_object</code>.
     */
    public final DigitalMediaObject DIGITAL_MEDIA_OBJECT = DigitalMediaObject.DIGITAL_MEDIA_OBJECT;

    /**
     * The table <code>public.digital_specimen</code>.
     */
    public final DigitalSpecimen DIGITAL_SPECIMEN = DigitalSpecimen.DIGITAL_SPECIMEN;

    /**
     * The table <code>public.machine_annotation_services</code>.
     */
    public final MachineAnnotationServices MACHINE_ANNOTATION_SERVICES = MachineAnnotationServices.MACHINE_ANNOTATION_SERVICES;

    /**
     * The table <code>public.mas_job_record</code>.
     */
    public final MasJobRecord MAS_JOB_RECORD = MasJobRecord.MAS_JOB_RECORD;

    /**
     * The table <code>public.new_annotation</code>.
     */
    public final NewAnnotation NEW_ANNOTATION = NewAnnotation.NEW_ANNOTATION;

    /**
     * The table <code>public.new_user</code>.
     */
    public final NewUser NEW_USER = NewUser.NEW_USER;

    /**
     * No further instances allowed
     */
    private Public() {
        super("public", null);
    }


    @Override
    public Catalog getCatalog() {
        return DefaultCatalog.DEFAULT_CATALOG;
    }

    @Override
    public final List<Table<?>> getTables() {
        return Arrays.asList(
            DigitalMediaObject.DIGITAL_MEDIA_OBJECT,
            DigitalSpecimen.DIGITAL_SPECIMEN,
            MachineAnnotationServices.MACHINE_ANNOTATION_SERVICES,
            MasJobRecord.MAS_JOB_RECORD,
            NewAnnotation.NEW_ANNOTATION,
            NewUser.NEW_USER
        );
    }
}
