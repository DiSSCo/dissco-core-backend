/*
 * This file is generated by jOOQ.
 */
package eu.dissco.backend.database.jooq.tables;


import eu.dissco.backend.database.jooq.Keys;
import eu.dissco.backend.database.jooq.Public;
import eu.dissco.backend.database.jooq.tables.records.NewUserRecord;
import java.time.Instant;
import java.util.function.Function;
import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Function8;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Records;
import org.jooq.Row8;
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
public class NewUser extends TableImpl<NewUserRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.new_user</code>
     */
    public static final NewUser NEW_USER = new NewUser();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<NewUserRecord> getRecordType() {
        return NewUserRecord.class;
    }

    /**
     * The column <code>public.new_user.id</code>.
     */
    public final TableField<NewUserRecord, String> ID = createField(DSL.name("id"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.new_user.first_name</code>.
     */
    public final TableField<NewUserRecord, String> FIRST_NAME = createField(DSL.name("first_name"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.new_user.last_name</code>.
     */
    public final TableField<NewUserRecord, String> LAST_NAME = createField(DSL.name("last_name"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.new_user.email</code>.
     */
    public final TableField<NewUserRecord, String> EMAIL = createField(DSL.name("email"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.new_user.orcid</code>.
     */
    public final TableField<NewUserRecord, String> ORCID = createField(DSL.name("orcid"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.new_user.organization</code>.
     */
    public final TableField<NewUserRecord, String> ORGANIZATION = createField(DSL.name("organization"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.new_user.created</code>.
     */
    public final TableField<NewUserRecord, Instant> CREATED = createField(DSL.name("created"), SQLDataType.INSTANT.nullable(false), this, "");

    /**
     * The column <code>public.new_user.updated</code>.
     */
    public final TableField<NewUserRecord, Instant> UPDATED = createField(DSL.name("updated"), SQLDataType.INSTANT.nullable(false), this, "");

    private NewUser(Name alias, Table<NewUserRecord> aliased) {
        this(alias, aliased, null);
    }

    private NewUser(Name alias, Table<NewUserRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.new_user</code> table reference
     */
    public NewUser(String alias) {
        this(DSL.name(alias), NEW_USER);
    }

    /**
     * Create an aliased <code>public.new_user</code> table reference
     */
    public NewUser(Name alias) {
        this(alias, NEW_USER);
    }

    /**
     * Create a <code>public.new_user</code> table reference
     */
    public NewUser() {
        this(DSL.name("new_user"), null);
    }

    public <O extends Record> NewUser(Table<O> child, ForeignKey<O, NewUserRecord> key) {
        super(child, key, NEW_USER);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public UniqueKey<NewUserRecord> getPrimaryKey() {
        return Keys.NEW_USER_PKEY;
    }

    @Override
    public NewUser as(String alias) {
        return new NewUser(DSL.name(alias), this);
    }

    @Override
    public NewUser as(Name alias) {
        return new NewUser(alias, this);
    }

    @Override
    public NewUser as(Table<?> alias) {
        return new NewUser(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public NewUser rename(String name) {
        return new NewUser(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public NewUser rename(Name name) {
        return new NewUser(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public NewUser rename(Table<?> name) {
        return new NewUser(name.getQualifiedName(), null);
    }

    // -------------------------------------------------------------------------
    // Row8 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row8<String, String, String, String, String, String, Instant, Instant> fieldsRow() {
        return (Row8) super.fieldsRow();
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Function)}.
     */
    public <U> SelectField<U> mapping(Function8<? super String, ? super String, ? super String, ? super String, ? super String, ? super String, ? super Instant, ? super Instant, ? extends U> from) {
        return convertFrom(Records.mapping(from));
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Class,
     * Function)}.
     */
    public <U> SelectField<U> mapping(Class<U> toType, Function8<? super String, ? super String, ? super String, ? super String, ? super String, ? super String, ? super Instant, ? super Instant, ? extends U> from) {
        return convertFrom(toType, Records.mapping(from));
    }
}
