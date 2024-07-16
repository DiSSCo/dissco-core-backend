/*
 * This file is generated by jOOQ.
 */
package eu.dissco.backend.database.jooq.tables.records;


import eu.dissco.backend.database.jooq.tables.User;

import java.time.Instant;

import org.jooq.Record1;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class UserRecord extends UpdatableRecordImpl<UserRecord> {

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
