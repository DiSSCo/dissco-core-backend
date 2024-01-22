/*
 * This file is generated by jOOQ.
 */
package eu.dissco.backend.database.jooq.tables.records;


import eu.dissco.backend.database.jooq.tables.MachineAnnotationServices;

import java.time.Instant;

import org.jooq.Field;
import org.jooq.JSONB;
import org.jooq.Record1;
import org.jooq.Record21;
import org.jooq.Row21;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class MachineAnnotationServicesRecord extends UpdatableRecordImpl<MachineAnnotationServicesRecord> implements Record21<String, Integer, String, Instant, String, String, String, JSONB, String, String, String, String, String, String, String[], String, String, String, Integer, Instant, Boolean> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.machine_annotation_services.id</code>.
     */
    public void setId(String value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.machine_annotation_services.id</code>.
     */
    public String getId() {
        return (String) get(0);
    }

    /**
     * Setter for <code>public.machine_annotation_services.version</code>.
     */
    public void setVersion(Integer value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.machine_annotation_services.version</code>.
     */
    public Integer getVersion() {
        return (Integer) get(1);
    }

    /**
     * Setter for <code>public.machine_annotation_services.name</code>.
     */
    public void setName(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.machine_annotation_services.name</code>.
     */
    public String getName() {
        return (String) get(2);
    }

    /**
     * Setter for <code>public.machine_annotation_services.created</code>.
     */
    public void setCreated(Instant value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.machine_annotation_services.created</code>.
     */
    public Instant getCreated() {
        return (Instant) get(3);
    }

    /**
     * Setter for <code>public.machine_annotation_services.administrator</code>.
     */
    public void setAdministrator(String value) {
        set(4, value);
    }

    /**
     * Getter for <code>public.machine_annotation_services.administrator</code>.
     */
    public String getAdministrator() {
        return (String) get(4);
    }

    /**
     * Setter for
     * <code>public.machine_annotation_services.container_image</code>.
     */
    public void setContainerImage(String value) {
        set(5, value);
    }

    /**
     * Getter for
     * <code>public.machine_annotation_services.container_image</code>.
     */
    public String getContainerImage() {
        return (String) get(5);
    }

    /**
     * Setter for
     * <code>public.machine_annotation_services.container_image_tag</code>.
     */
    public void setContainerImageTag(String value) {
        set(6, value);
    }

    /**
     * Getter for
     * <code>public.machine_annotation_services.container_image_tag</code>.
     */
    public String getContainerImageTag() {
        return (String) get(6);
    }

    /**
     * Setter for
     * <code>public.machine_annotation_services.target_digital_object_filters</code>.
     */
    public void setTargetDigitalObjectFilters(JSONB value) {
        set(7, value);
    }

    /**
     * Getter for
     * <code>public.machine_annotation_services.target_digital_object_filters</code>.
     */
    public JSONB getTargetDigitalObjectFilters() {
        return (JSONB) get(7);
    }

    /**
     * Setter for
     * <code>public.machine_annotation_services.service_description</code>.
     */
    public void setServiceDescription(String value) {
        set(8, value);
    }

    /**
     * Getter for
     * <code>public.machine_annotation_services.service_description</code>.
     */
    public String getServiceDescription() {
        return (String) get(8);
    }

    /**
     * Setter for <code>public.machine_annotation_services.service_state</code>.
     */
    public void setServiceState(String value) {
        set(9, value);
    }

    /**
     * Getter for <code>public.machine_annotation_services.service_state</code>.
     */
    public String getServiceState() {
        return (String) get(9);
    }

    /**
     * Setter for
     * <code>public.machine_annotation_services.source_code_repository</code>.
     */
    public void setSourceCodeRepository(String value) {
        set(10, value);
    }

    /**
     * Getter for
     * <code>public.machine_annotation_services.source_code_repository</code>.
     */
    public String getSourceCodeRepository() {
        return (String) get(10);
    }

    /**
     * Setter for
     * <code>public.machine_annotation_services.service_availability</code>.
     */
    public void setServiceAvailability(String value) {
        set(11, value);
    }

    /**
     * Getter for
     * <code>public.machine_annotation_services.service_availability</code>.
     */
    public String getServiceAvailability() {
        return (String) get(11);
    }

    /**
     * Setter for
     * <code>public.machine_annotation_services.code_maintainer</code>.
     */
    public void setCodeMaintainer(String value) {
        set(12, value);
    }

    /**
     * Getter for
     * <code>public.machine_annotation_services.code_maintainer</code>.
     */
    public String getCodeMaintainer() {
        return (String) get(12);
    }

    /**
     * Setter for <code>public.machine_annotation_services.code_license</code>.
     */
    public void setCodeLicense(String value) {
        set(13, value);
    }

    /**
     * Getter for <code>public.machine_annotation_services.code_license</code>.
     */
    public String getCodeLicense() {
        return (String) get(13);
    }

    /**
     * Setter for <code>public.machine_annotation_services.dependencies</code>.
     */
    public void setDependencies(String[] value) {
        set(14, value);
    }

    /**
     * Getter for <code>public.machine_annotation_services.dependencies</code>.
     */
    public String[] getDependencies() {
        return (String[]) get(14);
    }

    /**
     * Setter for
     * <code>public.machine_annotation_services.support_contact</code>.
     */
    public void setSupportContact(String value) {
        set(15, value);
    }

    /**
     * Getter for
     * <code>public.machine_annotation_services.support_contact</code>.
     */
    public String getSupportContact() {
        return (String) get(15);
    }

    /**
     * Setter for
     * <code>public.machine_annotation_services.sla_documentation</code>.
     */
    public void setSlaDocumentation(String value) {
        set(16, value);
    }

    /**
     * Getter for
     * <code>public.machine_annotation_services.sla_documentation</code>.
     */
    public String getSlaDocumentation() {
        return (String) get(16);
    }

    /**
     * Setter for <code>public.machine_annotation_services.topicname</code>.
     */
    public void setTopicname(String value) {
        set(17, value);
    }

    /**
     * Getter for <code>public.machine_annotation_services.topicname</code>.
     */
    public String getTopicname() {
        return (String) get(17);
    }

    /**
     * Setter for <code>public.machine_annotation_services.maxreplicas</code>.
     */
    public void setMaxreplicas(Integer value) {
        set(18, value);
    }

    /**
     * Getter for <code>public.machine_annotation_services.maxreplicas</code>.
     */
    public Integer getMaxreplicas() {
        return (Integer) get(18);
    }

    /**
     * Setter for <code>public.machine_annotation_services.deleted_on</code>.
     */
    public void setDeletedOn(Instant value) {
        set(19, value);
    }

    /**
     * Getter for <code>public.machine_annotation_services.deleted_on</code>.
     */
    public Instant getDeletedOn() {
        return (Instant) get(19);
    }

    /**
     * Setter for
     * <code>public.machine_annotation_services.batching_permitted</code>.
     */
    public void setBatchingPermitted(Boolean value) {
        set(20, value);
    }

    /**
     * Getter for
     * <code>public.machine_annotation_services.batching_permitted</code>.
     */
    public Boolean getBatchingPermitted() {
        return (Boolean) get(20);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<String> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record21 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row21<String, Integer, String, Instant, String, String, String, JSONB, String, String, String, String, String, String, String[], String, String, String, Integer, Instant, Boolean> fieldsRow() {
        return (Row21) super.fieldsRow();
    }

    @Override
    public Row21<String, Integer, String, Instant, String, String, String, JSONB, String, String, String, String, String, String, String[], String, String, String, Integer, Instant, Boolean> valuesRow() {
        return (Row21) super.valuesRow();
    }

    @Override
    public Field<String> field1() {
        return MachineAnnotationServices.MACHINE_ANNOTATION_SERVICES.ID;
    }

    @Override
    public Field<Integer> field2() {
        return MachineAnnotationServices.MACHINE_ANNOTATION_SERVICES.VERSION;
    }

    @Override
    public Field<String> field3() {
        return MachineAnnotationServices.MACHINE_ANNOTATION_SERVICES.NAME;
    }

    @Override
    public Field<Instant> field4() {
        return MachineAnnotationServices.MACHINE_ANNOTATION_SERVICES.CREATED;
    }

    @Override
    public Field<String> field5() {
        return MachineAnnotationServices.MACHINE_ANNOTATION_SERVICES.ADMINISTRATOR;
    }

    @Override
    public Field<String> field6() {
        return MachineAnnotationServices.MACHINE_ANNOTATION_SERVICES.CONTAINER_IMAGE;
    }

    @Override
    public Field<String> field7() {
        return MachineAnnotationServices.MACHINE_ANNOTATION_SERVICES.CONTAINER_IMAGE_TAG;
    }

    @Override
    public Field<JSONB> field8() {
        return MachineAnnotationServices.MACHINE_ANNOTATION_SERVICES.TARGET_DIGITAL_OBJECT_FILTERS;
    }

    @Override
    public Field<String> field9() {
        return MachineAnnotationServices.MACHINE_ANNOTATION_SERVICES.SERVICE_DESCRIPTION;
    }

    @Override
    public Field<String> field10() {
        return MachineAnnotationServices.MACHINE_ANNOTATION_SERVICES.SERVICE_STATE;
    }

    @Override
    public Field<String> field11() {
        return MachineAnnotationServices.MACHINE_ANNOTATION_SERVICES.SOURCE_CODE_REPOSITORY;
    }

    @Override
    public Field<String> field12() {
        return MachineAnnotationServices.MACHINE_ANNOTATION_SERVICES.SERVICE_AVAILABILITY;
    }

    @Override
    public Field<String> field13() {
        return MachineAnnotationServices.MACHINE_ANNOTATION_SERVICES.CODE_MAINTAINER;
    }

    @Override
    public Field<String> field14() {
        return MachineAnnotationServices.MACHINE_ANNOTATION_SERVICES.CODE_LICENSE;
    }

    @Override
    public Field<String[]> field15() {
        return MachineAnnotationServices.MACHINE_ANNOTATION_SERVICES.DEPENDENCIES;
    }

    @Override
    public Field<String> field16() {
        return MachineAnnotationServices.MACHINE_ANNOTATION_SERVICES.SUPPORT_CONTACT;
    }

    @Override
    public Field<String> field17() {
        return MachineAnnotationServices.MACHINE_ANNOTATION_SERVICES.SLA_DOCUMENTATION;
    }

    @Override
    public Field<String> field18() {
        return MachineAnnotationServices.MACHINE_ANNOTATION_SERVICES.TOPICNAME;
    }

    @Override
    public Field<Integer> field19() {
        return MachineAnnotationServices.MACHINE_ANNOTATION_SERVICES.MAXREPLICAS;
    }

    @Override
    public Field<Instant> field20() {
        return MachineAnnotationServices.MACHINE_ANNOTATION_SERVICES.DELETED_ON;
    }

    @Override
    public Field<Boolean> field21() {
        return MachineAnnotationServices.MACHINE_ANNOTATION_SERVICES.BATCHING_PERMITTED;
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
        return getName();
    }

    @Override
    public Instant component4() {
        return getCreated();
    }

    @Override
    public String component5() {
        return getAdministrator();
    }

    @Override
    public String component6() {
        return getContainerImage();
    }

    @Override
    public String component7() {
        return getContainerImageTag();
    }

    @Override
    public JSONB component8() {
        return getTargetDigitalObjectFilters();
    }

    @Override
    public String component9() {
        return getServiceDescription();
    }

    @Override
    public String component10() {
        return getServiceState();
    }

    @Override
    public String component11() {
        return getSourceCodeRepository();
    }

    @Override
    public String component12() {
        return getServiceAvailability();
    }

    @Override
    public String component13() {
        return getCodeMaintainer();
    }

    @Override
    public String component14() {
        return getCodeLicense();
    }

    @Override
    public String[] component15() {
        return getDependencies();
    }

    @Override
    public String component16() {
        return getSupportContact();
    }

    @Override
    public String component17() {
        return getSlaDocumentation();
    }

    @Override
    public String component18() {
        return getTopicname();
    }

    @Override
    public Integer component19() {
        return getMaxreplicas();
    }

    @Override
    public Instant component20() {
        return getDeletedOn();
    }

    @Override
    public Boolean component21() {
        return getBatchingPermitted();
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
        return getName();
    }

    @Override
    public Instant value4() {
        return getCreated();
    }

    @Override
    public String value5() {
        return getAdministrator();
    }

    @Override
    public String value6() {
        return getContainerImage();
    }

    @Override
    public String value7() {
        return getContainerImageTag();
    }

    @Override
    public JSONB value8() {
        return getTargetDigitalObjectFilters();
    }

    @Override
    public String value9() {
        return getServiceDescription();
    }

    @Override
    public String value10() {
        return getServiceState();
    }

    @Override
    public String value11() {
        return getSourceCodeRepository();
    }

    @Override
    public String value12() {
        return getServiceAvailability();
    }

    @Override
    public String value13() {
        return getCodeMaintainer();
    }

    @Override
    public String value14() {
        return getCodeLicense();
    }

    @Override
    public String[] value15() {
        return getDependencies();
    }

    @Override
    public String value16() {
        return getSupportContact();
    }

    @Override
    public String value17() {
        return getSlaDocumentation();
    }

    @Override
    public String value18() {
        return getTopicname();
    }

    @Override
    public Integer value19() {
        return getMaxreplicas();
    }

    @Override
    public Instant value20() {
        return getDeletedOn();
    }

    @Override
    public Boolean value21() {
        return getBatchingPermitted();
    }

    @Override
    public MachineAnnotationServicesRecord value1(String value) {
        setId(value);
        return this;
    }

    @Override
    public MachineAnnotationServicesRecord value2(Integer value) {
        setVersion(value);
        return this;
    }

    @Override
    public MachineAnnotationServicesRecord value3(String value) {
        setName(value);
        return this;
    }

    @Override
    public MachineAnnotationServicesRecord value4(Instant value) {
        setCreated(value);
        return this;
    }

    @Override
    public MachineAnnotationServicesRecord value5(String value) {
        setAdministrator(value);
        return this;
    }

    @Override
    public MachineAnnotationServicesRecord value6(String value) {
        setContainerImage(value);
        return this;
    }

    @Override
    public MachineAnnotationServicesRecord value7(String value) {
        setContainerImageTag(value);
        return this;
    }

    @Override
    public MachineAnnotationServicesRecord value8(JSONB value) {
        setTargetDigitalObjectFilters(value);
        return this;
    }

    @Override
    public MachineAnnotationServicesRecord value9(String value) {
        setServiceDescription(value);
        return this;
    }

    @Override
    public MachineAnnotationServicesRecord value10(String value) {
        setServiceState(value);
        return this;
    }

    @Override
    public MachineAnnotationServicesRecord value11(String value) {
        setSourceCodeRepository(value);
        return this;
    }

    @Override
    public MachineAnnotationServicesRecord value12(String value) {
        setServiceAvailability(value);
        return this;
    }

    @Override
    public MachineAnnotationServicesRecord value13(String value) {
        setCodeMaintainer(value);
        return this;
    }

    @Override
    public MachineAnnotationServicesRecord value14(String value) {
        setCodeLicense(value);
        return this;
    }

    @Override
    public MachineAnnotationServicesRecord value15(String[] value) {
        setDependencies(value);
        return this;
    }

    @Override
    public MachineAnnotationServicesRecord value16(String value) {
        setSupportContact(value);
        return this;
    }

    @Override
    public MachineAnnotationServicesRecord value17(String value) {
        setSlaDocumentation(value);
        return this;
    }

    @Override
    public MachineAnnotationServicesRecord value18(String value) {
        setTopicname(value);
        return this;
    }

    @Override
    public MachineAnnotationServicesRecord value19(Integer value) {
        setMaxreplicas(value);
        return this;
    }

    @Override
    public MachineAnnotationServicesRecord value20(Instant value) {
        setDeletedOn(value);
        return this;
    }

    @Override
    public MachineAnnotationServicesRecord value21(Boolean value) {
        setBatchingPermitted(value);
        return this;
    }

    @Override
    public MachineAnnotationServicesRecord values(String value1, Integer value2, String value3, Instant value4, String value5, String value6, String value7, JSONB value8, String value9, String value10, String value11, String value12, String value13, String value14, String[] value15, String value16, String value17, String value18, Integer value19, Instant value20, Boolean value21) {
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
        value16(value16);
        value17(value17);
        value18(value18);
        value19(value19);
        value20(value20);
        value21(value21);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached MachineAnnotationServicesRecord
     */
    public MachineAnnotationServicesRecord() {
        super(MachineAnnotationServices.MACHINE_ANNOTATION_SERVICES);
    }

    /**
     * Create a detached, initialised MachineAnnotationServicesRecord
     */
    public MachineAnnotationServicesRecord(String id, Integer version, String name, Instant created, String administrator, String containerImage, String containerImageTag, JSONB targetDigitalObjectFilters, String serviceDescription, String serviceState, String sourceCodeRepository, String serviceAvailability, String codeMaintainer, String codeLicense, String[] dependencies, String supportContact, String slaDocumentation, String topicname, Integer maxreplicas, Instant deletedOn, Boolean batchingPermitted) {
        super(MachineAnnotationServices.MACHINE_ANNOTATION_SERVICES);

        setId(id);
        setVersion(version);
        setName(name);
        setCreated(created);
        setAdministrator(administrator);
        setContainerImage(containerImage);
        setContainerImageTag(containerImageTag);
        setTargetDigitalObjectFilters(targetDigitalObjectFilters);
        setServiceDescription(serviceDescription);
        setServiceState(serviceState);
        setSourceCodeRepository(sourceCodeRepository);
        setServiceAvailability(serviceAvailability);
        setCodeMaintainer(codeMaintainer);
        setCodeLicense(codeLicense);
        setDependencies(dependencies);
        setSupportContact(supportContact);
        setSlaDocumentation(slaDocumentation);
        setTopicname(topicname);
        setMaxreplicas(maxreplicas);
        setDeletedOn(deletedOn);
        setBatchingPermitted(batchingPermitted);
        resetChangedOnNotNull();
    }
}
