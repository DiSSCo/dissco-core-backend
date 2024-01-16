/*
 * This file is generated by jOOQ.
 */
package eu.dissco.backend.database.jooq;


import eu.dissco.backend.database.jooq.tables.Annotation;
import eu.dissco.backend.database.jooq.tables.DigitalMediaObject;
import eu.dissco.backend.database.jooq.tables.DigitalSpecimen;
import eu.dissco.backend.database.jooq.tables.MachineAnnotationServicesTmp;
import eu.dissco.backend.database.jooq.tables.MasJobRecordTmp;
import eu.dissco.backend.database.jooq.tables.User;

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
     * The table <code>public.annotation</code>.
     */
    public final Annotation ANNOTATION = Annotation.ANNOTATION;

    /**
     * The table <code>public.digital_media_object</code>.
     */
    public final DigitalMediaObject DIGITAL_MEDIA_OBJECT = DigitalMediaObject.DIGITAL_MEDIA_OBJECT;

    /**
     * The table <code>public.digital_specimen</code>.
     */
    public final DigitalSpecimen DIGITAL_SPECIMEN = DigitalSpecimen.DIGITAL_SPECIMEN;

    /**
     * The table <code>public.machine_annotation_services_tmp</code>.
     */
    public final MachineAnnotationServicesTmp MACHINE_ANNOTATION_SERVICES_TMP = MachineAnnotationServicesTmp.MACHINE_ANNOTATION_SERVICES_TMP;

    /**
     * The table <code>public.mas_job_record_tmp</code>.
     */
    public final MasJobRecordTmp MAS_JOB_RECORD_TMP = MasJobRecordTmp.MAS_JOB_RECORD_TMP;

    /**
     * The table <code>public.user</code>.
     */
    public final User USER = User.USER;

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
            Annotation.ANNOTATION,
            DigitalMediaObject.DIGITAL_MEDIA_OBJECT,
            DigitalSpecimen.DIGITAL_SPECIMEN,
            MachineAnnotationServicesTmp.MACHINE_ANNOTATION_SERVICES_TMP,
            MasJobRecordTmp.MAS_JOB_RECORD_TMP,
            User.USER
        );
    }
}
