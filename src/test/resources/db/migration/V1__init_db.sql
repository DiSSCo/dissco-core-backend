CREATE TABLE public.organisation_do (
	id text NOT NULL,
	organisation_name text NULL,
	organisation_code text NULL,
	city text NULL,
	country text NULL,
	country_code text NULL,
	"data" jsonb NULL,
	CONSTRAINT organisation_do_pkey PRIMARY KEY (id)
);

CREATE TYPE public."curatedobjectidtypes" AS ENUM ('physicalSpecimenID', 'CETAFID');

CREATE TABLE public.digital_specimen (
	id text NOT NULL,
	object_type varchar(20) NOT NULL,
	curated_object_id text NOT NULL,
	mids_level int2 NOT NULL,
	curated_object_id_type public."curatedobjectidtypes" NULL,
	specimen_name text NULL,
	institution_id text NULL,
	institution_name text NULL,
	"data" jsonb NOT NULL,
	CONSTRAINT digital_specimen_pkey PRIMARY KEY (id)
);

CREATE INDEX physical_id_index ON public.digital_specimen USING btree (curated_object_id, object_type);

CREATE TABLE public.organisation_document (
	organisation_id text NULL,
	document_id text NOT NULL,
	document_title text NULL,
	document_type text NULL,
	"document" jsonb NULL,
	CONSTRAINT organisation_document_pkey PRIMARY KEY (document_id),
	CONSTRAINT fk_organisation FOREIGN KEY (organisation_id) REFERENCES public.organisation_do(id)
);

CREATE TABLE public.annotation (
	id text NOT NULL,
	"type" text NOT NULL,
	body jsonb NULL,
	target text NOT NULL,
	last_updated timestamptz NOT NULL,
	creator text NULL,
	created timestamptz NOT NULL,
	CONSTRAINT annotation_pkey PRIMARY KEY (id)
);
CREATE INDEX annotation_creator_idx ON public.annotation USING btree (creator);