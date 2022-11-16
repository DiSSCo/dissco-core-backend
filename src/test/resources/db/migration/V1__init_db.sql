CREATE TABLE public.new_user (
	id text NOT NULL,
	first_name text NULL,
	last_name text NULL,
	email text NULL,
	orcid text NULL,
	organization text NULL,
	created timestamptz NOT NULL,
	updated timestamptz NOT NULL,
	CONSTRAINT new_user_orcid_key UNIQUE (orcid),
	CONSTRAINT new_user_pkey PRIMARY KEY (id)
);
