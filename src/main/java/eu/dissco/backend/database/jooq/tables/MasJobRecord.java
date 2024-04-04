/*
 * This file is generated by jOOQ.
 */
package eu.dissco.backend.database.jooq.tables;


import eu.dissco.backend.database.jooq.Keys;
import eu.dissco.backend.database.jooq.Public;
import eu.dissco.backend.database.jooq.enums.ErrorCode;
import eu.dissco.backend.database.jooq.enums.MjrJobState;
import eu.dissco.backend.database.jooq.enums.MjrTargetType;
import eu.dissco.backend.database.jooq.tables.records.MasJobRecordRecord;

import java.time.Instant;
import java.util.function.Function;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Function12;
import org.jooq.JSONB;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Records;
import org.jooq.Row12;
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
    public final TableField<MasJobRecordRecord, String> JOB_ID = createField(DSL.name("job_id"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.mas_job_record.job_state</code>.
     */
    public final TableField<MasJobRecordRecord, MjrJobState> JOB_STATE = createField(DSL.name("job_state"), SQLDataType.VARCHAR.nullable(false).asEnumDataType(eu.dissco.backend.database.jooq.enums.MjrJobState.class), this, "");

    /**
     * The column <code>public.mas_job_record.mas_id</code>.
     */
    public final TableField<MasJobRecordRecord, String> MAS_ID = createField(DSL.name("mas_id"), SQLDataType.CLOB.nullable(false), this, "");

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

    /**
     * The column <code>public.mas_job_record.user_id</code>.
     */
    public final TableField<MasJobRecordRecord, String> USER_ID = createField(DSL.name("user_id"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.mas_job_record.target_type</code>.
     */
    public final TableField<MasJobRecordRecord, MjrTargetType> TARGET_TYPE = createField(DSL.name("target_type"), SQLDataType.VARCHAR.asEnumDataType(eu.dissco.backend.database.jooq.enums.MjrTargetType.class), this, "");

    /**
     * The column <code>public.mas_job_record.batching_requested</code>.
     */
    public final TableField<MasJobRecordRecord, Boolean> BATCHING_REQUESTED = createField(DSL.name("batching_requested"), SQLDataType.BOOLEAN, this, "");

    /**
     * The column <code>public.mas_job_record.error</code>.
     */
    public final TableField<MasJobRecordRecord, ErrorCode> ERROR = createField(DSL.name("error"), SQLDataType.VARCHAR.asEnumDataType(eu.dissco.backend.database.jooq.enums.ErrorCode.class), this, "");

    /**
     * The column <code>public.mas_job_record.expires_on</code>.
     */
    public final TableField<MasJobRecordRecord, Instant> EXPIRES_ON = createField(DSL.name("expires_on"), SQLDataType.INSTANT.nullable(false), this, "");

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
    // Row12 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row12<String, MjrJobState, String, Instant, Instant, JSONB, String, String, MjrTargetType, Boolean, ErrorCode, Instant> fieldsRow() {
        return (Row12) super.fieldsRow();
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Function)}.
     */
    public <U> SelectField<U> mapping(Function12<? super String, ? super MjrJobState, ? super String, ? super Instant, ? super Instant, ? super JSONB, ? super String, ? super String, ? super MjrTargetType, ? super Boolean, ? super ErrorCode, ? super Instant, ? extends U> from) {
        return convertFrom(Records.mapping(from));
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Class,
     * Function)}.
     */
    public <U> SelectField<U> mapping(Class<U> toType, Function12<? super String, ? super MjrJobState, ? super String, ? super Instant, ? super Instant, ? super JSONB, ? super String, ? super String, ? super MjrTargetType, ? super Boolean, ? super ErrorCode, ? super Instant, ? extends U> from) {
        return convertFrom(toType, Records.mapping(from));
    }
}
