CREATE TABLE public.new_user
(
    id           text        NOT NULL,
    first_name   text NULL,
    last_name    text NULL,
    email        text NULL,
    orcid        text NULL,
    organization text NULL,
    created      timestamptz NOT NULL,
    updated      timestamptz NOT NULL,
    CONSTRAINT new_user_pkey PRIMARY KEY (id)
);

CREATE TABLE public.new_annotation
(
    id               text        NOT NULL,
    "version"        int4        NOT NULL,
    "type"           text        NOT NULL,
    motivation       text        NOT NULL,
    target_id        text        NOT NULL,
    target_field     text NULL,
    target_body      jsonb       NOT NULL,
    body             jsonb       NOT NULL,
    preference_score int4        NOT NULL,
    creator          text        NOT NULL,
    created          timestamptz NOT NULL,
    generator_id     text        NOT NULL,
    generator_body   jsonb       NOT NULL,
    "generated"      timestamptz NOT NULL,
    last_checked     timestamptz NOT NULL,
    deleted          timestamptz NULL,
    CONSTRAINT new_annotation_pk PRIMARY KEY (id)
);

create table new_digital_media_object
(
    id                   text                     not null
        constraint new_digital_media_object_pk
            primary key,
    version              integer                  not null,
    type                 text,
    digital_specimen_id  text                     not null,
    media_url            text,
    format               text,
    source_system_id     text                     not null,
    created              timestamp with time zone not null,
    last_checked         timestamp with time zone not null,
    deleted              timestamp with time zone,
    data                 jsonb,
    original_data        jsonb,
    physical_specimen_id varchar default 'unknown':: character varying not null
);

create index new_digital_media_object_id_idx
    on new_digital_media_object (id, media_url);

create unique index new_digital_media_object_id_version_url
    on new_digital_media_object (id, version, media_url);

create index new_digital_media_object_digital_specimen_id_url
    on new_digital_media_object (digital_specimen_id, media_url);

CREATE TABLE public.new_digital_specimen
(
    id                           text        NOT NULL,
    "version"                    int4        NOT NULL,
    "type"                       text        NOT NULL,
    midslevel                    int2        NOT NULL,
    physical_specimen_id         text        NOT NULL,
    physical_specimen_type       text        NOT NULL,
    specimen_name                text NULL,
    organization_id              text        NOT NULL,
    physical_specimen_collection text NULL,
    dataset                      text NULL,
    source_system_id             text        NOT NULL,
    created                      timestamptz NOT NULL,
    last_checked                 timestamptz NOT NULL,
    deleted                      timestamptz NULL,
    "data"                       jsonb NULL,
    original_data                jsonb NULL,
    dwca_id                      text NULL,
    CONSTRAINT new_digital_specimen_pk PRIMARY KEY (id)
);
CREATE INDEX new_digital_specimen_created_idx ON public.new_digital_specimen USING btree (created);
CREATE INDEX new_digital_specimen_id_idx ON public.new_digital_specimen USING btree (id, created);
CREATE INDEX new_digital_specimen_physical_specimen_id_idx ON public.new_digital_specimen USING btree (physical_specimen_id);

CREATE TABLE public.machine_annotation_services
(
    id                            text                     not null
        primary key,
    version                       integer                  not null,
    name                          varchar                  not null,
    created                       timestamp with time zone not null,
    administrator                 text                     not null,
    container_image               text                     not null,
    container_image_tag           text                     not null,
    target_digital_object_filters jsonb,
    service_description           text,
    service_state                 text,
    source_code_repository        text,
    service_availability          text,
    code_maintainer               text,
    code_license                  text,
    dependencies                  text[],
    support_contact               text,
    sla_documentation             text,
    topicname                     text,
    maxreplicas                   integer,
    deleted_on                    timestamp with time zone
);

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

create table mas_job_record
(
    job_id         uuid default uuid_generate_v4() not null
        constraint mas_job_record_pk
            primary key,
    state          text                            not null,
    creator_id     text                            not null,
    time_started   timestamp with time zone        not null,
    time_completed timestamp with time zone,
    annotations    jsonb,
    target_id      text
);

create index mas_job_record_created_idx
    on mas_job_record (time_started);

create index mas_job_record_job_id_index
    on mas_job_record (job_id);
