/*
 * This file is generated by jOOQ.
 */
package eu.dissco.backend.database.jooq.tables.records;


import eu.dissco.backend.database.jooq.tables.MasJobRecord;

import java.time.Instant;
import java.util.UUID;

import org.jooq.Field;
import org.jooq.JSONB;
import org.jooq.Record1;
import org.jooq.Record7;
import org.jooq.Row7;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class MasJobRecordRecord extends UpdatableRecordImpl<MasJobRecordRecord> implements Record7<UUID, String, String, Instant, Instant, JSONB, String> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.mas_job_record.job_id</code>.
     */
    public void setJobId(UUID value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.mas_job_record.job_id</code>.
     */
    public UUID getJobId() {
        return (UUID) get(0);
    }

    /**
     * Setter for <code>public.mas_job_record.state</code>.
     */
    public void setState(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.mas_job_record.state</code>.
     */
    public String getState() {
        return (String) get(1);
    }

    /**
     * Setter for <code>public.mas_job_record.creator_id</code>.
     */
    public void setCreatorId(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.mas_job_record.creator_id</code>.
     */
    public String getCreatorId() {
        return (String) get(2);
    }

    /**
     * Setter for <code>public.mas_job_record.time_started</code>.
     */
    public void setTimeStarted(Instant value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.mas_job_record.time_started</code>.
     */
    public Instant getTimeStarted() {
        return (Instant) get(3);
    }

    /**
     * Setter for <code>public.mas_job_record.time_completed</code>.
     */
    public void setTimeCompleted(Instant value) {
        set(4, value);
    }

    /**
     * Getter for <code>public.mas_job_record.time_completed</code>.
     */
    public Instant getTimeCompleted() {
        return (Instant) get(4);
    }

    /**
     * Setter for <code>public.mas_job_record.annotations</code>.
     */
    public void setAnnotations(JSONB value) {
        set(5, value);
    }

    /**
     * Getter for <code>public.mas_job_record.annotations</code>.
     */
    public JSONB getAnnotations() {
        return (JSONB) get(5);
    }

    /**
     * Setter for <code>public.mas_job_record.target_id</code>.
     */
    public void setTargetId(String value) {
        set(6, value);
    }

    /**
     * Getter for <code>public.mas_job_record.target_id</code>.
     */
    public String getTargetId() {
        return (String) get(6);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<UUID> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record7 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row7<UUID, String, String, Instant, Instant, JSONB, String> fieldsRow() {
        return (Row7) super.fieldsRow();
    }

    @Override
    public Row7<UUID, String, String, Instant, Instant, JSONB, String> valuesRow() {
        return (Row7) super.valuesRow();
    }

    @Override
    public Field<UUID> field1() {
        return MasJobRecord.MAS_JOB_RECORD.JOB_ID;
    }

    @Override
    public Field<String> field2() {
        return MasJobRecord.MAS_JOB_RECORD.STATE;
    }

    @Override
    public Field<String> field3() {
        return MasJobRecord.MAS_JOB_RECORD.CREATOR_ID;
    }

    @Override
    public Field<Instant> field4() {
        return MasJobRecord.MAS_JOB_RECORD.TIME_STARTED;
    }

    @Override
    public Field<Instant> field5() {
        return MasJobRecord.MAS_JOB_RECORD.TIME_COMPLETED;
    }

    @Override
    public Field<JSONB> field6() {
        return MasJobRecord.MAS_JOB_RECORD.ANNOTATIONS;
    }

    @Override
    public Field<String> field7() {
        return MasJobRecord.MAS_JOB_RECORD.TARGET_ID;
    }

    @Override
    public UUID component1() {
        return getJobId();
    }

    @Override
    public String component2() {
        return getState();
    }

    @Override
    public String component3() {
        return getCreatorId();
    }

    @Override
    public Instant component4() {
        return getTimeStarted();
    }

    @Override
    public Instant component5() {
        return getTimeCompleted();
    }

    @Override
    public JSONB component6() {
        return getAnnotations();
    }

    @Override
    public String component7() {
        return getTargetId();
    }

    @Override
    public UUID value1() {
        return getJobId();
    }

    @Override
    public String value2() {
        return getState();
    }

    @Override
    public String value3() {
        return getCreatorId();
    }

    @Override
    public Instant value4() {
        return getTimeStarted();
    }

    @Override
    public Instant value5() {
        return getTimeCompleted();
    }

    @Override
    public JSONB value6() {
        return getAnnotations();
    }

    @Override
    public String value7() {
        return getTargetId();
    }

    @Override
    public MasJobRecordRecord value1(UUID value) {
        setJobId(value);
        return this;
    }

    @Override
    public MasJobRecordRecord value2(String value) {
        setState(value);
        return this;
    }

    @Override
    public MasJobRecordRecord value3(String value) {
        setCreatorId(value);
        return this;
    }

    @Override
    public MasJobRecordRecord value4(Instant value) {
        setTimeStarted(value);
        return this;
    }

    @Override
    public MasJobRecordRecord value5(Instant value) {
        setTimeCompleted(value);
        return this;
    }

    @Override
    public MasJobRecordRecord value6(JSONB value) {
        setAnnotations(value);
        return this;
    }

    @Override
    public MasJobRecordRecord value7(String value) {
        setTargetId(value);
        return this;
    }

    @Override
    public MasJobRecordRecord values(UUID value1, String value2, String value3, Instant value4, Instant value5, JSONB value6, String value7) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached MasJobRecordRecord
     */
    public MasJobRecordRecord() {
        super(MasJobRecord.MAS_JOB_RECORD);
    }

    /**
     * Create a detached, initialised MasJobRecordRecord
     */
    public MasJobRecordRecord(UUID jobId, String state, String creatorId, Instant timeStarted, Instant timeCompleted, JSONB annotations, String targetId) {
        super(MasJobRecord.MAS_JOB_RECORD);

        setJobId(jobId);
        setState(state);
        setCreatorId(creatorId);
        setTimeStarted(timeStarted);
        setTimeCompleted(timeCompleted);
        setAnnotations(annotations);
        setTargetId(targetId);
        resetChangedOnNotNull();
    }
}