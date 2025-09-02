-- ** Database generated with pgModeler (PostgreSQL Database Modeler).
-- ** pgModeler version: 1.2.0-beta1
-- ** PostgreSQL version: 17.0
-- ** Project Site: pgmodeler.io
-- ** Model Author: Infoyupay SACS - David Vidal

-- ** Database creation must be performed outside a multi lined SQL file. 
-- ** These commands were put in this file only as a convenience.

-- object: gangcomision_db | type: DATABASE --
-- DROP DATABASE IF EXISTS gangcomision_db;
CREATE DATABASE gangcomision_db;
-- ddl-end --


SET check_function_bodies = false;
-- ddl-end --

SET search_path TO pg_catalog,public;
-- ddl-end --

-- object: public.sq_user_id | type: SEQUENCE --
-- DROP SEQUENCE IF EXISTS public.sq_user_id CASCADE;
CREATE SEQUENCE public.sq_user_id
	INCREMENT BY 1
	MINVALUE 0
	MAXVALUE 9223372036854775807
	START WITH 1
	CACHE 1
	NO CYCLE
	OWNED BY NONE;

-- ddl-end --

-- object: public.user_role | type: TYPE --
-- DROP TYPE IF EXISTS public.user_role CASCADE;
CREATE TYPE public.user_role AS
ENUM ('ROOT','ADMIN','CASHIER');
-- ddl-end --

-- object: public."user" | type: TABLE --
-- DROP TABLE IF EXISTS public."user" CASCADE;
CREATE TABLE public."user" (
	id bigint NOT NULL DEFAULT nextval('public.sq_user_id'::regclass),
	username varchar NOT NULL,
	password_hash varchar NOT NULL,
	password_salt varchar NOT NULL,
	role public.user_role NOT NULL,
	active boolean NOT NULL,
	CONSTRAINT uq_user_username UNIQUE (username),
	CONSTRAINT user_pk PRIMARY KEY (id),
	CONSTRAINT chk_user_username_nonempty CHECK (char_length(username) > 0),
	CONSTRAINT chk_user_passwordhash_nonempty CHECK (char_length(password_hash) > 0),
	CONSTRAINT chk_user_passwordsalt_nonempty CHECK (char_length((password_salt)::text) > 0)
);
-- ddl-end --

-- object: public.sq_bank_id | type: SEQUENCE --
-- DROP SEQUENCE IF EXISTS public.sq_bank_id CASCADE;
CREATE SEQUENCE public.sq_bank_id
	INCREMENT BY 1
	MINVALUE 0
	MAXVALUE 2147483647
	START WITH 1
	CACHE 1
	NO CYCLE
	OWNED BY NONE;

-- ddl-end --

-- object: public.bank | type: TABLE --
-- DROP TABLE IF EXISTS public.bank CASCADE;
CREATE TABLE public.bank (
	id integer NOT NULL DEFAULT nextval('public.sq_bank_id'::regclass),
	name varchar NOT NULL,
	active boolean NOT NULL,
	CONSTRAINT bank_pk PRIMARY KEY (id),
	CONSTRAINT chk_bank_name_nonempty CHECK (char_length(name) > 0)
);
-- ddl-end --

-- object: public.sq_concept_id | type: SEQUENCE --
-- DROP SEQUENCE IF EXISTS public.sq_concept_id CASCADE;
CREATE SEQUENCE public.sq_concept_id
	INCREMENT BY 1
	MINVALUE 0
	MAXVALUE 9223372036854775807
	START WITH 1
	CACHE 1
	NO CYCLE
	OWNED BY NONE;

-- ddl-end --

-- object: public.concept_type | type: TYPE --
-- DROP TYPE IF EXISTS public.concept_type CASCADE;
CREATE TYPE public.concept_type AS
ENUM ('FIXED','RATE');
-- ddl-end --

-- object: public.concept | type: TABLE --
-- DROP TABLE IF EXISTS public.concept CASCADE;
CREATE TABLE public.concept (
	id bigint NOT NULL DEFAULT nextval('public.sq_concept_id'::regclass),
	name varchar NOT NULL,
	type public.concept_type NOT NULL,
	value decimal(6,4) NOT NULL,
	active boolean NOT NULL,
	CONSTRAINT concept_pk PRIMARY KEY (id),
	CONSTRAINT chk_concept_name_nonempty CHECK (char_length(name) > 0),
	CONSTRAINT chk_concept_value_valid CHECK ((type = 'FIXED' AND value >= 0) OR (type = 'RATE' AND value BETWEEN 0 AND 1))
);
-- ddl-end --

-- object: public.sq_transaction_id | type: SEQUENCE --
-- DROP SEQUENCE IF EXISTS public.sq_transaction_id CASCADE;
CREATE SEQUENCE public.sq_transaction_id
	INCREMENT BY 1
	MINVALUE 0
	MAXVALUE 9223372036854775807
	START WITH 1
	CACHE 1
	NO CYCLE
	OWNED BY NONE;

-- ddl-end --

-- object: public.transaction_status | type: TYPE --
-- DROP TYPE IF EXISTS public.transaction_status CASCADE;
CREATE TYPE public.transaction_status AS
ENUM ('REGISTERED','REVERSION_REQUESTED','REVERSED');
-- ddl-end --

-- object: public.transaction | type: TABLE --
-- DROP TABLE IF EXISTS public.transaction CASCADE;
CREATE TABLE public.transaction (
	id bigint NOT NULL DEFAULT nextval('public.sq_transaction_id'::regclass),
	bank integer NOT NULL,
	concept bigint NOT NULL,
	cashier bigint NOT NULL,
	amount decimal(14,2) NOT NULL,
	commission decimal(14,2) NOT NULL,
	moment timestamptz(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	status public.transaction_status NOT NULL,
	CONSTRAINT transaction_pk PRIMARY KEY (id),
	CONSTRAINT chk_transaction_amount_positive CHECK (amount > 0),
	CONSTRAINT chk_transaction_commission_nonnegative CHECK (commission >= 0)
);
-- ddl-end --

-- object: user_fk | type: CONSTRAINT --
-- ALTER TABLE public.transaction DROP CONSTRAINT IF EXISTS user_fk CASCADE;
ALTER TABLE public.transaction ADD CONSTRAINT user_fk FOREIGN KEY (cashier)
REFERENCES public."user" (id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: bank_fk | type: CONSTRAINT --
-- ALTER TABLE public.transaction DROP CONSTRAINT IF EXISTS bank_fk CASCADE;
ALTER TABLE public.transaction ADD CONSTRAINT bank_fk FOREIGN KEY (bank)
REFERENCES public.bank (id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: concept_fk | type: CONSTRAINT --
-- ALTER TABLE public.transaction DROP CONSTRAINT IF EXISTS concept_fk CASCADE;
ALTER TABLE public.transaction ADD CONSTRAINT concept_fk FOREIGN KEY (concept)
REFERENCES public.concept (id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: public.reversal_request_status | type: TYPE --
-- DROP TYPE IF EXISTS public.reversal_request_status CASCADE;
CREATE TYPE public.reversal_request_status AS
ENUM ('PENDING','APPROVED','REJECTED');
-- ddl-end --

-- object: public.sq_reversal_id | type: SEQUENCE --
-- DROP SEQUENCE IF EXISTS public.sq_reversal_id CASCADE;
CREATE SEQUENCE public.sq_reversal_id
	INCREMENT BY 1
	MINVALUE 0
	MAXVALUE 9223372036854775807
	START WITH 1
	CACHE 1
	NO CYCLE
	OWNED BY NONE;

-- ddl-end --

-- object: public.reversal_request | type: TABLE --
-- DROP TABLE IF EXISTS public.reversal_request CASCADE;
CREATE TABLE public.reversal_request (
	id bigint NOT NULL DEFAULT nextval('public.sq_reversal_id'::regclass),
	transaction bigint NOT NULL,
	message text NOT NULL,
	request_stamp timestamptz(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	answer text,
	requested_by bigint NOT NULL,
	answer_stamp timestamptz,
	status public.reversal_request_status NOT NULL,
	evaluated_by bigint,
	CONSTRAINT reversal_request_pk PRIMARY KEY (id),
	CONSTRAINT chk_reversal_message_nonempty CHECK (char_length(message) > 0)
);
-- ddl-end --

-- object: user_fk | type: CONSTRAINT --
-- ALTER TABLE public.reversal_request DROP CONSTRAINT IF EXISTS user_fk CASCADE;
ALTER TABLE public.reversal_request ADD CONSTRAINT user_fk FOREIGN KEY (requested_by)
REFERENCES public."user" (id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: user_fk1 | type: CONSTRAINT --
-- ALTER TABLE public.reversal_request DROP CONSTRAINT IF EXISTS user_fk1 CASCADE;
ALTER TABLE public.reversal_request ADD CONSTRAINT user_fk1 FOREIGN KEY (evaluated_by)
REFERENCES public."user" (id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: transaction_fk | type: CONSTRAINT --
-- ALTER TABLE public.reversal_request DROP CONSTRAINT IF EXISTS transaction_fk CASCADE;
ALTER TABLE public.reversal_request ADD CONSTRAINT transaction_fk FOREIGN KEY (transaction)
REFERENCES public.transaction (id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: reversal_request_uq | type: CONSTRAINT --
-- ALTER TABLE public.reversal_request DROP CONSTRAINT IF EXISTS reversal_request_uq CASCADE;
ALTER TABLE public.reversal_request ADD CONSTRAINT reversal_request_uq UNIQUE (transaction);
-- ddl-end --

-- object: public.sq_audit_log_id | type: SEQUENCE --
-- DROP SEQUENCE IF EXISTS public.sq_audit_log_id CASCADE;
CREATE SEQUENCE public.sq_audit_log_id
	INCREMENT BY 1
	MINVALUE 0
	MAXVALUE 9223372036854775807
	START WITH 1
	CACHE 1
	NO CYCLE
	OWNED BY NONE;

-- ddl-end --

-- object: public.audit_log | type: TABLE --
-- DROP TABLE IF EXISTS public.audit_log CASCADE;
CREATE TABLE public.audit_log (
	id bigint NOT NULL DEFAULT nextval('public.sq_audit_log_id'::regclass),
	event_stamp timestamptz(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	user_id bigint NOT NULL,
	action varchar NOT NULL,
	entity varchar,
	entity_id bigint,
	details text,
	computer_name varchar NOT NULL,
	CONSTRAINT audit_log_pk PRIMARY KEY (id),
	CONSTRAINT chk_audit_action_nonempty CHECK (char_length(action) > 0),
	CONSTRAINT chk_audit_computername_nonempty CHECK (char_length(computer_name) > 0)
);
-- ddl-end --

-- object: user_fk | type: CONSTRAINT --
-- ALTER TABLE public.audit_log DROP CONSTRAINT IF EXISTS user_fk CASCADE;
ALTER TABLE public.audit_log ADD CONSTRAINT user_fk FOREIGN KEY (user_id)
REFERENCES public."user" (id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: idx_transaction_bank | type: INDEX --
-- DROP INDEX IF EXISTS public.idx_transaction_bank CASCADE;
CREATE INDEX idx_transaction_bank ON public.transaction
USING btree
(
	bank
);
-- ddl-end --

-- object: idx_transaction_date | type: INDEX --
-- DROP INDEX IF EXISTS public.idx_transaction_date CASCADE;
CREATE INDEX idx_transaction_date ON public.transaction
USING btree
(
	moment ASC NULLS LAST
);
-- ddl-end --

-- object: idx_transaction_cashier | type: INDEX --
-- DROP INDEX IF EXISTS public.idx_transaction_cashier CASCADE;
CREATE INDEX idx_transaction_cashier ON public.transaction
USING btree
(
	cashier
);
-- ddl-end --

-- object: idx_transaction_bank_date | type: INDEX --
-- DROP INDEX IF EXISTS public.idx_transaction_bank_date CASCADE;
CREATE INDEX idx_transaction_bank_date ON public.transaction
USING btree
(
	bank ASC NULLS LAST,
	moment DESC NULLS LAST
);
-- ddl-end --

-- object: idx_audit_user | type: INDEX --
-- DROP INDEX IF EXISTS public.idx_audit_user CASCADE;
CREATE INDEX idx_audit_user ON public.audit_log
USING btree
(
	user_id
);
-- ddl-end --

-- object: idx_audit_event_stamp | type: INDEX --
-- DROP INDEX IF EXISTS public.idx_audit_event_stamp CASCADE;
CREATE INDEX idx_audit_event_stamp ON public.audit_log
USING btree
(
	event_stamp DESC NULLS LAST
);
-- ddl-end --

-- object: idx_audit_user_date | type: INDEX --
-- DROP INDEX IF EXISTS public.idx_audit_user_date CASCADE;
CREATE INDEX idx_audit_user_date ON public.audit_log
USING btree
(
	user_id ASC NULLS LAST,
	event_stamp DESC NULLS LAST
);
-- ddl-end --

-- object: public.global_config | type: TABLE --
-- DROP TABLE IF EXISTS public.global_config CASCADE;
CREATE TABLE public.global_config (
	id smallint NOT NULL DEFAULT 1,
	ruc varchar(11) NOT NULL,
	legal_name varchar(255) NOT NULL,
	business_name varchar(100) NOT NULL,
	address text NOT NULL,
	announcement varchar(80),
	updated_at timestamptz(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	updated_by bigint NOT NULL,
	updated_from varchar(100) NOT NULL,
	CONSTRAINT global_config_pk PRIMARY KEY (id),
	CONSTRAINT chk_id_one CHECK (id = 1),
	CONSTRAINT chk_ruc_digits CHECK (ruc ~ '^[0-9]{11}$'),
	CONSTRAINT chk_legal_name_nonempty CHECK (char_length(legal_name) > 0),
	CONSTRAINT chk_business_name_nonempty CHECK (char_length(business_name) > 0),
	CONSTRAINT chk_address_nonempty CHECK (char_length(address) > 0),
	CONSTRAINT chk_announcement_no_newline CHECK (announcement !~ '[\r\n]')
);
-- ddl-end --

-- object: user_fk | type: CONSTRAINT --
-- ALTER TABLE public.global_config DROP CONSTRAINT IF EXISTS user_fk CASCADE;
ALTER TABLE public.global_config ADD CONSTRAINT user_fk FOREIGN KEY (updated_by)
REFERENCES public."user" (id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: global_config_uq | type: CONSTRAINT --
-- ALTER TABLE public.global_config DROP CONSTRAINT IF EXISTS global_config_uq CASCADE;
ALTER TABLE public.global_config ADD CONSTRAINT global_config_uq UNIQUE (updated_by);
-- ddl-end --

-- object: public.update_global_config_timestamp | type: FUNCTION --
-- DROP FUNCTION IF EXISTS public.update_global_config_timestamp() CASCADE;
CREATE OR REPLACE FUNCTION public.update_global_config_timestamp ()
	RETURNS trigger
	LANGUAGE plpgsql
	VOLATILE 
	CALLED ON NULL INPUT
	SECURITY INVOKER
	PARALLEL UNSAFE
	COST 1
	AS 
$function$
BEGIN
  NEW.updated_at := CURRENT_TIMESTAMP;
  RETURN NEW;
END;

$function$;
-- ddl-end --

-- object: trg_global_config_updated | type: TRIGGER --
-- DROP TRIGGER IF EXISTS trg_global_config_updated ON public.global_config CASCADE;
CREATE OR REPLACE TRIGGER trg_global_config_updated
	BEFORE UPDATE
	ON public.global_config
	FOR EACH ROW
	EXECUTE PROCEDURE public.update_global_config_timestamp();
-- ddl-end --


