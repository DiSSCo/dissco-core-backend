/*
 * This file is generated by jOOQ.
 */
package eu.dissco.backend.database.jooq.tables.records;


import eu.dissco.backend.database.jooq.tables.DigitalMediaObject;

import java.time.Instant;

import org.jooq.Field;
import org.jooq.JSONB;
import org.jooq.Record1;
import org.jooq.Record10;
import org.jooq.Row10;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class DigitalMediaObjectRecord extends UpdatableRecordImpl<DigitalMediaObjectRecord> implements Record10<String, Integer, String, String, String, Instant, Instant, Instant, JSONB, JSONB> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.digital_media_object.id</code>.
     */
    public void setId(String value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.digital_media_object.id</code>.
     */
    public String getId() {
        return (String) get(0);
    }

    /**
     * Setter for <code>public.digital_media_object.version</code>.
     */
    public void setVersion(Integer value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.digital_media_object.version</code>.
     */
    public Integer getVersion() {
        return (Integer) get(1);
    }

    /**
     * Setter for <code>public.digital_media_object.type</code>.
     */
    public void setType(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.digital_media_object.type</code>.
     */
    public String getType() {
        return (String) get(2);
    }

    /**
     * Setter for <code>public.digital_media_object.digital_specimen_id</code>.
     */
    public void setDigitalSpecimenId(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.digital_media_object.digital_specimen_id</code>.
     */
    public String getDigitalSpecimenId() {
        return (String) get(3);
    }

    /**
     * Setter for <code>public.digital_media_object.media_url</code>.
     */
    public void setMediaUrl(String value) {
        set(4, value);
    }

    /**
     * Getter for <code>public.digital_media_object.media_url</code>.
     */
    public String getMediaUrl() {
        return (String) get(4);
    }

    /**
     * Setter for <code>public.digital_media_object.created</code>.
     */
    public void setCreated(Instant value) {
        set(5, value);
    }

    /**
     * Getter for <code>public.digital_media_object.created</code>.
     */
    public Instant getCreated() {
        return (Instant) get(5);
    }

    /**
     * Setter for <code>public.digital_media_object.last_checked</code>.
     */
    public void setLastChecked(Instant value) {
        set(6, value);
    }

    /**
     * Getter for <code>public.digital_media_object.last_checked</code>.
     */
    public Instant getLastChecked() {
        return (Instant) get(6);
    }

    /**
     * Setter for <code>public.digital_media_object.deleted</code>.
     */
    public void setDeleted(Instant value) {
        set(7, value);
    }

    /**
     * Getter for <code>public.digital_media_object.deleted</code>.
     */
    public Instant getDeleted() {
        return (Instant) get(7);
    }

    /**
     * Setter for <code>public.digital_media_object.data</code>.
     */
    public void setData(JSONB value) {
        set(8, value);
    }

    /**
     * Getter for <code>public.digital_media_object.data</code>.
     */
    public JSONB getData() {
        return (JSONB) get(8);
    }

    /**
     * Setter for <code>public.digital_media_object.original_data</code>.
     */
    public void setOriginalData(JSONB value) {
        set(9, value);
    }

    /**
     * Getter for <code>public.digital_media_object.original_data</code>.
     */
    public JSONB getOriginalData() {
        return (JSONB) get(9);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<String> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record10 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row10<String, Integer, String, String, String, Instant, Instant, Instant, JSONB, JSONB> fieldsRow() {
        return (Row10) super.fieldsRow();
    }

    @Override
    public Row10<String, Integer, String, String, String, Instant, Instant, Instant, JSONB, JSONB> valuesRow() {
        return (Row10) super.valuesRow();
    }

    @Override
    public Field<String> field1() {
        return DigitalMediaObject.DIGITAL_MEDIA_OBJECT.ID;
    }

    @Override
    public Field<Integer> field2() {
        return DigitalMediaObject.DIGITAL_MEDIA_OBJECT.VERSION;
    }

    @Override
    public Field<String> field3() {
        return DigitalMediaObject.DIGITAL_MEDIA_OBJECT.TYPE;
    }

    @Override
    public Field<String> field4() {
        return DigitalMediaObject.DIGITAL_MEDIA_OBJECT.DIGITAL_SPECIMEN_ID;
    }

    @Override
    public Field<String> field5() {
        return DigitalMediaObject.DIGITAL_MEDIA_OBJECT.MEDIA_URL;
    }

    @Override
    public Field<Instant> field6() {
        return DigitalMediaObject.DIGITAL_MEDIA_OBJECT.CREATED;
    }

    @Override
    public Field<Instant> field7() {
        return DigitalMediaObject.DIGITAL_MEDIA_OBJECT.LAST_CHECKED;
    }

    @Override
    public Field<Instant> field8() {
        return DigitalMediaObject.DIGITAL_MEDIA_OBJECT.DELETED;
    }

    @Override
    public Field<JSONB> field9() {
        return DigitalMediaObject.DIGITAL_MEDIA_OBJECT.DATA;
    }

    @Override
    public Field<JSONB> field10() {
        return DigitalMediaObject.DIGITAL_MEDIA_OBJECT.ORIGINAL_DATA;
    }

    @Override
    public String component1() {
        return getId();
    }

    @Override
    public Integer component2() {
        return getVersion();
    }

    @Override
    public String component3() {
        return getType();
    }

    @Override
    public String component4() {
        return getDigitalSpecimenId();
    }

    @Override
    public String component5() {
        return getMediaUrl();
    }

    @Override
    public Instant component6() {
        return getCreated();
    }

    @Override
    public Instant component7() {
        return getLastChecked();
    }

    @Override
    public Instant component8() {
        return getDeleted();
    }

    @Override
    public JSONB component9() {
        return getData();
    }

    @Override
    public JSONB component10() {
        return getOriginalData();
    }

    @Override
    public String value1() {
        return getId();
    }

    @Override
    public Integer value2() {
        return getVersion();
    }

    @Override
    public String value3() {
        return getType();
    }

    @Override
    public String value4() {
        return getDigitalSpecimenId();
    }

    @Override
    public String value5() {
        return getMediaUrl();
    }

    @Override
    public Instant value6() {
        return getCreated();
    }

    @Override
    public Instant value7() {
        return getLastChecked();
    }

    @Override
    public Instant value8() {
        return getDeleted();
    }

    @Override
    public JSONB value9() {
        return getData();
    }

    @Override
    public JSONB value10() {
        return getOriginalData();
    }

    @Override
    public DigitalMediaObjectRecord value1(String value) {
        setId(value);
        return this;
    }

    @Override
    public DigitalMediaObjectRecord value2(Integer value) {
        setVersion(value);
        return this;
    }

    @Override
    public DigitalMediaObjectRecord value3(String value) {
        setType(value);
        return this;
    }

    @Override
    public DigitalMediaObjectRecord value4(String value) {
        setDigitalSpecimenId(value);
        return this;
    }

    @Override
    public DigitalMediaObjectRecord value5(String value) {
        setMediaUrl(value);
        return this;
    }

    @Override
    public DigitalMediaObjectRecord value6(Instant value) {
        setCreated(value);
        return this;
    }

    @Override
    public DigitalMediaObjectRecord value7(Instant value) {
        setLastChecked(value);
        return this;
    }

    @Override
    public DigitalMediaObjectRecord value8(Instant value) {
        setDeleted(value);
        return this;
    }

    @Override
    public DigitalMediaObjectRecord value9(JSONB value) {
        setData(value);
        return this;
    }

    @Override
    public DigitalMediaObjectRecord value10(JSONB value) {
        setOriginalData(value);
        return this;
    }

    @Override
    public DigitalMediaObjectRecord values(String value1, Integer value2, String value3, String value4, String value5, Instant value6, Instant value7, Instant value8, JSONB value9, JSONB value10) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        value8(value8);
        value9(value9);
        value10(value10);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached DigitalMediaObjectRecord
     */
    public DigitalMediaObjectRecord() {
        super(DigitalMediaObject.DIGITAL_MEDIA_OBJECT);
    }

    /**
     * Create a detached, initialised DigitalMediaObjectRecord
     */
    public DigitalMediaObjectRecord(String id, Integer version, String type, String digitalSpecimenId, String mediaUrl, Instant created, Instant lastChecked, Instant deleted, JSONB data, JSONB originalData) {
        super(DigitalMediaObject.DIGITAL_MEDIA_OBJECT);

        setId(id);
        setVersion(version);
        setType(type);
        setDigitalSpecimenId(digitalSpecimenId);
        setMediaUrl(mediaUrl);
        setCreated(created);
        setLastChecked(lastChecked);
        setDeleted(deleted);
        setData(data);
        setOriginalData(originalData);
        resetChangedOnNotNull();
    }
}
