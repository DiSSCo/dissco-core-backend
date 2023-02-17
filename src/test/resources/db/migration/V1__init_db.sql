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
	CONSTRAINT new_annotation_pkey PRIMARY KEY (id, version)
);

CREATE TABLE public.new_digital_media_object (
	id text NOT NULL,
	"version" int4 NOT NULL,
	"type" text NULL,
	digital_specimen_id text NOT NULL,
	media_url text NULL,
	format text NULL,
	source_system_id text NOT NULL,
	created timestamptz NOT NULL,
	last_checked timestamptz NOT NULL,
	deleted timestamptz NULL,
	"data" jsonb NULL,
	original_data jsonb NULL,
	CONSTRAINT new_digital_media_object_pkey PRIMARY KEY (id, version)
);

CREATE TABLE public.new_digital_specimen (
	id text NOT NULL,
	"version" int4 NOT NULL,
	"type" text NOT NULL,
	midslevel int2 NOT NULL,
	physical_specimen_id text NOT NULL,
	physical_specimen_type text NOT NULL,
	specimen_name text NULL,
	organization_id text NOT NULL,
	physical_specimen_collection text NULL,
	dataset text NULL,
	source_system_id text NOT NULL,
	created timestamptz NOT NULL,
	last_checked timestamptz NOT NULL,
	deleted timestamptz NULL,
	"data" jsonb NULL,
	original_data jsonb NULL,
	dwca_id text NULL,
	CONSTRAINT new_digital_specimen_pkey PRIMARY KEY (id, version)
);
CREATE INDEX new_digital_specimen_created_idx ON public.new_digital_specimen USING btree (created);
CREATE INDEX new_digital_specimen_id_idx ON public.new_digital_specimen USING btree (id, created);
CREATE INDEX new_digital_specimen_physical_specimen_id_idx ON public.new_digital_specimen USING btree (physical_specimen_id);