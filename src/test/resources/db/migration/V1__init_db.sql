CREATE TABLE public.user
(
    id           text        NOT NULL,
    first_name   text        NULL,
    last_name    text        NULL,
    email        text        NULL,
    orcid        text        NULL,
    organization text        NULL,
    created      timestamptz NOT NULL,
    updated      timestamptz NOT NULL,
    CONSTRAINT user_pkey PRIMARY KEY (id)
);

create table annotation
(
    id               text                     not null
        constraint annotation_pk
            primary key,
    version          integer                  not null,
    type             text                     not null,
    motivation       text                     not null,
    motivated_by     text,
    target_id        text                     not null,
    target           jsonb                    not null,
    body             jsonb                    not null,
    creator_id       text                     not null,
    creator          jsonb                    not null,
    created          timestamp with time zone not null,
    generator        jsonb                    not null,
    generated        timestamp with time zone not null,
    last_checked     timestamp with time zone not null,
    aggregate_rating jsonb,
    deleted_on       timestamp with time zone,
    annotation_hash  uuid,
    mjr_job_id       text,
    batch_id         uuid
);

create table digital_media_object
(
    id                  text                     not null
        constraint digital_media_object_pk
            primary key,
    version             integer                  not null,
    type                text,
    digital_specimen_id text                     not null,
    media_url           text                     not null,
    created             timestamp with time zone not null,
    last_checked        timestamp with time zone not null,
    deleted             timestamp with time zone,
    data                jsonb                    not null,
    original_data       jsonb                    not null
);

create index digital_media_object_id_idx
    on digital_media_object (id, media_url);

create unique index digital_media_object_id_version_url
    on digital_media_object (id, version, media_url);

create index digital_media_object_digital_specimen_id_url
    on digital_media_object (digital_specimen_id, media_url);


create table digital_specimen
(
    id                     text                     not null
        constraint digital_specimen_pk
            primary key,
    version                integer                  not null,
    type                   text                     not null,
    midslevel              smallint                 not null,
    physical_specimen_id   text                     not null,
    physical_specimen_type text                     not null,
    specimen_name          text,
    organization_id        text                     not null,
    source_system_id       text                     not null,
    created                timestamp with time zone not null,
    last_checked           timestamp with time zone not null,
    deleted                timestamp with time zone,
    data                   jsonb,
    original_data          jsonb
);
create index digital_specimen_created_idx
    on digital_specimen (created);

create index digital_specimen_physical_specimen_id_idx
    on digital_specimen (physical_specimen_id);

CREATE TABLE machine_annotation_services
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
    deleted_on                    timestamp with time zone,
    batching_permitted            boolean                  not null,
    time_to_live                  integer default 86400    not null
);

create type job_state as enum ('SCHEDULED', 'RUNNING', 'FAILED', 'COMPLETED');

create type mjr_target_type as enum ('DIGITAL_SPECIMEN', 'MEDIA_OBJECT');

create type error_code as enum ('TIMEOUT', 'DISSCO_EXCEPTION');

create table mas_job_record
(
    job_id             text                     not null
        constraint mas_job_record_pk
            primary key,
    job_state          job_state                not null,
    mas_id             text                     not null,
    time_started       timestamp with time zone not null,
    time_completed     timestamp with time zone,
    annotations        jsonb,
    target_id          text                     not null,
    user_id            text,
    target_type        mjr_target_type,
    batching_requested boolean,
    error              error_code,
    expires_on         timestamp with time zone not null
);

