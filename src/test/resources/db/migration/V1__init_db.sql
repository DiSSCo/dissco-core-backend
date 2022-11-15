CREATE TABLE public.new_user (
	id text NOT NULL,
	first_name text NULL,
	last_name text NULL,
	email text NULL,
	orchid text NULL,
	organization text NULL,
	created timestamptz NOT NULL,
	updated timestamptz NOT NULL,
	CONSTRAINT new_user_orchid_key UNIQUE (orchid),
	CONSTRAINT new_user_pkey PRIMARY KEY (id)
);