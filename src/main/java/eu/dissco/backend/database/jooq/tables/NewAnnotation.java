/*
 * This file is generated by jOOQ.
 */
package eu.dissco.backend.database.jooq.tables;


import eu.dissco.backend.database.jooq.Keys;
import eu.dissco.backend.database.jooq.Public;
import eu.dissco.backend.database.jooq.tables.records.NewAnnotationRecord;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.JSONB;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row16;
import org.jooq.Schema;
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
public class NewAnnotation extends TableImpl<NewAnnotationRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.new_annotation</code>
     */
    public static final NewAnnotation NEW_ANNOTATION = new NewAnnotation();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<NewAnnotationRecord> getRecordType() {
        return NewAnnotationRecord.class;
    }

    /**
     * The column <code>public.new_annotation.id</code>.
     */
    public final TableField<NewAnnotationRecord, String> ID = createField(DSL.name("id"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.new_annotation.version</code>.
     */
    public final TableField<NewAnnotationRecord, Integer> VERSION = createField(DSL.name("version"), SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>public.new_annotation.type</code>.
     */
    public final TableField<NewAnnotationRecord, String> TYPE = createField(DSL.name("type"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.new_annotation.motivation</code>.
     */
    public final TableField<NewAnnotationRecord, String> MOTIVATION = createField(DSL.name("motivation"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.new_annotation.target_id</code>.
     */
    public final TableField<NewAnnotationRecord, String> TARGET_ID = createField(DSL.name("target_id"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.new_annotation.target_field</code>.
     */
    public final TableField<NewAnnotationRecord, String> TARGET_FIELD = createField(DSL.name("target_field"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.new_annotation.target_body</code>.
     */
    public final TableField<NewAnnotationRecord, JSONB> TARGET_BODY = createField(DSL.name("target_body"), SQLDataType.JSONB.nullable(false), this, "");

    /**
     * The column <code>public.new_annotation.body</code>.
     */
    public final TableField<NewAnnotationRecord, JSONB> BODY = createField(DSL.name("body"), SQLDataType.JSONB.nullable(false), this, "");

    /**
     * The column <code>public.new_annotation.preference_score</code>.
     */
    public final TableField<NewAnnotationRecord, Integer> PREFERENCE_SCORE = createField(DSL.name("preference_score"), SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>public.new_annotation.creator</code>.
     */
    public final TableField<NewAnnotationRecord, String> CREATOR = createField(DSL.name("creator"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.new_annotation.created</code>.
     */
    public final TableField<NewAnnotationRecord, Instant> CREATED = createField(DSL.name("created"), SQLDataType.INSTANT.nullable(false), this, "");

    /**
     * The column <code>public.new_annotation.generator_id</code>.
     */
    public final TableField<NewAnnotationRecord, String> GENERATOR_ID = createField(DSL.name("generator_id"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.new_annotation.generator_body</code>.
     */
    public final TableField<NewAnnotationRecord, JSONB> GENERATOR_BODY = createField(DSL.name("generator_body"), SQLDataType.JSONB.nullable(false), this, "");

    /**
     * The column <code>public.new_annotation.generated</code>.
     */
    public final TableField<NewAnnotationRecord, Instant> GENERATED = createField(DSL.name("generated"), SQLDataType.INSTANT.nullable(false), this, "");

    /**
     * The column <code>public.new_annotation.last_checked</code>.
     */
    public final TableField<NewAnnotationRecord, Instant> LAST_CHECKED = createField(DSL.name("last_checked"), SQLDataType.INSTANT.nullable(false), this, "");

    /**
     * The column <code>public.new_annotation.deleted</code>.
     */
    public final TableField<NewAnnotationRecord, Instant> DELETED = createField(DSL.name("deleted"), SQLDataType.INSTANT, this, "");

    private NewAnnotation(Name alias, Table<NewAnnotationRecord> aliased) {
        this(alias, aliased, null);
    }

    private NewAnnotation(Name alias, Table<NewAnnotationRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.new_annotation</code> table reference
     */
    public NewAnnotation(String alias) {
        this(DSL.name(alias), NEW_ANNOTATION);
    }

    /**
     * Create an aliased <code>public.new_annotation</code> table reference
     */
    public NewAnnotation(Name alias) {
        this(alias, NEW_ANNOTATION);
    }

    /**
     * Create a <code>public.new_annotation</code> table reference
     */
    public NewAnnotation() {
        this(DSL.name("new_annotation"), null);
    }

    public <O extends Record> NewAnnotation(Table<O> child, ForeignKey<O, NewAnnotationRecord> key) {
        super(child, key, NEW_ANNOTATION);
    }

    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    @Override
    public UniqueKey<NewAnnotationRecord> getPrimaryKey() {
        return Keys.NEW_ANNOTATION_PKEY;
    }

    @Override
    public List<UniqueKey<NewAnnotationRecord>> getKeys() {
        return Arrays.<UniqueKey<NewAnnotationRecord>>asList(Keys.NEW_ANNOTATION_PKEY);
    }

    @Override
    public NewAnnotation as(String alias) {
        return new NewAnnotation(DSL.name(alias), this);
    }

    @Override
    public NewAnnotation as(Name alias) {
        return new NewAnnotation(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public NewAnnotation rename(String name) {
        return new NewAnnotation(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public NewAnnotation rename(Name name) {
        return new NewAnnotation(name, null);
    }

    // -------------------------------------------------------------------------
    // Row16 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row16<String, Integer, String, String, String, String, JSONB, JSONB, Integer, String, Instant, String, JSONB, Instant, Instant, Instant> fieldsRow() {
        return (Row16) super.fieldsRow();
    }
}
