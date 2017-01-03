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
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


--
-- Name: uuid-ossp; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS "uuid-ossp" WITH SCHEMA public;


--
-- Name: EXTENSION "uuid-ossp"; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION "uuid-ossp" IS 'generate universally unique identifiers (UUIDs)';


SET search_path = public, pg_catalog;

--
-- Name: closetimestep(); Type: FUNCTION; Schema: public; Owner: office
--

CREATE FUNCTION closetimestep() RETURNS void
    LANGUAGE sql
    AS $$
  

    INSERT INTO Paths (
      SELECT pathId, pathId2 FROM 
        (Select row_number() OVER (ORDER BY c.id DESC), c.id  AS pathId FROM (SELECT id FROM Paths
        EXCEPT
        SELECT prev FROM Paths) c) c 
      JOIN 
        (SELECT row_number() OVER (ORDER BY c2.id DESC ), c2.id AS pathId2 FROM (SELECT id FROM Paths
        EXCEPT
        SELECT prev FROM Paths) c2 OFFSET 1) c2 
      ON c.row_number = c2.row_number - 1
      
        
    ) ON CONFLICT(id) DO UPDATE SET prev = excluded.prev;

$$;


ALTER FUNCTION public.closetimestep() OWNER TO office;

--
-- Name: dispatchactionrequirements(integer, integer, integer, integer, uuid); Type: FUNCTION; Schema: public; Owner: office
--

CREATE FUNCTION dispatchactionrequirements(integer, integer, integer, integer, uuid) RETURNS boolean
    LANGUAGE sql
    AS $_$
  SELECT dispatchActionRequirements(
    (SELECT requirementType FROM ActionRequirements WHERE action = $1),
    $1,
    $2,
    $3,
    $4,
    $5
  )
$_$;


ALTER FUNCTION public.dispatchactionrequirements(integer, integer, integer, integer, uuid) OWNER TO office;

--
-- Name: dispatchactionrequirements(integer, integer, integer, integer, integer, uuid); Type: FUNCTION; Schema: public; Owner: office
--

CREATE FUNCTION dispatchactionrequirements(integer, integer, integer, integer, integer, uuid) RETURNS boolean
    LANGUAGE sql
    AS $_$
  SELECT
    CASE WHEN $1 = 0 THEN
      -- same location
      (SELECT sameLocation($3, $4, $5, $6))
    WHEN $1 = 2 THEN
      -- count relation types
      (SELECT 
        (SELECT (r.relationtype, COUNT(req.relation)) FROM ActionRequirements req 
          JOIN Relations r ON req.relation = r.id 
          WHERE req.action=$2
          GROUP BY r.relationtype
        )
        <= 
        (SELECT (r2.relationtype, COUNT(world.relation)) FROM
             FutureWorldState world JOIN Relations r2 
              ON world.relation = r2.id
              WHERE world.timestep = $5
            
          GROUP BY r2.relationtype
        )
      )
    WHEN $1 = 1 THEN 
      -- count relations 
      (SELECT 
        (SELECT COUNT(req.relation) FROM ActionRequirements req WHERE req.action=$2)
        = 
        (SELECT COUNT(req.relation) FROM ActionRequirements req 
          JOIN FutureWorldState world ON world.relation=req.relation AND world.timestep = $5 AND world.pathId = $6 
          WHERE req.action=$2
        )
      )
    END;

$_$;


ALTER FUNCTION public.dispatchactionrequirements(integer, integer, integer, integer, integer, uuid) OWNER TO office;

--
-- Name: dispatchactionresults(uuid, integer, integer, integer, integer); Type: FUNCTION; Schema: public; Owner: office
--

CREATE FUNCTION dispatchactionresults(uuid, integer, integer, integer, integer) RETURNS void
    LANGUAGE sql
    AS $_$
  SELECT 
    CASE 
      WHEN $2=0 THEN
        -- pathId, actor, timestep, x, y, Direction
        (SELECT walk(
          $1,
          $4, 
          $5, 
          (SELECT x FROM Positions WHERE id = (SELECT positionOfActorAtTimestep($4, $5, $1))),
          (SELECT y FROM Positions WHERE id = (SELECT positionOfActorAtTimestep($4, $5, $1))),
          $3)
        )
      WHEN $2=1 THEN
        (SELECT makeFutureRelation($1, $5, $3))
  END
$_$;


ALTER FUNCTION public.dispatchactionresults(uuid, integer, integer, integer, integer) OWNER TO office;

--
-- Name: dispatchactionresults(uuid, integer, integer, integer, integer, uuid); Type: FUNCTION; Schema: public; Owner: office
--

CREATE FUNCTION dispatchactionresults(uuid, integer, integer, integer, integer, uuid) RETURNS integer
    LANGUAGE sql
    AS $_$

    SELECT 
      CASE 
        WHEN $2=0 THEN
          -- pathId, actor, timestep, x, y, Direction
          (SELECT walk(
            $1,
            $4, 
            $5, 
            (SELECT x FROM Positions WHERE id = (SELECT positionOfActorAtTimestep($4, $5, $6))),
            (SELECT y FROM Positions WHERE id = (SELECT positionOfActorAtTimestep($4, $5, $6))),
            $3)
          )
        WHEN $2=1 THEN
          (SELECT makeFutureRelation($1, $5, $3))
      END
    ;

$_$;


ALTER FUNCTION public.dispatchactionresults(uuid, integer, integer, integer, integer, uuid) OWNER TO office;

--
-- Name: dispatchpathupdate(uuid, uuid); Type: FUNCTION; Schema: public; Owner: office
--

CREATE FUNCTION dispatchpathupdate(uuid, uuid) RETURNS void
    LANGUAGE sql
    AS $_$
  INSERT INTO Paths VALUES ($1, $2) ON CONFLICT DO NOTHING;
$_$;


ALTER FUNCTION public.dispatchpathupdate(uuid, uuid) OWNER TO office;

--
-- Name: initfuturewithcurrent(uuid, integer); Type: FUNCTION; Schema: public; Owner: office
--

CREATE FUNCTION initfuturewithcurrent(uuid, integer) RETURNS void
    LANGUAGE sql
    AS $_$
  INSERT INTO FutureWorldState (pathId, timestep, relation) (
    SELECT $1, 0, $2
  )
  ON CONFLICT DO NOTHING; 

  INSERT INTO Paths (
    SELECT pathId, pathId2 FROM 
        (Select row_number() OVER (ORDER BY c.id DESC), c.id, c.pathId FROM FutureWorldState c) c 
      LEFT JOIN 
        (SELECT row_number() OVER (ORDER BY c2.id DESC ), c2.id AS id2, c2.pathId AS pathId2 FROM FutureWorldState c2 OFFSET 1) c2 
      ON c.row_number = c2.row_number - 1
  )
  ON CONFLICT DO NOTHING

$_$;


ALTER FUNCTION public.initfuturewithcurrent(uuid, integer) OWNER TO office;

--
-- Name: insertnewposition(integer, integer); Type: FUNCTION; Schema: public; Owner: office
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


ALTER FUNCTION public.insertnewposition(integer, integer) OWNER TO office;

--
-- Name: insertnewposition(integer, integer, integer); Type: FUNCTION; Schema: public; Owner: office
--

CREATE FUNCTION insertnewposition(integer, integer, integer) RETURNS integer
    LANGUAGE sql
    AS $_$
  INSERT INTO Positions VALUES($1, $2, $3)
  ON CONFLICT(x,y) DO UPDATE SET x=$2
  RETURNING id;
$_$;


ALTER FUNCTION public.insertnewposition(integer, integer, integer) OWNER TO office;

--
-- Name: lastpathidoftimestep(integer); Type: FUNCTION; Schema: public; Owner: office
--

CREATE FUNCTION lastpathidoftimestep(integer) RETURNS uuid
    LANGUAGE sql
    AS $_$
  
  SELECT FutureWorldState.pathId FROM FutureWorldState
    JOIN Paths p ON p.prev = FutureWorldState.pathId
    LEFT JOIN FutureWorldState nextState ON p.id = nextState.pathId 
    WHERE FutureWorldState.timestep = $1 AND (nextState.timestep = $1 + 1 OR nextState IS NULL);
$_$;


ALTER FUNCTION public.lastpathidoftimestep(integer) OWNER TO office;

--
-- Name: makefuturerelation(uuid, integer, integer); Type: FUNCTION; Schema: public; Owner: office
--

CREATE FUNCTION makefuturerelation(uuid, integer, integer) RETURNS integer
    LANGUAGE sql
    AS $_$
  INSERT INTO FutureWorldState(pathId, timestep, relation) VALUES($1, $2 + 1, $3) ON CONFLICT DO NOTHING
  RETURNING id;
$_$;


ALTER FUNCTION public.makefuturerelation(uuid, integer, integer) OWNER TO office;

--
-- Name: positionofactorattimestep(integer, integer, uuid); Type: FUNCTION; Schema: public; Owner: office
--

CREATE FUNCTION positionofactorattimestep(integer, integer, uuid) RETURNS integer
    LANGUAGE sql
    AS $_$
  -- traverses backwards from the given pathId ($3)
  -- returns positionId of the actor ($1) at the given timestep ($2)
  WITH RECURSIVE TraceBackwards AS (
    SELECT Paths.id AS start, Paths.prev AS prev, 1 AS length
      FROM Paths
      WHERE id = $3
    UNION
    SELECT Paths.id AS start, Paths.id AS prev, 0 AS length
      FROM Paths
      WHERE id = $3
    UNION
    SELECT tb.start AS start, p.prev AS prev, tb.length + 1 AS length
      FROM Paths p
      JOIN TraceBackwards tb 
        ON tb.prev = p.id
  )
  SELECT r.subject FROM TraceBackwards tb 
    JOIN FutureWorldState world ON tb.prev = world.pathId 
    JOIN Relations r ON world.relation = r.id 
    JOIN RelationTypes rt ON rt.id = r.relationType AND rt.name = 'At' 
    WHERE tb.start = $3 AND world.timestep <= $2 AND r.object = $1
    ORDER BY tb.length
    LIMIT 1;
  
$_$;


ALTER FUNCTION public.positionofactorattimestep(integer, integer, uuid) OWNER TO office;

--
-- Name: relationafterwalking(integer, integer, integer, integer); Type: FUNCTION; Schema: public; Owner: office
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
    RETURNING id;
  
$_$;


ALTER FUNCTION public.relationafterwalking(integer, integer, integer, integer) OWNER TO office;

--
-- Name: samelocation(integer, integer, integer, uuid); Type: FUNCTION; Schema: public; Owner: office
--

CREATE FUNCTION samelocation(integer, integer, integer, uuid) RETURNS boolean
    LANGUAGE sql
    AS $_$
   SELECT (SELECT positionOfActorAtTimestep($1, $3, $4)) = (SELECT positionOfActorAtTimestep($2, $3, $4));
  
$_$;


ALTER FUNCTION public.samelocation(integer, integer, integer, uuid) OWNER TO office;

--
-- Name: seethefuture(integer); Type: FUNCTION; Schema: public; Owner: office
--

CREATE FUNCTION seethefuture(integer) RETURNS bigint
    LANGUAGE sql
    AS $_$
  
    SELECT COUNT(dispatchPathUpdate(AppliedResults.newPathId, AppliedResults.pathId)) FROM (
      SELECT 
        COUNT(dispatchActionResults(
          PossiblePathResults.newPathId, 
          PossiblePathResults.type, 
          PossiblePathResults.value, 
          PossiblePathResults.person, 
          $1,
          PossiblePathResults.pathId
        )), 
        PossiblePathResults.pathId, PossiblePathResults.newPathId
      FROM (
        SELECT 
            PossibleResults.pathId, 
            (uuid_generate_v4()) as newPathId,
            PossibleResults.type, PossibleResults.value,
            PossibleResults.person, $1
          FROM (
            SELECT results.value, results.type, p.id AS person, currentStep.pathId
              FROM Actions a
              CROSS JOIN Persons p
              CROSS JOIN Relations rr 
              JOIN ActionRequirements ar ON ar.action = a.id
              JOIN ActionResults results ON results.action = a.id 
              JOIN FutureWorldState previousWorldState 
                ON previousWorldState.relation = rr.id 
                AND previousWorldState.timestep <= $1 
                AND rr.object = p.id
              JOIN FutureWorldState currentStep 
                ON currentStep.timestep = $1 
                AND -- currentStep can be reached from previousWorldState
                    -- without an intermediate step overiding the previousWorldState's relation
                    --  i.e. Relation (relationType, rr.object, _) does not occur along the path
                (
                    WITH RECURSIVE TraceBackwards AS (
                      SELECT Paths.id AS start, Paths.prev AS prev, 0 AS length
                        FROM Paths
                        WHERE id = currentStep.pathId
                      UNION
                      SELECT Paths.id AS start, Paths.id AS prev, 0 AS length
                        FROM Paths
                        WHERE id = currentStep.pathId
                      UNION
                      SELECT tb.start AS start, p.prev AS prev, tb.length + 1 AS length
                        FROM Paths p
                        JOIN TraceBackwards tb 
                          ON tb.prev = p.id
                    )
                    SELECT previousWorldState.pathId IN (
                      SELECT world.pathId FROM TraceBackwards tb 
                        JOIN FutureWorldState world ON tb.prev = world.pathId 
                        JOIN Relations r ON world.relation = r.id 
                        WHERE tb.start = currentStep.pathId AND r.object = rr.object AND r.relationType = rr.relationType
                        ORDER BY tb.length
                        LIMIT 1
                    ) = TRUE

                )
              WHERE (
                  -- action, actor, subject, timestep, pathId
                  SELECT dispatchActionRequirements(a.id, p.id, rr.subject, $1, currentStep.pathId) = TRUE
              )
          ) PossibleResults
      ) PossiblePathResults GROUP BY pathId, newPathId
    ) AppliedResults;
  
  -- FROM generate_series(0, $1 - 1) AS i;
$_$;


ALTER FUNCTION public.seethefuture(integer) OWNER TO office;

--
-- Name: walk(uuid, integer, integer, integer, integer, integer); Type: FUNCTION; Schema: public; Owner: office
--

CREATE FUNCTION walk(uuid, integer, integer, integer, integer, integer) RETURNS integer
    LANGUAGE sql
    AS $_$
    INSERT INTO FutureWorldState(pathId, timestep, relation) VALUES($1, $3 + 1, relationAfterWalking($2, $4, $5, $6)) 
    ON CONFLICT DO NOTHING
    RETURNING relation;
$_$;


ALTER FUNCTION public.walk(uuid, integer, integer, integer, integer, integer) OWNER TO office;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: aaa; Type: TABLE; Schema: public; Owner: office
--

CREATE TABLE aaa (
    lastpathid uuid
);


ALTER TABLE aaa OWNER TO office;

--
-- Name: actionrequirements; Type: TABLE; Schema: public; Owner: office
--

CREATE TABLE actionrequirements (
    id integer NOT NULL,
    action integer,
    requirementtype integer,
    relation integer
);


ALTER TABLE actionrequirements OWNER TO office;

--
-- Name: actionrequirementstypes; Type: TABLE; Schema: public; Owner: office
--

CREATE TABLE actionrequirementstypes (
    id integer NOT NULL,
    name text
);


ALTER TABLE actionrequirementstypes OWNER TO office;

--
-- Name: actionresults; Type: TABLE; Schema: public; Owner: office
--

CREATE TABLE actionresults (
    id integer NOT NULL,
    action integer,
    type integer,
    value integer
);


ALTER TABLE actionresults OWNER TO office;

--
-- Name: actionresultstype; Type: TABLE; Schema: public; Owner: office
--

CREATE TABLE actionresultstype (
    id integer NOT NULL,
    name text
);


ALTER TABLE actionresultstype OWNER TO office;

--
-- Name: actions; Type: TABLE; Schema: public; Owner: office
--

CREATE TABLE actions (
    id integer NOT NULL,
    name text
);


ALTER TABLE actions OWNER TO office;

--
-- Name: commodities; Type: TABLE; Schema: public; Owner: office
--

CREATE TABLE commodities (
    id integer NOT NULL,
    name text
);


ALTER TABLE commodities OWNER TO office;

--
-- Name: currentworldstate; Type: TABLE; Schema: public; Owner: office
--

CREATE TABLE currentworldstate (
    id integer NOT NULL,
    relation integer
);


ALTER TABLE currentworldstate OWNER TO office;

--
-- Name: entities; Type: TABLE; Schema: public; Owner: office
--

CREATE TABLE entities (
    id integer NOT NULL
);


ALTER TABLE entities OWNER TO office;

--
-- Name: futureworldstate; Type: TABLE; Schema: public; Owner: office
--

CREATE TABLE futureworldstate (
    id integer NOT NULL,
    pathid uuid,
    timestep integer,
    relation integer
);


ALTER TABLE futureworldstate OWNER TO office;

--
-- Name: futureworldstate_id_seq; Type: SEQUENCE; Schema: public; Owner: office
--

CREATE SEQUENCE futureworldstate_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE futureworldstate_id_seq OWNER TO office;

--
-- Name: futureworldstate_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: office
--

ALTER SEQUENCE futureworldstate_id_seq OWNED BY futureworldstate.id;


--
-- Name: goals; Type: TABLE; Schema: public; Owner: office
--

CREATE TABLE goals (
    id integer NOT NULL,
    person integer,
    relation integer
);


ALTER TABLE goals OWNER TO office;

--
-- Name: items; Type: TABLE; Schema: public; Owner: office
--

CREATE TABLE items (
    id integer NOT NULL,
    name text
);


ALTER TABLE items OWNER TO office;

--
-- Name: locationtraits; Type: TABLE; Schema: public; Owner: office
--

CREATE TABLE locationtraits (
    id integer NOT NULL,
    name text
);


ALTER TABLE locationtraits OWNER TO office;

--
-- Name: paths; Type: TABLE; Schema: public; Owner: office
--

CREATE TABLE paths (
    id uuid NOT NULL,
    prev uuid
);


ALTER TABLE paths OWNER TO office;

--
-- Name: persons; Type: TABLE; Schema: public; Owner: office
--

CREATE TABLE persons (
    id integer NOT NULL,
    name text
);


ALTER TABLE persons OWNER TO office;

--
-- Name: positions; Type: TABLE; Schema: public; Owner: office
--

CREATE TABLE positions (
    id integer NOT NULL,
    x integer NOT NULL,
    y integer NOT NULL
);


ALTER TABLE positions OWNER TO office;

--
-- Name: positions_id_seq; Type: SEQUENCE; Schema: public; Owner: office
--

CREATE SEQUENCE positions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE positions_id_seq OWNER TO office;

--
-- Name: positions_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: office
--

ALTER SEQUENCE positions_id_seq OWNED BY positions.id;


--
-- Name: relations; Type: TABLE; Schema: public; Owner: office
--

CREATE TABLE relations (
    id integer NOT NULL,
    relationtype integer,
    object integer,
    subject integer
);


ALTER TABLE relations OWNER TO office;

--
-- Name: relationtypes; Type: TABLE; Schema: public; Owner: office
--

CREATE TABLE relationtypes (
    id integer NOT NULL,
    name text
);


ALTER TABLE relationtypes OWNER TO office;

--
-- Name: futureworldstate id; Type: DEFAULT; Schema: public; Owner: office
--

ALTER TABLE ONLY futureworldstate ALTER COLUMN id SET DEFAULT nextval('futureworldstate_id_seq'::regclass);


--
-- Name: positions id; Type: DEFAULT; Schema: public; Owner: office
--

ALTER TABLE ONLY positions ALTER COLUMN id SET DEFAULT nextval('positions_id_seq'::regclass);


--
-- Name: actionrequirements actionrequirements_action_relation_key; Type: CONSTRAINT; Schema: public; Owner: office
--

ALTER TABLE ONLY actionrequirements
    ADD CONSTRAINT actionrequirements_action_relation_key UNIQUE (action, relation);


--
-- Name: actionrequirements actionrequirements_pkey; Type: CONSTRAINT; Schema: public; Owner: office
--

ALTER TABLE ONLY actionrequirements
    ADD CONSTRAINT actionrequirements_pkey PRIMARY KEY (id);


--
-- Name: actionrequirementstypes actionrequirementstypes_pkey; Type: CONSTRAINT; Schema: public; Owner: office
--

ALTER TABLE ONLY actionrequirementstypes
    ADD CONSTRAINT actionrequirementstypes_pkey PRIMARY KEY (id);


--
-- Name: actionresults actionresults_pkey; Type: CONSTRAINT; Schema: public; Owner: office
--

ALTER TABLE ONLY actionresults
    ADD CONSTRAINT actionresults_pkey PRIMARY KEY (id);


--
-- Name: actionresultstype actionresultstype_pkey; Type: CONSTRAINT; Schema: public; Owner: office
--

ALTER TABLE ONLY actionresultstype
    ADD CONSTRAINT actionresultstype_pkey PRIMARY KEY (id);


--
-- Name: actions actions_pkey; Type: CONSTRAINT; Schema: public; Owner: office
--

ALTER TABLE ONLY actions
    ADD CONSTRAINT actions_pkey PRIMARY KEY (id);


--
-- Name: commodities commodities_pkey; Type: CONSTRAINT; Schema: public; Owner: office
--

ALTER TABLE ONLY commodities
    ADD CONSTRAINT commodities_pkey PRIMARY KEY (id);


--
-- Name: currentworldstate currentworldstate_pkey; Type: CONSTRAINT; Schema: public; Owner: office
--

ALTER TABLE ONLY currentworldstate
    ADD CONSTRAINT currentworldstate_pkey PRIMARY KEY (id);


--
-- Name: currentworldstate currentworldstate_relation_key; Type: CONSTRAINT; Schema: public; Owner: office
--

ALTER TABLE ONLY currentworldstate
    ADD CONSTRAINT currentworldstate_relation_key UNIQUE (relation);


--
-- Name: entities entities_pkey; Type: CONSTRAINT; Schema: public; Owner: office
--

ALTER TABLE ONLY entities
    ADD CONSTRAINT entities_pkey PRIMARY KEY (id);


--
-- Name: futureworldstate futureworldstate_pathid_timestep_key; Type: CONSTRAINT; Schema: public; Owner: office
--

ALTER TABLE ONLY futureworldstate
    ADD CONSTRAINT futureworldstate_pathid_timestep_key UNIQUE (pathid, timestep);


--
-- Name: futureworldstate futureworldstate_pkey; Type: CONSTRAINT; Schema: public; Owner: office
--

ALTER TABLE ONLY futureworldstate
    ADD CONSTRAINT futureworldstate_pkey PRIMARY KEY (id);


--
-- Name: goals goals_pkey; Type: CONSTRAINT; Schema: public; Owner: office
--

ALTER TABLE ONLY goals
    ADD CONSTRAINT goals_pkey PRIMARY KEY (id);


--
-- Name: items items_pkey; Type: CONSTRAINT; Schema: public; Owner: office
--

ALTER TABLE ONLY items
    ADD CONSTRAINT items_pkey PRIMARY KEY (id);


--
-- Name: locationtraits locationtraits_pkey; Type: CONSTRAINT; Schema: public; Owner: office
--

ALTER TABLE ONLY locationtraits
    ADD CONSTRAINT locationtraits_pkey PRIMARY KEY (id);


--
-- Name: paths paths_pkey; Type: CONSTRAINT; Schema: public; Owner: office
--

ALTER TABLE ONLY paths
    ADD CONSTRAINT paths_pkey PRIMARY KEY (id);


--
-- Name: persons persons_pkey; Type: CONSTRAINT; Schema: public; Owner: office
--

ALTER TABLE ONLY persons
    ADD CONSTRAINT persons_pkey PRIMARY KEY (id);


--
-- Name: positions positions_pkey; Type: CONSTRAINT; Schema: public; Owner: office
--

ALTER TABLE ONLY positions
    ADD CONSTRAINT positions_pkey PRIMARY KEY (id);


--
-- Name: positions positions_x_y_key; Type: CONSTRAINT; Schema: public; Owner: office
--

ALTER TABLE ONLY positions
    ADD CONSTRAINT positions_x_y_key UNIQUE (x, y);


--
-- Name: relations relations_pkey; Type: CONSTRAINT; Schema: public; Owner: office
--

ALTER TABLE ONLY relations
    ADD CONSTRAINT relations_pkey PRIMARY KEY (id);


--
-- Name: relations relations_relationtype_object_subject_key; Type: CONSTRAINT; Schema: public; Owner: office
--

ALTER TABLE ONLY relations
    ADD CONSTRAINT relations_relationtype_object_subject_key UNIQUE (relationtype, object, subject);


--
-- Name: relationtypes relationtypes_pkey; Type: CONSTRAINT; Schema: public; Owner: office
--

ALTER TABLE ONLY relationtypes
    ADD CONSTRAINT relationtypes_pkey PRIMARY KEY (id);


--
-- Name: actionrequirements actionrequirements_action_fkey; Type: FK CONSTRAINT; Schema: public; Owner: office
--

ALTER TABLE ONLY actionrequirements
    ADD CONSTRAINT actionrequirements_action_fkey FOREIGN KEY (action) REFERENCES actions(id);


--
-- Name: actionrequirements actionrequirements_relation_fkey; Type: FK CONSTRAINT; Schema: public; Owner: office
--

ALTER TABLE ONLY actionrequirements
    ADD CONSTRAINT actionrequirements_relation_fkey FOREIGN KEY (relation) REFERENCES relations(id);


--
-- Name: actionrequirements actionrequirements_requirementtype_fkey; Type: FK CONSTRAINT; Schema: public; Owner: office
--

ALTER TABLE ONLY actionrequirements
    ADD CONSTRAINT actionrequirements_requirementtype_fkey FOREIGN KEY (requirementtype) REFERENCES actionrequirementstypes(id);


--
-- Name: actionresults actionresults_action_fkey; Type: FK CONSTRAINT; Schema: public; Owner: office
--

ALTER TABLE ONLY actionresults
    ADD CONSTRAINT actionresults_action_fkey FOREIGN KEY (action) REFERENCES actions(id);


--
-- Name: actionresults actionresults_type_fkey; Type: FK CONSTRAINT; Schema: public; Owner: office
--

ALTER TABLE ONLY actionresults
    ADD CONSTRAINT actionresults_type_fkey FOREIGN KEY (type) REFERENCES actionresultstype(id);


--
-- Name: commodities commodities_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: office
--

ALTER TABLE ONLY commodities
    ADD CONSTRAINT commodities_id_fkey FOREIGN KEY (id) REFERENCES entities(id);


--
-- Name: goals goals_person_fkey; Type: FK CONSTRAINT; Schema: public; Owner: office
--

ALTER TABLE ONLY goals
    ADD CONSTRAINT goals_person_fkey FOREIGN KEY (person) REFERENCES persons(id);


--
-- Name: goals goals_relation_fkey; Type: FK CONSTRAINT; Schema: public; Owner: office
--

ALTER TABLE ONLY goals
    ADD CONSTRAINT goals_relation_fkey FOREIGN KEY (relation) REFERENCES relations(id);


--
-- Name: items items_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: office
--

ALTER TABLE ONLY items
    ADD CONSTRAINT items_id_fkey FOREIGN KEY (id) REFERENCES entities(id);


--
-- Name: locationtraits locationtraits_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: office
--

ALTER TABLE ONLY locationtraits
    ADD CONSTRAINT locationtraits_id_fkey FOREIGN KEY (id) REFERENCES entities(id);


--
-- Name: paths paths_prev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: office
--

ALTER TABLE ONLY paths
    ADD CONSTRAINT paths_prev_fkey FOREIGN KEY (prev) REFERENCES paths(id);


--
-- Name: persons persons_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: office
--

ALTER TABLE ONLY persons
    ADD CONSTRAINT persons_id_fkey FOREIGN KEY (id) REFERENCES entities(id);


--
-- Name: positions positions_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: office
--

ALTER TABLE ONLY positions
    ADD CONSTRAINT positions_id_fkey FOREIGN KEY (id) REFERENCES entities(id);


--
-- Name: relations relations_object_fkey; Type: FK CONSTRAINT; Schema: public; Owner: office
--

ALTER TABLE ONLY relations
    ADD CONSTRAINT relations_object_fkey FOREIGN KEY (object) REFERENCES entities(id);


--
-- Name: relations relations_relationtype_fkey; Type: FK CONSTRAINT; Schema: public; Owner: office
--

ALTER TABLE ONLY relations
    ADD CONSTRAINT relations_relationtype_fkey FOREIGN KEY (relationtype) REFERENCES relationtypes(id);


--
-- Name: relations relations_subject_fkey; Type: FK CONSTRAINT; Schema: public; Owner: office
--

ALTER TABLE ONLY relations
    ADD CONSTRAINT relations_subject_fkey FOREIGN KEY (subject) REFERENCES entities(id);


--
-- PostgreSQL database dump complete
--

