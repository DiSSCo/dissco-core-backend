/*
 * This file is generated by jOOQ.
 */
package eu.dissco.backend.database.jooq.tables;


import eu.dissco.backend.database.jooq.Indexes;
import eu.dissco.backend.database.jooq.Keys;
import eu.dissco.backend.database.jooq.Public;
import eu.dissco.backend.database.jooq.tables.records.MasJobRecordRecord;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Function7;
import org.jooq.Index;
import org.jooq.JSONB;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Records;
import org.jooq.Row7;
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
public class MasJobRecord extends TableImpl<MasJobRecordRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.mas_job_record</code>
     */
    public static final MasJobRecord MAS_JOB_RECORD = new MasJobRecord();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<MasJobRecordRecord> getRecordType() {
        return MasJobRecordRecord.class;
    }

    /**
     * The column <code>public.mas_job_record.job_id</code>.
     */
    public final TableField<MasJobRecordRecord, UUID> JOB_ID = createField(DSL.name("job_id"), SQLDataType.UUID.nullable(false).defaultValue(DSL.field(DSL.raw("uuid_generate_v4()"), SQLDataType.UUID)), this, "");

    /**
     * The column <code>public.mas_job_record.state</code>.
     */
    public final TableField<MasJobRecordRecord, String> STATE = createField(DSL.name("state"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.mas_job_record.creator_id</code>.
     */
    public final TableField<MasJobRecordRecord, String> CREATOR_ID = createField(DSL.name("creator_id"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.mas_job_record.time_started</code>.
     */
    public final TableField<MasJobRecordRecord, Instant> TIME_STARTED = createField(DSL.name("time_started"), SQLDataType.INSTANT.nullable(false), this, "");

    /**
     * The column <code>public.mas_job_record.time_completed</code>.
     */
    public final TableField<MasJobRecordRecord, Instant> TIME_COMPLETED = createField(DSL.name("time_completed"), SQLDataType.INSTANT, this, "");

    /**
     * The column <code>public.mas_job_record.annotations</code>.
     */
    public final TableField<MasJobRecordRecord, JSONB> ANNOTATIONS = createField(DSL.name("annotations"), SQLDataType.JSONB, this, "");

    /**
     * The column <code>public.mas_job_record.target_id</code>.
     */
    public final TableField<MasJobRecordRecord, String> TARGET_ID = createField(DSL.name("target_id"), SQLDataType.CLOB.nullable(false), this, "");

    private MasJobRecord(Name alias, Table<MasJobRecordRecord> aliased) {
        this(alias, aliased, null);
    }

    private MasJobRecord(Name alias, Table<MasJobRecordRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.mas_job_record</code> table reference
     */
    public MasJobRecord(String alias) {
        this(DSL.name(alias), MAS_JOB_RECORD);
    }

    /**
     * Create an aliased <code>public.mas_job_record</code> table reference
     */
    public MasJobRecord(Name alias) {
        this(alias, MAS_JOB_RECORD);
    }

    /**
     * Create a <code>public.mas_job_record</code> table reference
     */
    public MasJobRecord() {
        this(DSL.name("mas_job_record"), null);
    }

    public <O extends Record> MasJobRecord(Table<O> child, ForeignKey<O, MasJobRecordRecord> key) {
        super(child, key, MAS_JOB_RECORD);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public List<Index> getIndexes() {
        return Arrays.asList(Indexes.MAS_JOB_RECORD_CREATED_IDX, Indexes.MAS_JOB_RECORD_JOB_ID_INDEX);
    }

    @Override
    public UniqueKey<MasJobRecordRecord> getPrimaryKey() {
        return Keys.MAS_JOB_RECORD_PK;
    }

    @Override
    public MasJobRecord as(String alias) {
        return new MasJobRecord(DSL.name(alias), this);
    }

    @Override
    public MasJobRecord as(Name alias) {
        return new MasJobRecord(alias, this);
    }

    @Override
    public MasJobRecord as(Table<?> alias) {
        return new MasJobRecord(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public MasJobRecord rename(String name) {
        return new MasJobRecord(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public MasJobRecord rename(Name name) {
        return new MasJobRecord(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public MasJobRecord rename(Table<?> name) {
        return new MasJobRecord(name.getQualifiedName(), null);
    }

    // -------------------------------------------------------------------------
    // Row7 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row7<UUID, String, String, Instant, Instant, JSONB, String> fieldsRow() {
        return (Row7) super.fieldsRow();
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Function)}.
     */
    public <U> SelectField<U> mapping(Function7<? super UUID, ? super String, ? super String, ? super Instant, ? super Instant, ? super JSONB, ? super String, ? extends U> from) {
        return convertFrom(Records.mapping(from));
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Class,
     * Function)}.
     */
    public <U> SelectField<U> mapping(Class<U> toType, Function7<? super UUID, ? super String, ? super String, ? super Instant, ? super Instant, ? super JSONB, ? super String, ? extends U> from) {
        return convertFrom(toType, Records.mapping(from));
    }
}
