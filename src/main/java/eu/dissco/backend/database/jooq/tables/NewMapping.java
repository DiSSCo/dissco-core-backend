/*
 * This file is generated by jOOQ.
 */
package eu.dissco.backend.database.jooq.tables;


import eu.dissco.backend.database.jooq.Keys;
import eu.dissco.backend.database.jooq.Public;
import eu.dissco.backend.database.jooq.tables.records.NewMappingRecord;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.JSONB;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row8;
import org.jooq.Schema;
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
public class NewMapping extends TableImpl<NewMappingRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.new_mapping</code>
     */
    public static final NewMapping NEW_MAPPING = new NewMapping();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<NewMappingRecord> getRecordType() {
        return NewMappingRecord.class;
    }

    /**
     * The column <code>public.new_mapping.id</code>.
     */
    public final TableField<NewMappingRecord, String> ID = createField(DSL.name("id"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.new_mapping.version</code>.
     */
    public final TableField<NewMappingRecord, Integer> VERSION = createField(DSL.name("version"), SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>public.new_mapping.name</code>.
     */
    public final TableField<NewMappingRecord, String> NAME = createField(DSL.name("name"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.new_mapping.description</code>.
     */
    public final TableField<NewMappingRecord, String> DESCRIPTION = createField(DSL.name("description"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.new_mapping.mapping</code>.
     */
    public final TableField<NewMappingRecord, JSONB> MAPPING = createField(DSL.name("mapping"), SQLDataType.JSONB.nullable(false), this, "");

    /**
     * The column <code>public.new_mapping.created</code>.
     */
    public final TableField<NewMappingRecord, Instant> CREATED = createField(DSL.name("created"), SQLDataType.INSTANT.nullable(false), this, "");

    /**
     * The column <code>public.new_mapping.creator</code>.
     */
    public final TableField<NewMappingRecord, String> CREATOR = createField(DSL.name("creator"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.new_mapping.deleted</code>.
     */
    public final TableField<NewMappingRecord, Instant> DELETED = createField(DSL.name("deleted"), SQLDataType.INSTANT, this, "");

    private NewMapping(Name alias, Table<NewMappingRecord> aliased) {
        this(alias, aliased, null);
    }

    private NewMapping(Name alias, Table<NewMappingRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.new_mapping</code> table reference
     */
    public NewMapping(String alias) {
        this(DSL.name(alias), NEW_MAPPING);
    }

    /**
     * Create an aliased <code>public.new_mapping</code> table reference
     */
    public NewMapping(Name alias) {
        this(alias, NEW_MAPPING);
    }

    /**
     * Create a <code>public.new_mapping</code> table reference
     */
    public NewMapping() {
        this(DSL.name("new_mapping"), null);
    }

    public <O extends Record> NewMapping(Table<O> child, ForeignKey<O, NewMappingRecord> key) {
        super(child, key, NEW_MAPPING);
    }

    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    @Override
    public UniqueKey<NewMappingRecord> getPrimaryKey() {
        return Keys.NEW_MAPPING_PK;
    }

    @Override
    public List<UniqueKey<NewMappingRecord>> getKeys() {
        return Arrays.<UniqueKey<NewMappingRecord>>asList(Keys.NEW_MAPPING_PK);
    }

    @Override
    public NewMapping as(String alias) {
        return new NewMapping(DSL.name(alias), this);
    }

    @Override
    public NewMapping as(Name alias) {
        return new NewMapping(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public NewMapping rename(String name) {
        return new NewMapping(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public NewMapping rename(Name name) {
        return new NewMapping(name, null);
    }

    // -------------------------------------------------------------------------
    // Row8 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row8<String, Integer, String, String, JSONB, Instant, String, Instant> fieldsRow() {
        return (Row8) super.fieldsRow();
    }
}
