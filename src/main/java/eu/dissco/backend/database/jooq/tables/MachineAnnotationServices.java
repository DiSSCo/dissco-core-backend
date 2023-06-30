/*
 * This file is generated by jOOQ.
 */
package eu.dissco.backend.database.jooq.tables;


import eu.dissco.backend.database.jooq.Keys;
import eu.dissco.backend.database.jooq.Public;
import eu.dissco.backend.database.jooq.tables.records.MachineAnnotationServicesRecord;

import java.time.Instant;
import java.util.function.Function;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Function20;
import org.jooq.JSONB;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Records;
import org.jooq.Row20;
import org.jooq.Schema;
import org.jooq.SelectField;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class MachineAnnotationServices extends TableImpl<MachineAnnotationServicesRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.machine_annotation_services</code>
     */
    public static final MachineAnnotationServices MACHINE_ANNOTATION_SERVICES = new MachineAnnotationServices();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<MachineAnnotationServicesRecord> getRecordType() {
        return MachineAnnotationServicesRecord.class;
    }

    /**
     * The column <code>public.machine_annotation_services.id</code>.
     */
    public final TableField<MachineAnnotationServicesRecord, String> ID = createField(DSL.name("id"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.machine_annotation_services.version</code>.
     */
    public final TableField<MachineAnnotationServicesRecord, Integer> VERSION = createField(DSL.name("version"), SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>public.machine_annotation_services.name</code>.
     */
    public final TableField<MachineAnnotationServicesRecord, String> NAME = createField(DSL.name("name"), SQLDataType.VARCHAR.nullable(false), this, "");

    /**
     * The column <code>public.machine_annotation_services.created</code>.
     */
    public final TableField<MachineAnnotationServicesRecord, Instant> CREATED = createField(DSL.name("created"), SQLDataType.INSTANT.nullable(false), this, "");

    /**
     * The column <code>public.machine_annotation_services.administrator</code>.
     */
    public final TableField<MachineAnnotationServicesRecord, String> ADMINISTRATOR = createField(DSL.name("administrator"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column
     * <code>public.machine_annotation_services.container_image</code>.
     */
    public final TableField<MachineAnnotationServicesRecord, String> CONTAINER_IMAGE = createField(DSL.name("container_image"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column
     * <code>public.machine_annotation_services.container_image_tag</code>.
     */
    public final TableField<MachineAnnotationServicesRecord, String> CONTAINER_IMAGE_TAG = createField(DSL.name("container_image_tag"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column
     * <code>public.machine_annotation_services.target_digital_object_filters</code>.
     */
    public final TableField<MachineAnnotationServicesRecord, JSONB> TARGET_DIGITAL_OBJECT_FILTERS = createField(DSL.name("target_digital_object_filters"), SQLDataType.JSONB, this, "");

    /**
     * The column
     * <code>public.machine_annotation_services.service_description</code>.
     */
    public final TableField<MachineAnnotationServicesRecord, String> SERVICE_DESCRIPTION = createField(DSL.name("service_description"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.machine_annotation_services.service_state</code>.
     */
    public final TableField<MachineAnnotationServicesRecord, String> SERVICE_STATE = createField(DSL.name("service_state"), SQLDataType.CLOB, this, "");

    /**
     * The column
     * <code>public.machine_annotation_services.source_code_repository</code>.
     */
    public final TableField<MachineAnnotationServicesRecord, String> SOURCE_CODE_REPOSITORY = createField(DSL.name("source_code_repository"), SQLDataType.CLOB, this, "");

    /**
     * The column
     * <code>public.machine_annotation_services.service_availability</code>.
     */
    public final TableField<MachineAnnotationServicesRecord, String> SERVICE_AVAILABILITY = createField(DSL.name("service_availability"), SQLDataType.CLOB, this, "");

    /**
     * The column
     * <code>public.machine_annotation_services.code_maintainer</code>.
     */
    public final TableField<MachineAnnotationServicesRecord, String> CODE_MAINTAINER = createField(DSL.name("code_maintainer"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.machine_annotation_services.code_license</code>.
     */
    public final TableField<MachineAnnotationServicesRecord, String> CODE_LICENSE = createField(DSL.name("code_license"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.machine_annotation_services.dependencies</code>.
     */
    public final TableField<MachineAnnotationServicesRecord, String[]> DEPENDENCIES = createField(DSL.name("dependencies"), SQLDataType.CLOB.array(), this, "");

    /**
     * The column
     * <code>public.machine_annotation_services.support_contact</code>.
     */
    public final TableField<MachineAnnotationServicesRecord, String> SUPPORT_CONTACT = createField(DSL.name("support_contact"), SQLDataType.CLOB, this, "");

    /**
     * The column
     * <code>public.machine_annotation_services.sla_documentation</code>.
     */
    public final TableField<MachineAnnotationServicesRecord, String> SLA_DOCUMENTATION = createField(DSL.name("sla_documentation"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.machine_annotation_services.topicname</code>.
     */
    public final TableField<MachineAnnotationServicesRecord, String> TOPICNAME = createField(DSL.name("topicname"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.machine_annotation_services.maxreplicas</code>.
     */
    public final TableField<MachineAnnotationServicesRecord, Integer> MAXREPLICAS = createField(DSL.name("maxreplicas"), SQLDataType.INTEGER, this, "");

    /**
     * The column <code>public.machine_annotation_services.deleted_on</code>.
     */
    public final TableField<MachineAnnotationServicesRecord, Instant> DELETED_ON = createField(DSL.name("deleted_on"), SQLDataType.INSTANT, this, "");

    private MachineAnnotationServices(Name alias, Table<MachineAnnotationServicesRecord> aliased) {
        this(alias, aliased, null);
    }

    private MachineAnnotationServices(Name alias, Table<MachineAnnotationServicesRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.machine_annotation_services</code> table
     * reference
     */
    public MachineAnnotationServices(String alias) {
        this(DSL.name(alias), MACHINE_ANNOTATION_SERVICES);
    }

    /**
     * Create an aliased <code>public.machine_annotation_services</code> table
     * reference
     */
    public MachineAnnotationServices(Name alias) {
        this(alias, MACHINE_ANNOTATION_SERVICES);
    }

    /**
     * Create a <code>public.machine_annotation_services</code> table reference
     */
    public MachineAnnotationServices() {
        this(DSL.name("machine_annotation_services"), null);
    }

    public <O extends Record> MachineAnnotationServices(Table<O> child, ForeignKey<O, MachineAnnotationServicesRecord> key) {
        super(child, key, MACHINE_ANNOTATION_SERVICES);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public UniqueKey<MachineAnnotationServicesRecord> getPrimaryKey() {
        return Keys.MACHINE_ANNOTATION_SERVICES_PKEY;
    }

    @Override
    public MachineAnnotationServices as(String alias) {
        return new MachineAnnotationServices(DSL.name(alias), this);
    }

    @Override
    public MachineAnnotationServices as(Name alias) {
        return new MachineAnnotationServices(alias, this);
    }

    @Override
    public MachineAnnotationServices as(Table<?> alias) {
        return new MachineAnnotationServices(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public MachineAnnotationServices rename(String name) {
        return new MachineAnnotationServices(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public MachineAnnotationServices rename(Name name) {
        return new MachineAnnotationServices(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public MachineAnnotationServices rename(Table<?> name) {
        return new MachineAnnotationServices(name.getQualifiedName(), null);
    }

    // -------------------------------------------------------------------------
    // Row20 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row20<String, Integer, String, Instant, String, String, String, JSONB, String, String, String, String, String, String, String[], String, String, String, Integer, Instant> fieldsRow() {
        return (Row20) super.fieldsRow();
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Function)}.
     */
    public <U> SelectField<U> mapping(Function20<? super String, ? super Integer, ? super String, ? super Instant, ? super String, ? super String, ? super String, ? super JSONB, ? super String, ? super String, ? super String, ? super String, ? super String, ? super String, ? super String[], ? super String, ? super String, ? super String, ? super Integer, ? super Instant, ? extends U> from) {
        return convertFrom(Records.mapping(from));
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Class,
     * Function)}.
     */
    public <U> SelectField<U> mapping(Class<U> toType, Function20<? super String, ? super Integer, ? super String, ? super Instant, ? super String, ? super String, ? super String, ? super JSONB, ? super String, ? super String, ? super String, ? super String, ? super String, ? super String, ? super String[], ? super String, ? super String, ? super String, ? super Integer, ? super Instant, ? extends U> from) {
        return convertFrom(toType, Records.mapping(from));
    }
}
