/*
 * This file is generated by jOOQ.
 */
package eu.dissco.backend.database.jooq.tables.records;


import eu.dissco.backend.database.jooq.tables.NewAnnotation;

import java.time.Instant;

import org.jooq.Field;
import org.jooq.JSONB;
import org.jooq.Record15;
import org.jooq.Record2;
import org.jooq.Row15;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class NewAnnotationRecord extends UpdatableRecordImpl<NewAnnotationRecord> implements Record15<String, Integer, String, String, String, String, JSONB, JSONB, Integer, String, Instant, String, JSONB, Instant, Instant> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.new_annotation.id</code>.
     */
    public void setId(String value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.new_annotation.id</code>.
     */
    public String getId() {
        return (String) get(0);
    }

    /**
     * Setter for <code>public.new_annotation.version</code>.
     */
    public void setVersion(Integer value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.new_annotation.version</code>.
     */
    public Integer getVersion() {
        return (Integer) get(1);
    }

    /**
     * Setter for <code>public.new_annotation.type</code>.
     */
    public void setType(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.new_annotation.type</code>.
     */
    public String getType() {
        return (String) get(2);
    }

    /**
     * Setter for <code>public.new_annotation.motivation</code>.
     */
    public void setMotivation(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.new_annotation.motivation</code>.
     */
    public String getMotivation() {
        return (String) get(3);
    }

    /**
     * Setter for <code>public.new_annotation.target_id</code>.
     */
    public void setTargetId(String value) {
        set(4, value);
    }

    /**
     * Getter for <code>public.new_annotation.target_id</code>.
     */
    public String getTargetId() {
        return (String) get(4);
    }

    /**
     * Setter for <code>public.new_annotation.target_field</code>.
     */
    public void setTargetField(String value) {
        set(5, value);
    }

    /**
     * Getter for <code>public.new_annotation.target_field</code>.
     */
    public String getTargetField() {
        return (String) get(5);
    }

    /**
     * Setter for <code>public.new_annotation.target_body</code>.
     */
    public void setTargetBody(JSONB value) {
        set(6, value);
    }

    /**
     * Getter for <code>public.new_annotation.target_body</code>.
     */
    public JSONB getTargetBody() {
        return (JSONB) get(6);
    }

    /**
     * Setter for <code>public.new_annotation.body</code>.
     */
    public void setBody(JSONB value) {
        set(7, value);
    }

    /**
     * Getter for <code>public.new_annotation.body</code>.
     */
    public JSONB getBody() {
        return (JSONB) get(7);
    }

    /**
     * Setter for <code>public.new_annotation.preference_score</code>.
     */
    public void setPreferenceScore(Integer value) {
        set(8, value);
    }

    /**
     * Getter for <code>public.new_annotation.preference_score</code>.
     */
    public Integer getPreferenceScore() {
        return (Integer) get(8);
    }

    /**
     * Setter for <code>public.new_annotation.creator</code>.
     */
    public void setCreator(String value) {
        set(9, value);
    }

    /**
     * Getter for <code>public.new_annotation.creator</code>.
     */
    public String getCreator() {
        return (String) get(9);
    }

    /**
     * Setter for <code>public.new_annotation.created</code>.
     */
    public void setCreated(Instant value) {
        set(10, value);
    }

    /**
     * Getter for <code>public.new_annotation.created</code>.
     */
    public Instant getCreated() {
        return (Instant) get(10);
    }

    /**
     * Setter for <code>public.new_annotation.generator_id</code>.
     */
    public void setGeneratorId(String value) {
        set(11, value);
    }

    /**
     * Getter for <code>public.new_annotation.generator_id</code>.
     */
    public String getGeneratorId() {
        return (String) get(11);
    }

    /**
     * Setter for <code>public.new_annotation.generator_body</code>.
     */
    public void setGeneratorBody(JSONB value) {
        set(12, value);
    }

    /**
     * Getter for <code>public.new_annotation.generator_body</code>.
     */
    public JSONB getGeneratorBody() {
        return (JSONB) get(12);
    }

    /**
     * Setter for <code>public.new_annotation.generated</code>.
     */
    public void setGenerated(Instant value) {
        set(13, value);
    }

    /**
     * Getter for <code>public.new_annotation.generated</code>.
     */
    public Instant getGenerated() {
        return (Instant) get(13);
    }

    /**
     * Setter for <code>public.new_annotation.last_checked</code>.
     */
    public void setLastChecked(Instant value) {
        set(14, value);
    }

    /**
     * Getter for <code>public.new_annotation.last_checked</code>.
     */
    public Instant getLastChecked() {
        return (Instant) get(14);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record2<String, Integer> key() {
        return (Record2) super.key();
    }

    // -------------------------------------------------------------------------
    // Record15 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row15<String, Integer, String, String, String, String, JSONB, JSONB, Integer, String, Instant, String, JSONB, Instant, Instant> fieldsRow() {
        return (Row15) super.fieldsRow();
    }

    @Override
    public Row15<String, Integer, String, String, String, String, JSONB, JSONB, Integer, String, Instant, String, JSONB, Instant, Instant> valuesRow() {
        return (Row15) super.valuesRow();
    }

    @Override
    public Field<String> field1() {
        return NewAnnotation.NEW_ANNOTATION.ID;
    }

    @Override
    public Field<Integer> field2() {
        return NewAnnotation.NEW_ANNOTATION.VERSION;
    }

    @Override
    public Field<String> field3() {
        return NewAnnotation.NEW_ANNOTATION.TYPE;
    }

    @Override
    public Field<String> field4() {
        return NewAnnotation.NEW_ANNOTATION.MOTIVATION;
    }

    @Override
    public Field<String> field5() {
        return NewAnnotation.NEW_ANNOTATION.TARGET_ID;
    }

    @Override
    public Field<String> field6() {
        return NewAnnotation.NEW_ANNOTATION.TARGET_FIELD;
    }

    @Override
    public Field<JSONB> field7() {
        return NewAnnotation.NEW_ANNOTATION.TARGET_BODY;
    }

    @Override
    public Field<JSONB> field8() {
        return NewAnnotation.NEW_ANNOTATION.BODY;
    }

    @Override
    public Field<Integer> field9() {
        return NewAnnotation.NEW_ANNOTATION.PREFERENCE_SCORE;
    }

    @Override
    public Field<String> field10() {
        return NewAnnotation.NEW_ANNOTATION.CREATOR;
    }

    @Override
    public Field<Instant> field11() {
        return NewAnnotation.NEW_ANNOTATION.CREATED;
    }

    @Override
    public Field<String> field12() {
        return NewAnnotation.NEW_ANNOTATION.GENERATOR_ID;
    }

    @Override
    public Field<JSONB> field13() {
        return NewAnnotation.NEW_ANNOTATION.GENERATOR_BODY;
    }

    @Override
    public Field<Instant> field14() {
        return NewAnnotation.NEW_ANNOTATION.GENERATED;
    }

    @Override
    public Field<Instant> field15() {
        return NewAnnotation.NEW_ANNOTATION.LAST_CHECKED;
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
        return getMotivation();
    }

    @Override
    public String component5() {
        return getTargetId();
    }

    @Override
    public String component6() {
        return getTargetField();
    }

    @Override
    public JSONB component7() {
        return getTargetBody();
    }

    @Override
    public JSONB component8() {
        return getBody();
    }

    @Override
    public Integer component9() {
        return getPreferenceScore();
    }

    @Override
    public String component10() {
        return getCreator();
    }

    @Override
    public Instant component11() {
        return getCreated();
    }

    @Override
    public String component12() {
        return getGeneratorId();
    }

    @Override
    public JSONB component13() {
        return getGeneratorBody();
    }

    @Override
    public Instant component14() {
        return getGenerated();
    }

    @Override
    public Instant component15() {
        return getLastChecked();
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
        return getMotivation();
    }

    @Override
    public String value5() {
        return getTargetId();
    }

    @Override
    public String value6() {
        return getTargetField();
    }

    @Override
    public JSONB value7() {
        return getTargetBody();
    }

    @Override
    public JSONB value8() {
        return getBody();
    }

    @Override
    public Integer value9() {
        return getPreferenceScore();
    }

    @Override
    public String value10() {
        return getCreator();
    }

    @Override
    public Instant value11() {
        return getCreated();
    }

    @Override
    public String value12() {
        return getGeneratorId();
    }

    @Override
    public JSONB value13() {
        return getGeneratorBody();
    }

    @Override
    public Instant value14() {
        return getGenerated();
    }

    @Override
    public Instant value15() {
        return getLastChecked();
    }

    @Override
    public NewAnnotationRecord value1(String value) {
        setId(value);
        return this;
    }

    @Override
    public NewAnnotationRecord value2(Integer value) {
        setVersion(value);
        return this;
    }

    @Override
    public NewAnnotationRecord value3(String value) {
        setType(value);
        return this;
    }

    @Override
    public NewAnnotationRecord value4(String value) {
        setMotivation(value);
        return this;
    }

    @Override
    public NewAnnotationRecord value5(String value) {
        setTargetId(value);
        return this;
    }

    @Override
    public NewAnnotationRecord value6(String value) {
        setTargetField(value);
        return this;
    }

    @Override
    public NewAnnotationRecord value7(JSONB value) {
        setTargetBody(value);
        return this;
    }

    @Override
    public NewAnnotationRecord value8(JSONB value) {
        setBody(value);
        return this;
    }

    @Override
    public NewAnnotationRecord value9(Integer value) {
        setPreferenceScore(value);
        return this;
    }

    @Override
    public NewAnnotationRecord value10(String value) {
        setCreator(value);
        return this;
    }

    @Override
    public NewAnnotationRecord value11(Instant value) {
        setCreated(value);
        return this;
    }

    @Override
    public NewAnnotationRecord value12(String value) {
        setGeneratorId(value);
        return this;
    }

    @Override
    public NewAnnotationRecord value13(JSONB value) {
        setGeneratorBody(value);
        return this;
    }

    @Override
    public NewAnnotationRecord value14(Instant value) {
        setGenerated(value);
        return this;
    }

    @Override
    public NewAnnotationRecord value15(Instant value) {
        setLastChecked(value);
        return this;
    }

    @Override
    public NewAnnotationRecord values(String value1, Integer value2, String value3, String value4, String value5, String value6, JSONB value7, JSONB value8, Integer value9, String value10, Instant value11, String value12, JSONB value13, Instant value14, Instant value15) {
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
        value11(value11);
        value12(value12);
        value13(value13);
        value14(value14);
        value15(value15);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached NewAnnotationRecord
     */
    public NewAnnotationRecord() {
        super(NewAnnotation.NEW_ANNOTATION);
    }

    /**
     * Create a detached, initialised NewAnnotationRecord
     */
    public NewAnnotationRecord(String id, Integer version, String type, String motivation, String targetId, String targetField, JSONB targetBody, JSONB body, Integer preferenceScore, String creator, Instant created, String generatorId, JSONB generatorBody, Instant generated, Instant lastChecked) {
        super(NewAnnotation.NEW_ANNOTATION);

        setId(id);
        setVersion(version);
        setType(type);
        setMotivation(motivation);
        setTargetId(targetId);
        setTargetField(targetField);
        setTargetBody(targetBody);
        setBody(body);
        setPreferenceScore(preferenceScore);
        setCreator(creator);
        setCreated(created);
        setGeneratorId(generatorId);
        setGeneratorBody(generatorBody);
        setGenerated(generated);
        setLastChecked(lastChecked);
    }
}
