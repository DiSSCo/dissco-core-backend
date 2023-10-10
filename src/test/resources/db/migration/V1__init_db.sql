CREATE TABLE public.new_user (
	id text NOT NULL,
	first_name text NULL,
	last_name text NULL,
	email text NULL,
	orcid text NULL,
	organization text NULL,
	created timestamptz NOT NULL,
	updated timestamptz NOT NULL,
	CONSTRAINT new_user_pkey PRIMARY KEY (id)
);

CREATE TABLE public.new_annotation (
	id text NOT NULL,
	"version" int4 NOT NULL,
	"type" text NOT NULL,
	motivation text NOT NULL,
	target_id text NOT NULL,
	target_field text NULL,
	target_body jsonb NOT NULL,
	body jsonb NOT NULL,
	preference_score int4 NOT NULL,
	creator text NOT NULL,
	created timestamptz NOT NULL,
	generator_id text NOT NULL,
	generator_body jsonb NOT NULL,
	"generated" timestamptz NOT NULL,
	last_checked timestamptz NOT NULL,
	deleted timestamptz NULL,
	CONSTRAINT new_annotation_pk PRIMARY KEY (id)
);

create table digital_media_object
(
    id text not null
        constraint digital_media_object_pk
            primary key,
    version integer not null,
    type text,
    digital_specimen_id text not null,
    media_url text not null,
    created timestamp with time zone not null,
    last_checked timestamp with time zone not null,
    deleted timestamp with time zone,
    data jsonb not null,
    original_data jsonb not null
);

create index digital_media_object_id_idx
    on digital_media_object (id, media_url);

create unique index digital_media_object_id_version_url
    on digital_media_object (id, version, media_url);

create index digital_media_object_digital_specimen_id_url
    on digital_media_object (digital_specimen_id, media_url);


create table digital_specimen
(
    id text not null
        constraint digital_specimen_pk
            primary key,
    version integer not null,
    type text not null,
    midslevel smallint not null,
    physical_specimen_id text not null,
    physical_specimen_type text not null,
    specimen_name text,
    organization_id text not null,
    source_system_id text not null,
    created timestamp with time zone not null,
    last_checked timestamp with time zone not null,
    deleted timestamp with time zone,
    data jsonb,
    original_data jsonb
);
create index digital_specimen_created_idx
    on digital_specimen (created);

create index digital_specimen_physical_specimen_id_idx
    on digital_specimen (physical_specimen_id);

CREATE TABLE machine_annotation_services
(
    id text not null
        primary key,
    version integer not null,
    name varchar not null,
    created timestamp with time zone not null,
    administrator text not null,
    container_image text not null,
    container_image_tag text not null,
    target_digital_object_filters jsonb,
    service_description text,
    service_state text,
    source_code_repository text,
    service_availability text,
    code_maintainer text,
    code_license text,
    dependencies text[],
    support_contact text,
    sla_documentation text,
    topicname text,
    maxreplicas integer,
    deleted_on timestamp with time zone
);