/*
 * This file is generated by jOOQ.
 */
package eu.dissco.backend.database.jooq.tables;


import eu.dissco.backend.database.jooq.Indexes;
import eu.dissco.backend.database.jooq.Keys;
import eu.dissco.backend.database.jooq.Public;
import eu.dissco.backend.database.jooq.tables.records.AnnotationRecord;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Index;
import org.jooq.JSONB;
import org.jooq.Name;
import org.jooq.PlainSQL;
import org.jooq.QueryPart;
import org.jooq.SQL;
import org.jooq.Schema;
import org.jooq.Select;
import org.jooq.Stringly;
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
public class Annotation extends TableImpl<AnnotationRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.annotation</code>
     */
    public static final Annotation ANNOTATION = new Annotation();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<AnnotationRecord> getRecordType() {
        return AnnotationRecord.class;
    }

    /**
     * The column <code>public.annotation.id</code>.
     */
    public final TableField<AnnotationRecord, String> ID = createField(DSL.name("id"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.annotation.version</code>.
     */
    public final TableField<AnnotationRecord, Integer> VERSION = createField(DSL.name("version"), SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>public.annotation.type</code>.
     */
    public final TableField<AnnotationRecord, String> TYPE = createField(DSL.name("type"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.annotation.annotation_hash</code>.
     */
    public final TableField<AnnotationRecord, UUID> ANNOTATION_HASH = createField(DSL.name("annotation_hash"), SQLDataType.UUID, this, "");

    /**
     * The column <code>public.annotation.motivation</code>.
     */
    public final TableField<AnnotationRecord, String> MOTIVATION = createField(DSL.name("motivation"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.annotation.mjr_job_id</code>.
     */
    public final TableField<AnnotationRecord, String> MJR_JOB_ID = createField(DSL.name("mjr_job_id"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.annotation.batch_id</code>.
     */
    public final TableField<AnnotationRecord, UUID> BATCH_ID = createField(DSL.name("batch_id"), SQLDataType.UUID, this, "");

    /**
     * The column <code>public.annotation.creator</code>.
     */
    public final TableField<AnnotationRecord, String> CREATOR = createField(DSL.name("creator"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.annotation.created</code>.
     */
    public final TableField<AnnotationRecord, Instant> CREATED = createField(DSL.name("created"), SQLDataType.INSTANT.nullable(false), this, "");

    /**
     * The column <code>public.annotation.modified</code>.
     */
    public final TableField<AnnotationRecord, Instant> MODIFIED = createField(DSL.name("modified"), SQLDataType.INSTANT.nullable(false), this, "");

    /**
     * The column <code>public.annotation.last_checked</code>.
     */
    public final TableField<AnnotationRecord, Instant> LAST_CHECKED = createField(DSL.name("last_checked"), SQLDataType.INSTANT.nullable(false), this, "");

    /**
     * The column <code>public.annotation.tombstoned</code>.
     */
    public final TableField<AnnotationRecord, Instant> TOMBSTONED = createField(DSL.name("tombstoned"), SQLDataType.INSTANT, this, "");

    /**
     * The column <code>public.annotation.target_id</code>.
     */
    public final TableField<AnnotationRecord, String> TARGET_ID = createField(DSL.name("target_id"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.annotation.data</code>.
     */
    public final TableField<AnnotationRecord, JSONB> DATA = createField(DSL.name("data"), SQLDataType.JSONB, this, "");

    private Annotation(Name alias, Table<AnnotationRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private Annotation(Name alias, Table<AnnotationRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>public.annotation</code> table reference
     */
    public Annotation(String alias) {
        this(DSL.name(alias), ANNOTATION);
    }

    /**
     * Create an aliased <code>public.annotation</code> table reference
     */
    public Annotation(Name alias) {
        this(alias, ANNOTATION);
    }

    /**
     * Create a <code>public.annotation</code> table reference
     */
    public Annotation() {
        this(DSL.name("annotation"), null);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

  @Override
  public List<Index> getIndexes() {
    return Arrays.asList(Indexes.ANNOTATION_HASH, Indexes.ANNOTATION_ID_CREATOR_ID_INDEX,
        Indexes.ANNOTATION_ID_TARGET_ID_INDEX);
  }

    @Override
    public UniqueKey<AnnotationRecord> getPrimaryKey() {
        return Keys.ANNOTATION_PK;
    }

    @Override
    public Annotation as(String alias) {
        return new Annotation(DSL.name(alias), this);
    }

    @Override
    public Annotation as(Name alias) {
        return new Annotation(alias, this);
    }

    @Override
    public Annotation as(Table<?> alias) {
        return new Annotation(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public Annotation rename(String name) {
        return new Annotation(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Annotation rename(Name name) {
        return new Annotation(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public Annotation rename(Table<?> name) {
        return new Annotation(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Annotation where(Condition condition) {
        return new Annotation(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Annotation where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Annotation where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Annotation where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Annotation where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Annotation where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Annotation where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Annotation where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Annotation whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Annotation whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
