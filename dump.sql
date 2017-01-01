--
-- PostgreSQL database dump
--

-- Dumped from database version 9.6.1
-- Dumped by pg_dump version 9.6.1

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: postgres; Type: COMMENT; Schema: -; Owner: postgres
--

COMMENT ON DATABASE postgres IS 'default administrative connection database';


--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


--
-- Name: adminpack; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS adminpack WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION adminpack; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION adminpack IS 'administrative functions for PostgreSQL';


SET search_path = public, pg_catalog;

--
-- Name: canreach(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION canreach(integer, integer) RETURNS boolean
    LANGUAGE sql
    AS $_$
  SELECT createTheFuture($1);
  
  SELECT CASE WHEN EXISTS (
    SELECT * FROM FutureWorldState
    WHERE timestep = $1 AND relation = $2
  )
  THEN TRUE
  ELSE FALSE END
  
$_$;


ALTER FUNCTION public.canreach(integer, integer) OWNER TO postgres;

--
-- Name: createthefuture(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION createthefuture(integer) RETURNS integer
    LANGUAGE sql
    AS $_$
  
  INSERT INTO FutureWorldState (timestep, relation) (SELECT 0, relation FROM CurrentWorldState)
    ON CONFLICT DO NOTHING;

  SELECT 
    (SELECT 
      COUNT(dispatchActionResults(
        PossibleResults.type, PossibleResults.value,
        PossibleResults.person, i
      ))
      FROM (
        SELECT results.value, results.type, p.id AS person
          FROM Actions a
          CROSS JOIN Persons p
          CROSS JOIN Relations rr 
          LEFT JOIN ActionRequirements ar ON 
             ar.action = a.id
             AND ar.relation = rr.id
          JOIN ActionResults results ON results.action = a.id
          WHERE (SELECT dispatchActionRequirements(a.id, p.id, rr.subject, i) = TRUE)
      ) PossibleResults
    )
  FROM generate_series(0, $1) AS i;
  
  SELECT 1;
$_$;


ALTER FUNCTION public.createthefuture(integer) OWNER TO postgres;

--
-- Name: dispatchactionrequirements(integer, integer, integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION dispatchactionrequirements(integer, integer, integer, integer) RETURNS boolean
    LANGUAGE sql
    AS $_$
  SELECT dispatchActionRequirements(
    (SELECT requirementType FROM ActionRequirements WHERE action = $1),
    $1,
    $2,
    $3,
    $4
  )
$_$;


ALTER FUNCTION public.dispatchactionrequirements(integer, integer, integer, integer) OWNER TO postgres;

--
-- Name: dispatchactionrequirements(integer, integer, integer, integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION dispatchactionrequirements(integer, integer, integer, integer, integer) RETURNS boolean
    LANGUAGE sql
    AS $_$
  SELECT
    CASE WHEN $1 = 0 THEN
      (SELECT sameLocation($3, $4, $5))
    ELSE 
      (SELECT (SELECT COUNT(req.relation) FROM ActionRequirements req WHERE req.action=$2)
              = 
              (SELECT COUNT(req.relation) FROM ActionRequirements req 
                      JOIN CurrentWorldState world ON world.relation=req.relation 
                      WHERE req.action=$2)
      )
    END;

$_$;


ALTER FUNCTION public.dispatchactionrequirements(integer, integer, integer, integer, integer) OWNER TO postgres;

--
-- Name: dispatchactionresults(integer, integer, integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION dispatchactionresults(integer, integer, integer, integer) RETURNS void
    LANGUAGE sql
    AS $_$
  SELECT 
    CASE 
      WHEN $1=0 THEN
        (SELECT walk(
          $3, 
          $4, 
          (SELECT x FROM Positions WHERE id = (SELECT positionOfActorAtTimestep($3, $4))),
          (SELECT y FROM Positions WHERE id = (SELECT positionOfActorAtTimestep($3, $4))),
          $2)
        )
      WHEN $1=1 THEN
        (SELECT makeFutureRelation($4, $2))
  END
$_$;


ALTER FUNCTION public.dispatchactionresults(integer, integer, integer, integer) OWNER TO postgres;

--
-- Name: insertnewposition(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insertnewposition(integer, integer) RETURNS integer
    LANGUAGE sql
    AS $_$
  INSERT INTO Entities (SELECT MAX(id) + 1 FROM Entities);
  SELECT insertNewPosition(
    (SELECT MAX(id) FROM Entities),
    $1, 
    $2
  );
$_$;


ALTER FUNCTION public.insertnewposition(integer, integer) OWNER TO postgres;

--
-- Name: insertnewposition(integer, integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insertnewposition(integer, integer, integer) RETURNS integer
    LANGUAGE sql
    AS $_$
  INSERT INTO Positions VALUES($1, $2, $3)
  ON CONFLICT(x,y) DO UPDATE SET x=$2
  RETURNING id;
$_$;


ALTER FUNCTION public.insertnewposition(integer, integer, integer) OWNER TO postgres;

--
-- Name: lalala(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION lalala(integer) RETURNS integer
    LANGUAGE sql
    AS $_$
      (SELECT ($1) );
$_$;


ALTER FUNCTION public.lalala(integer) OWNER TO postgres;

--
-- Name: makefuturerelation(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION makefuturerelation(integer, integer) RETURNS void
    LANGUAGE sql
    AS $_$
  INSERT INTO FutureWorldState(timestep, relation) VALUES($1 + 1, $2) ON CONFLICT DO NOTHING;
$_$;


ALTER FUNCTION public.makefuturerelation(integer, integer) OWNER TO postgres;

--
-- Name: paramarithmetic(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION paramarithmetic(integer) RETURNS integer
    LANGUAGE sql
    AS $_$
  SELECT $1 + 1;
$_$;


ALTER FUNCTION public.paramarithmetic(integer) OWNER TO postgres;

--
-- Name: positionofactorattimestep(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION positionofactorattimestep(integer, integer) RETURNS integer
    LANGUAGE sql
    AS $_$
  SELECT Positions.id FROM Positions JOIN FutureWorldState ON FutureWorldState.timestep = $2
      JOIN Relations ON Relations.id = FutureWorldState.relation AND Relations.object = $1 
                        AND Relations.subject=Positions.id
      JOIN RelationTypes ON RelationTypes.id = Relations.relationType AND RelationTypes.name = 'At'
$_$;


ALTER FUNCTION public.positionofactorattimestep(integer, integer) OWNER TO postgres;

--
-- Name: relationafterwalking(integer, integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION relationafterwalking(integer, integer, integer) RETURNS integer
    LANGUAGE sql
    AS $_$
  SELECT 
    CASE WHEN $3=1 THEN 
        (SELECT insertNewPosition($1 - 1, $2))
       WHEN $3=2 THEN 
        (SELECT insertNewPosition($1, $2 - 1))
       WHEN $3=3 THEN
        (SELECT insertNewPosition($1+1, $2))
       WHEN $3=4 THEN
        (SELECT insertNewPosition($1, $2+1))
       ELSE 
        (SELECT 0)
  END
$_$;


ALTER FUNCTION public.relationafterwalking(integer, integer, integer) OWNER TO postgres;

--
-- Name: relationafterwalking(integer, integer, integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION relationafterwalking(integer, integer, integer, integer) RETURNS integer
    LANGUAGE sql
    AS $_$
  INSERT INTO Relations (id, relationType, object, subject) 
    VALUES( 
      ( SELECT MAX(id) + 1 FROM Relations), 
      ( SELECT id FROM RelationTypes WHERE name = 'At'), 
      $1, 
      (SELECT 
        CASE WHEN $4=1 THEN 
              (SELECT insertNewPosition($2 - 1, $3))
             WHEN $4=2 THEN 
              (SELECT insertNewPosition($2, $3 - 1))
             WHEN $4=3 THEN
              (SELECT insertNewPosition($2+1, $3))
             WHEN $4=4 THEN
              (SELECT insertNewPosition($2, $3+1))
             ELSE 
              (SELECT -99)
       END
      )
    ) 
    ON CONFLICT(relationType, object, subject) DO UPDATE SET object = $1
    RETURNING id  
$_$;


ALTER FUNCTION public.relationafterwalking(integer, integer, integer, integer) OWNER TO postgres;

--
-- Name: relationidof(integer, integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION relationidof(integer, integer, integer) RETURNS integer
    LANGUAGE sql
    AS $_$
  SELECT id FROM Relations WHERE relationType = $1 AND object = $2 AND subject = $3;
$_$;


ALTER FUNCTION public.relationidof(integer, integer, integer) OWNER TO postgres;

--
-- Name: returningvoid(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION returningvoid(integer) RETURNS void
    LANGUAGE sql
    AS $$
  UPDATE RELATIONS SET id =id WHERE id = -5;
$$;


ALTER FUNCTION public.returningvoid(integer) OWNER TO postgres;

--
-- Name: samelocation(integer, integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION samelocation(integer, integer, integer) RETURNS boolean
    LANGUAGE sql
    AS $_$
   SELECT (SELECT positionOfActorAtTimestep($1, $3)) = (SELECT positionOfActorAtTimestep($2, $3));
  
$_$;


ALTER FUNCTION public.samelocation(integer, integer, integer) OWNER TO postgres;

--
-- Name: testfunction(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION testfunction(integer, integer) RETURNS integer
    LANGUAGE sql
    AS $_$
INSERT INTO testTable(x, y) VALUES($1, $2)
ON CONFLICT(x, y) DO UPDATE SET x = $1
  RETURNING id;
$_$;


ALTER FUNCTION public.testfunction(integer, integer) OWNER TO postgres;

--
-- Name: walk(integer, integer, integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION walk(integer, integer, integer, integer) RETURNS void
    LANGUAGE sql
    AS $_$
    INSERT INTO FutureWorldState VALUES($1 + 1, relationAfterWalking($2, $3, $4)) 
    ON CONFLICT DO NOTHING;
$_$;


ALTER FUNCTION public.walk(integer, integer, integer, integer) OWNER TO postgres;

--
-- Name: walk(integer, integer, integer, integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION walk(integer, integer, integer, integer, integer) RETURNS void
    LANGUAGE sql
    AS $_$
    INSERT INTO FutureWorldState(timestep, relation) VALUES($2 + 1, relationAfterWalking($1, $3, $4, $5)) 
    ON CONFLICT DO NOTHING;
$_$;


ALTER FUNCTION public.walk(integer, integer, integer, integer, integer) OWNER TO postgres;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: actionrequirements; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE actionrequirements (
    id integer NOT NULL,
    action integer,
    requirementtype integer,
    relation integer
);


ALTER TABLE actionrequirements OWNER TO postgres;

--
-- Name: actionrequirementstypes; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE actionrequirementstypes (
    id integer NOT NULL,
    name text
);


ALTER TABLE actionrequirementstypes OWNER TO postgres;

--
-- Name: actionresults; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE actionresults (
    id integer NOT NULL,
    action integer,
    type integer,
    value integer
);


ALTER TABLE actionresults OWNER TO postgres;

--
-- Name: actionresultstype; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE actionresultstype (
    id integer NOT NULL,
    name text
);


ALTER TABLE actionresultstype OWNER TO postgres;

--
-- Name: actions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE actions (
    id integer NOT NULL,
    name text
);


ALTER TABLE actions OWNER TO postgres;

--
-- Name: commodities; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE commodities (
    id integer NOT NULL,
    name text
);


ALTER TABLE commodities OWNER TO postgres;

--
-- Name: currentworldstate; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE currentworldstate (
    id integer NOT NULL,
    relation integer
);


ALTER TABLE currentworldstate OWNER TO postgres;

--
-- Name: entities; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE entities (
    id integer NOT NULL
);


ALTER TABLE entities OWNER TO postgres;

--
-- Name: futureworldstate; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE futureworldstate (
    id integer NOT NULL,
    timestep integer,
    relation integer
);


ALTER TABLE futureworldstate OWNER TO postgres;

--
-- Name: futureworldstate_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE futureworldstate_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE futureworldstate_id_seq OWNER TO postgres;

--
-- Name: futureworldstate_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE futureworldstate_id_seq OWNED BY futureworldstate.id;


--
-- Name: goals; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE goals (
    id integer NOT NULL,
    person integer,
    relation integer
);


ALTER TABLE goals OWNER TO postgres;

--
-- Name: items; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE items (
    id integer NOT NULL,
    name text
);


ALTER TABLE items OWNER TO postgres;

--
-- Name: locationtrait; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE locationtrait (
    id integer NOT NULL,
    name text
);


ALTER TABLE locationtrait OWNER TO postgres;

--
-- Name: locationtraits; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE locationtraits (
    id integer NOT NULL,
    name text
);


ALTER TABLE locationtraits OWNER TO postgres;

--
-- Name: persons; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE persons (
    id integer NOT NULL,
    name text
);


ALTER TABLE persons OWNER TO postgres;

--
-- Name: positions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE positions (
    id integer NOT NULL,
    x integer,
    y integer
);


ALTER TABLE positions OWNER TO postgres;

--
-- Name: positions_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE positions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE positions_id_seq OWNER TO postgres;

--
-- Name: positions_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE positions_id_seq OWNED BY positions.id;


--
-- Name: relations; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE relations (
    id integer NOT NULL,
    relationtype integer,
    object integer,
    subject integer
);


ALTER TABLE relations OWNER TO postgres;

--
-- Name: relationshiptypes; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE relationshiptypes (
    id integer NOT NULL,
    name text
);


ALTER TABLE relationshiptypes OWNER TO postgres;

--
-- Name: relationtypes; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE relationtypes (
    id integer NOT NULL,
    name text
);


ALTER TABLE relationtypes OWNER TO postgres;

--
-- Name: testtable; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE testtable (
    id integer NOT NULL,
    x integer,
    y integer
);


ALTER TABLE testtable OWNER TO postgres;

--
-- Name: testtable_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE testtable_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE testtable_id_seq OWNER TO postgres;

--
-- Name: testtable_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE testtable_id_seq OWNED BY testtable.id;


--
-- Name: futureworldstate id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY futureworldstate ALTER COLUMN id SET DEFAULT nextval('futureworldstate_id_seq'::regclass);


--
-- Name: positions id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY positions ALTER COLUMN id SET DEFAULT nextval('positions_id_seq'::regclass);


--
-- Name: testtable id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY testtable ALTER COLUMN id SET DEFAULT nextval('testtable_id_seq'::regclass);


--
-- Name: actionrequirements actionrequirements_action_relation_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY actionrequirements
    ADD CONSTRAINT actionrequirements_action_relation_key UNIQUE (action, relation);


--
-- Name: actionrequirements actionrequirements_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY actionrequirements
    ADD CONSTRAINT actionrequirements_pkey PRIMARY KEY (id);


--
-- Name: actionrequirementstypes actionrequirementstypes_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY actionrequirementstypes
    ADD CONSTRAINT actionrequirementstypes_pkey PRIMARY KEY (id);


--
-- Name: actionresults actionresults_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY actionresults
    ADD CONSTRAINT actionresults_pkey PRIMARY KEY (id);


--
-- Name: actionresultstype actionresultstype_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY actionresultstype
    ADD CONSTRAINT actionresultstype_pkey PRIMARY KEY (id);


--
-- Name: actions actions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY actions
    ADD CONSTRAINT actions_pkey PRIMARY KEY (id);


--
-- Name: commodities commodities_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY commodities
    ADD CONSTRAINT commodities_pkey PRIMARY KEY (id);


--
-- Name: currentworldstate currentworldstate_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY currentworldstate
    ADD CONSTRAINT currentworldstate_pkey PRIMARY KEY (id);


--
-- Name: entities entities_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY entities
    ADD CONSTRAINT entities_pkey PRIMARY KEY (id);


--
-- Name: futureworldstate futureworldstate_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY futureworldstate
    ADD CONSTRAINT futureworldstate_pkey PRIMARY KEY (id);


--
-- Name: futureworldstate futureworldstate_timestep_relation_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY futureworldstate
    ADD CONSTRAINT futureworldstate_timestep_relation_key UNIQUE (timestep, relation);


--
-- Name: goals goals_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY goals
    ADD CONSTRAINT goals_pkey PRIMARY KEY (id);


--
-- Name: items items_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY items
    ADD CONSTRAINT items_pkey PRIMARY KEY (id);


--
-- Name: locationtrait locationtrait_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY locationtrait
    ADD CONSTRAINT locationtrait_pkey PRIMARY KEY (id);


--
-- Name: locationtraits locationtraits_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY locationtraits
    ADD CONSTRAINT locationtraits_pkey PRIMARY KEY (id);


--
-- Name: persons persons_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY persons
    ADD CONSTRAINT persons_pkey PRIMARY KEY (id);


--
-- Name: positions positions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY positions
    ADD CONSTRAINT positions_pkey PRIMARY KEY (id);


--
-- Name: positions positions_x_y_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY positions
    ADD CONSTRAINT positions_x_y_key UNIQUE (x, y);


--
-- Name: relations relations_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY relations
    ADD CONSTRAINT relations_pkey PRIMARY KEY (id);


--
-- Name: relations relations_relationtype_object_subject_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY relations
    ADD CONSTRAINT relations_relationtype_object_subject_key UNIQUE (relationtype, object, subject);


--
-- Name: relationshiptypes relationshiptypes_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY relationshiptypes
    ADD CONSTRAINT relationshiptypes_pkey PRIMARY KEY (id);


--
-- Name: relationtypes relationtypes_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY relationtypes
    ADD CONSTRAINT relationtypes_pkey PRIMARY KEY (id);


--
-- Name: testtable testtable_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY testtable
    ADD CONSTRAINT testtable_pkey PRIMARY KEY (id);


--
-- Name: testtable testtable_x_y_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY testtable
    ADD CONSTRAINT testtable_x_y_key UNIQUE (x, y);


--
-- Name: actionrequirements actionrequirements_action_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY actionrequirements
    ADD CONSTRAINT actionrequirements_action_fkey FOREIGN KEY (action) REFERENCES actions(id);


--
-- Name: actionrequirements actionrequirements_relation_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY actionrequirements
    ADD CONSTRAINT actionrequirements_relation_fkey FOREIGN KEY (relation) REFERENCES relations(id);


--
-- Name: actionrequirements actionrequirements_requirementtype_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY actionrequirements
    ADD CONSTRAINT actionrequirements_requirementtype_fkey FOREIGN KEY (requirementtype) REFERENCES actionrequirementstypes(id);


--
-- Name: actionresults actionresults_action_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY actionresults
    ADD CONSTRAINT actionresults_action_fkey FOREIGN KEY (action) REFERENCES actions(id);


--
-- Name: actionresults actionresults_type_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY actionresults
    ADD CONSTRAINT actionresults_type_fkey FOREIGN KEY (type) REFERENCES actionresultstype(id);


--
-- Name: commodities commodities_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY commodities
    ADD CONSTRAINT commodities_id_fkey FOREIGN KEY (id) REFERENCES entities(id);


--
-- Name: goals goals_person_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY goals
    ADD CONSTRAINT goals_person_fkey FOREIGN KEY (person) REFERENCES persons(id);


--
-- Name: goals goals_relation_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY goals
    ADD CONSTRAINT goals_relation_fkey FOREIGN KEY (relation) REFERENCES relations(id);


--
-- Name: items items_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY items
    ADD CONSTRAINT items_id_fkey FOREIGN KEY (id) REFERENCES entities(id);


--
-- Name: locationtrait locationtrait_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY locationtrait
    ADD CONSTRAINT locationtrait_id_fkey FOREIGN KEY (id) REFERENCES entities(id);


--
-- Name: locationtraits locationtraits_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY locationtraits
    ADD CONSTRAINT locationtraits_id_fkey FOREIGN KEY (id) REFERENCES entities(id);


--
-- Name: persons persons_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY persons
    ADD CONSTRAINT persons_id_fkey FOREIGN KEY (id) REFERENCES entities(id);


--
-- Name: positions positions_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY positions
    ADD CONSTRAINT positions_id_fkey FOREIGN KEY (id) REFERENCES entities(id);


--
-- Name: relations relations_object_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY relations
    ADD CONSTRAINT relations_object_fkey FOREIGN KEY (object) REFERENCES entities(id);


--
-- Name: relations relations_relationtype_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY relations
    ADD CONSTRAINT relations_relationtype_fkey FOREIGN KEY (relationtype) REFERENCES relationtypes(id);


--
-- Name: relations relations_subject_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY relations
    ADD CONSTRAINT relations_subject_fkey FOREIGN KEY (subject) REFERENCES entities(id);


--
-- PostgreSQL database dump complete
--

