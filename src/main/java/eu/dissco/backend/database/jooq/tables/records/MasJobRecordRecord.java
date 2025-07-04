/*
 * This file is generated by jOOQ.
 */
package eu.dissco.backend.database.jooq.tables.records;


import eu.dissco.backend.database.jooq.enums.ErrorCode;
import eu.dissco.backend.database.jooq.enums.JobState;
import eu.dissco.backend.database.jooq.enums.MjrTargetType;
import eu.dissco.backend.database.jooq.tables.MasJobRecord;
import java.time.Instant;
import org.jooq.JSONB;
import org.jooq.Record1;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class MasJobRecordRecord extends UpdatableRecordImpl<MasJobRecordRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.mas_job_record.job_id</code>.
     */
    public void setJobId(String value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.mas_job_record.job_id</code>.
     */
    public String getJobId() {
        return (String) get(0);
    }

    /**
     * Setter for <code>public.mas_job_record.job_state</code>.
     */
    public void setJobState(JobState value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.mas_job_record.job_state</code>.
     */
    public JobState getJobState() {
        return (JobState) get(1);
    }

    /**
     * Setter for <code>public.mas_job_record.mas_id</code>.
     */
    public void setMasId(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.mas_job_record.mas_id</code>.
     */
    public String getMasId() {
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

    /**
     * Setter for <code>public.mas_job_record.creator</code>.
     */
    public void setCreator(String value) {
        set(7, value);
    }

    /**
     * Getter for <code>public.mas_job_record.creator</code>.
     */
    public String getCreator() {
        return (String) get(7);
    }

    /**
     * Setter for <code>public.mas_job_record.target_type</code>.
     */
    public void setTargetType(MjrTargetType value) {
        set(8, value);
    }

    /**
     * Getter for <code>public.mas_job_record.target_type</code>.
     */
    public MjrTargetType getTargetType() {
        return (MjrTargetType) get(8);
    }

    /**
     * Setter for <code>public.mas_job_record.batching_requested</code>.
     */
    public void setBatchingRequested(Boolean value) {
        set(9, value);
    }

    /**
     * Getter for <code>public.mas_job_record.batching_requested</code>.
     */
    public Boolean getBatchingRequested() {
        return (Boolean) get(9);
    }

    /**
     * Setter for <code>public.mas_job_record.error</code>.
     */
    public void setError(ErrorCode value) {
        set(10, value);
    }

    /**
     * Getter for <code>public.mas_job_record.error</code>.
     */
    public ErrorCode getError() {
        return (ErrorCode) get(10);
    }

    /**
     * Setter for <code>public.mas_job_record.expires_on</code>.
     */
    public void setExpiresOn(Instant value) {
        set(11, value);
    }

    /**
     * Getter for <code>public.mas_job_record.expires_on</code>.
     */
    public Instant getExpiresOn() {
        return (Instant) get(11);
    }

  /**
   * Setter for <code>public.mas_job_record.error_message</code>.
   */
  public void setErrorMessage(String value) {
    set(12, value);
  }

  /**
   * Getter for <code>public.mas_job_record.error_message</code>.
   */
  public String getErrorMessage() {
    return (String) get(12);
  }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<String> key() {
        return (Record1) super.key();
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
    public MasJobRecordRecord(String jobId, JobState jobState, String masId, Instant timeStarted,
        Instant timeCompleted, JSONB annotations, String targetId, String creator,
        MjrTargetType targetType, Boolean batchingRequested, ErrorCode error, Instant expiresOn,
        String errorMessage) {
      super(MasJobRecord.MAS_JOB_RECORD);

        setJobId(jobId);
        setJobState(jobState);
        setMasId(masId);
        setTimeStarted(timeStarted);
        setTimeCompleted(timeCompleted);
        setAnnotations(annotations);
        setTargetId(targetId);
        setCreator(creator);
        setTargetType(targetType);
        setBatchingRequested(batchingRequested);
        setError(error);
        setExpiresOn(expiresOn);
      setErrorMessage(errorMessage);
      resetChangedOnNotNull();
    }
}
