/*
 * This file is generated by jOOQ.
 */
package eu.dissco.backend.database.jooq.tables.records;


import eu.dissco.backend.database.jooq.tables.User;

import java.time.Instant;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record8;
import org.jooq.Row8;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class UserRecord extends UpdatableRecordImpl<UserRecord> implements Record8<String, String, String, String, String, String, Instant, Instant> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.user.id</code>.
     */
    public void setId(String value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.user.id</code>.
     */
    public String getId() {
        return (String) get(0);
    }

    /**
     * Setter for <code>public.user.first_name</code>.
     */
    public void setFirstName(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.user.first_name</code>.
     */
    public String getFirstName() {
        return (String) get(1);
    }

    /**
     * Setter for <code>public.user.last_name</code>.
     */
    public void setLastName(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.user.last_name</code>.
     */
    public String getLastName() {
        return (String) get(2);
    }

    /**
     * Setter for <code>public.user.email</code>.
     */
    public void setEmail(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.user.email</code>.
     */
    public String getEmail() {
        return (String) get(3);
    }

    /**
     * Setter for <code>public.user.orcid</code>.
     */
    public void setOrcid(String value) {
        set(4, value);
    }

    /**
     * Getter for <code>public.user.orcid</code>.
     */
    public String getOrcid() {
        return (String) get(4);
    }

    /**
     * Setter for <code>public.user.organization</code>.
     */
    public void setOrganization(String value) {
        set(5, value);
    }

    /**
     * Getter for <code>public.user.organization</code>.
     */
    public String getOrganization() {
        return (String) get(5);
    }

    /**
     * Setter for <code>public.user.created</code>.
     */
    public void setCreated(Instant value) {
        set(6, value);
    }

    /**
     * Getter for <code>public.user.created</code>.
     */
    public Instant getCreated() {
        return (Instant) get(6);
    }

    /**
     * Setter for <code>public.user.updated</code>.
     */
    public void setUpdated(Instant value) {
        set(7, value);
    }

    /**
     * Getter for <code>public.user.updated</code>.
     */
    public Instant getUpdated() {
        return (Instant) get(7);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<String> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record8 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row8<String, String, String, String, String, String, Instant, Instant> fieldsRow() {
        return (Row8) super.fieldsRow();
    }

    @Override
    public Row8<String, String, String, String, String, String, Instant, Instant> valuesRow() {
        return (Row8) super.valuesRow();
    }

    @Override
    public Field<String> field1() {
        return User.USER.ID;
    }

    @Override
    public Field<String> field2() {
        return User.USER.FIRST_NAME;
    }

    @Override
    public Field<String> field3() {
        return User.USER.LAST_NAME;
    }

    @Override
    public Field<String> field4() {
        return User.USER.EMAIL;
    }

    @Override
    public Field<String> field5() {
        return User.USER.ORCID;
    }

    @Override
    public Field<String> field6() {
        return User.USER.ORGANIZATION;
    }

    @Override
    public Field<Instant> field7() {
        return User.USER.CREATED;
    }

    @Override
    public Field<Instant> field8() {
        return User.USER.UPDATED;
    }

    @Override
    public String component1() {
        return getId();
    }

    @Override
    public String component2() {
        return getFirstName();
    }

    @Override
    public String component3() {
        return getLastName();
    }

    @Override
    public String component4() {
        return getEmail();
    }

    @Override
    public String component5() {
        return getOrcid();
    }

    @Override
    public String component6() {
        return getOrganization();
    }

    @Override
    public Instant component7() {
        return getCreated();
    }

    @Override
    public Instant component8() {
        return getUpdated();
    }

    @Override
    public String value1() {
        return getId();
    }

    @Override
    public String value2() {
        return getFirstName();
    }

    @Override
    public String value3() {
        return getLastName();
    }

    @Override
    public String value4() {
        return getEmail();
    }

    @Override
    public String value5() {
        return getOrcid();
    }

    @Override
    public String value6() {
        return getOrganization();
    }

    @Override
    public Instant value7() {
        return getCreated();
    }

    @Override
    public Instant value8() {
        return getUpdated();
    }

    @Override
    public UserRecord value1(String value) {
        setId(value);
        return this;
    }

    @Override
    public UserRecord value2(String value) {
        setFirstName(value);
        return this;
    }

    @Override
    public UserRecord value3(String value) {
        setLastName(value);
        return this;
    }

    @Override
    public UserRecord value4(String value) {
        setEmail(value);
        return this;
    }

    @Override
    public UserRecord value5(String value) {
        setOrcid(value);
        return this;
    }

    @Override
    public UserRecord value6(String value) {
        setOrganization(value);
        return this;
    }

    @Override
    public UserRecord value7(Instant value) {
        setCreated(value);
        return this;
    }

    @Override
    public UserRecord value8(Instant value) {
        setUpdated(value);
        return this;
    }

    @Override
    public UserRecord values(String value1, String value2, String value3, String value4, String value5, String value6, Instant value7, Instant value8) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        value8(value8);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached UserRecord
     */
    public UserRecord() {
        super(User.USER);
    }

    /**
     * Create a detached, initialised UserRecord
     */
    public UserRecord(String id, String firstName, String lastName, String email, String orcid, String organization, Instant created, Instant updated) {
        super(User.USER);

        setId(id);
        setFirstName(firstName);
        setLastName(lastName);
        setEmail(email);
        setOrcid(orcid);
        setOrganization(organization);
        setCreated(created);
        setUpdated(updated);
        resetChangedOnNotNull();
    }
}
