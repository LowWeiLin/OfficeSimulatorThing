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
-- Name: createthefuture(integer); Type: FUNCTION; Schema: public; Owner: office
--

CREATE FUNCTION createthefuture(integer) RETURNS bigint
    LANGUAGE sql
    AS $_$
  SELECT (
    SELECT COUNT(dispatchPathUpdate(AppliedResults.newPathId, AppliedResults.pathId)) FROM (
      SELECT 
        COUNT(dispatchActionResults(
          PossiblePathResults.newPathId, 
          PossiblePathResults.type, 
          PossiblePathResults.value, 
          PossiblePathResults.person, 
          $1,
          PossiblePathResults.pathId
        )), PossiblePathResults.pathId, PossiblePathResults.newPathId
      FROM (
        SELECT 
            lastPathId() as pathId, 
            (uuid_generate_v4()) as newPathId,
            PossibleResults.type, PossibleResults.value,
            PossibleResults.person, $1
          FROM (
            SELECT results.value, results.type, p.id AS person, world.pathId
              FROM Actions a
              CROSS JOIN Persons p
              CROSS JOIN Relations rr 
              JOIN ActionRequirements ar ON 
                 ar.action = a.id
                 -- AND ar.relation = rr.id
              JOIN ActionResults results ON results.action = a.id
              JOIN FutureWorldState world ON world.relation = rr.id 
                                          AND world.timestep = $1 
              WHERE (
                -- action, actor, subject, timestep, pathId
                SELECT dispatchActionRequirements(a.id, p.id, rr.subject, $1, lastPathId()) = TRUE
              )
          ) PossibleResults
      ) PossiblePathResults GROUP BY pathId, newPathId
    ) AppliedResults
  );
  
  -- FROM generate_series(0, $1 - 1) AS i;
$_$;


ALTER FUNCTION public.createthefuture(integer) OWNER TO office;

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
-- Name: lastpathid(); Type: FUNCTION; Schema: public; Owner: office
--

CREATE FUNCTION lastpathid() RETURNS uuid
    LANGUAGE sql
    AS $$
    SELECT id FROM Paths
      EXCEPT
      SELECT prev FROM Paths
  ;
$$;


ALTER FUNCTION public.lastpathid() OWNER TO office;

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
    SELECT Paths.id AS start, Paths.prev AS prev, 0 AS length
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
    WHERE tb.start = $3 AND world.timestep = $2 AND r.object = $1
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
    RETURNING id  
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
-- Data for Name: aaa; Type: TABLE DATA; Schema: public; Owner: office
--

COPY aaa (lastpathid) FROM stdin;
015b7269-ae29-4889-8b0f-a263947e3a2e
\.


--
-- Data for Name: actionrequirements; Type: TABLE DATA; Schema: public; Owner: office
--

COPY actionrequirements (id, action, requirementtype, relation) FROM stdin;
0	1	0	6
1	2	2	1
2	3	2	1
3	4	2	1
4	5	2	1
\.


--
-- Data for Name: actionrequirementstypes; Type: TABLE DATA; Schema: public; Owner: office
--

COPY actionrequirementstypes (id, name) FROM stdin;
0	same location
1	relations
2	has relation of type
\.


--
-- Data for Name: actionresults; Type: TABLE DATA; Schema: public; Owner: office
--

COPY actionresults (id, action, type, value) FROM stdin;
0	1	1	3
1	2	0	1
2	3	0	2
3	4	0	3
4	5	0	4
\.


--
-- Data for Name: actionresultstype; Type: TABLE DATA; Schema: public; Owner: office
--

COPY actionresultstype (id, name) FROM stdin;
0	movement
1	relations
\.


--
-- Data for Name: actions; Type: TABLE DATA; Schema: public; Owner: office
--

COPY actions (id, name) FROM stdin;
1	take coffee
2	walk left
3	walk up
4	walk right
5	walk down
\.


--
-- Data for Name: commodities; Type: TABLE DATA; Schema: public; Owner: office
--

COPY commodities (id, name) FROM stdin;
5	Food
\.


--
-- Data for Name: currentworldstate; Type: TABLE DATA; Schema: public; Owner: office
--

COPY currentworldstate (id, relation) FROM stdin;
2	5
3	7
0	1
1	2
\.


--
-- Data for Name: entities; Type: TABLE DATA; Schema: public; Owner: office
--

COPY entities (id) FROM stdin;
0
1
2
3
4
5
6
7
8
9
10
11
12
13
14
15
16
17
18
19
20
21
22
23
24
25
26
27
28
29
30
31
32
33
34
35
36
37
38
39
40
41
42
43
44
45
46
47
48
49
50
51
52
53
54
55
56
57
58
59
60
61
62
63
64
65
66
67
68
69
70
71
72
73
74
75
76
77
78
79
80
81
82
83
84
85
86
87
88
89
90
91
92
93
94
95
96
97
98
99
100
101
102
103
104
105
106
107
108
109
110
111
112
113
114
115
116
117
118
119
120
121
122
123
124
125
126
127
128
129
130
131
132
133
134
135
136
137
138
139
140
141
142
143
144
145
146
147
148
149
150
151
152
\.


--
-- Data for Name: futureworldstate; Type: TABLE DATA; Schema: public; Owner: office
--

COPY futureworldstate (id, pathid, timestep, relation) FROM stdin;
268	568f76a5-052b-4a3a-b6be-d54304f39fd8	0	5
269	0c3b9e39-a4fc-4b1d-8757-9661cfb290db	0	7
270	db04acff-8c33-4842-8636-cb23d52521f5	0	1
271	632c6082-20c3-4752-8f50-3503cbfef189	0	2
272	0913c9ef-3c59-4156-8527-54345aa1122a	1	11
273	258d3127-719d-4c90-9f5c-bcb0277643d3	1	15
274	29bb79c8-f6d3-4e29-bef1-d9fec63ae8f1	1	9
275	2c57ceea-6ea8-4935-8baf-512221057058	1	10
276	2f59fce3-676f-46b1-9215-bca5923d1b07	1	8
277	3cc4c68e-feea-43e2-b587-385cfb6e3388	1	8
278	3fb3a67f-1b66-45d6-ba39-82e85a99986e	1	12
279	4d56e0c2-f737-4a94-90b5-d6d431eaee06	1	15
280	577891e0-9901-45e4-81fe-8fd53d13659f	1	14
281	586d4e35-3947-47de-9b30-ec2fa7da57bc	1	10
282	5fee1fa4-96bd-48b4-a0f7-46f1eb0caa99	1	13
283	68de56fa-b411-4532-a933-0b51bd749317	1	10
284	6fc8440c-85e7-495f-b7f1-26fe35934332	1	14
285	768d749c-1608-45c1-9044-cf03e7da293b	1	11
286	7a4138d9-12e1-4f11-a97d-bb21851f5665	1	8
287	7da62871-b649-452f-b413-280a25e34f02	1	12
288	8229ef13-22d9-4a19-81d7-bce80e56c2d8	1	9
289	93b4389f-27eb-4e64-bbd6-a90e02457c96	1	11
290	a2bd45fb-0d46-428b-96c1-9f0d9a67111e	1	8
291	a6fd110b-7bb1-4132-a142-bce19704e85f	1	13
292	a95442be-2c90-4abf-b32c-ba687ab84fc8	1	13
293	ab712442-8e2d-4996-88ff-c1f1a08e4536	1	13
294	ad8f712a-9be4-4f3a-af54-4c7684f4dc05	1	12
295	b3d55298-664d-4e68-9790-956d855dd73c	1	15
296	b82f35ff-f750-43b0-a435-c8625cacbf4f	1	14
297	c153353b-3f50-4a03-8496-1ae4b737cd68	1	12
298	c6534fa4-80be-4832-98c9-cec116af2806	1	15
299	c691d6ed-932b-4ea9-a9c3-d50e1a011f42	1	10
300	c72ba7eb-93f5-4e03-bdf0-94db7a363480	1	11
301	d0dcdd0f-3a18-449a-a237-aae4fd676530	1	14
302	faba7055-5a83-47ac-b5d7-af85cc91abd7	1	9
303	ffcc3007-8241-4e71-8d76-0450e0ff4b3a	1	9
\.


--
-- Name: futureworldstate_id_seq; Type: SEQUENCE SET; Schema: public; Owner: office
--

SELECT pg_catalog.setval('futureworldstate_id_seq', 304, true);


--
-- Data for Name: goals; Type: TABLE DATA; Schema: public; Owner: office
--

COPY goals (id, person, relation) FROM stdin;
1	0	3
2	1	4
\.


--
-- Data for Name: items; Type: TABLE DATA; Schema: public; Owner: office
--

COPY items (id, name) FROM stdin;
2	Coffee Machine
3	Coffee
\.


--
-- Data for Name: locationtraits; Type: TABLE DATA; Schema: public; Owner: office
--

COPY locationtraits (id, name) FROM stdin;
4	Cubicle
\.


--
-- Data for Name: paths; Type: TABLE DATA; Schema: public; Owner: office
--

COPY paths (id, prev) FROM stdin;
568f76a5-052b-4a3a-b6be-d54304f39fd8	\N
0c3b9e39-a4fc-4b1d-8757-9661cfb290db	568f76a5-052b-4a3a-b6be-d54304f39fd8
db04acff-8c33-4842-8636-cb23d52521f5	0c3b9e39-a4fc-4b1d-8757-9661cfb290db
632c6082-20c3-4752-8f50-3503cbfef189	db04acff-8c33-4842-8636-cb23d52521f5
0913c9ef-3c59-4156-8527-54345aa1122a	632c6082-20c3-4752-8f50-3503cbfef189
ffcc3007-8241-4e71-8d76-0450e0ff4b3a	faba7055-5a83-47ac-b5d7-af85cc91abd7
faba7055-5a83-47ac-b5d7-af85cc91abd7	d0dcdd0f-3a18-449a-a237-aae4fd676530
d0dcdd0f-3a18-449a-a237-aae4fd676530	c72ba7eb-93f5-4e03-bdf0-94db7a363480
c72ba7eb-93f5-4e03-bdf0-94db7a363480	c691d6ed-932b-4ea9-a9c3-d50e1a011f42
c691d6ed-932b-4ea9-a9c3-d50e1a011f42	c6534fa4-80be-4832-98c9-cec116af2806
c6534fa4-80be-4832-98c9-cec116af2806	c153353b-3f50-4a03-8496-1ae4b737cd68
c153353b-3f50-4a03-8496-1ae4b737cd68	b82f35ff-f750-43b0-a435-c8625cacbf4f
b82f35ff-f750-43b0-a435-c8625cacbf4f	b3d55298-664d-4e68-9790-956d855dd73c
b3d55298-664d-4e68-9790-956d855dd73c	ad8f712a-9be4-4f3a-af54-4c7684f4dc05
ad8f712a-9be4-4f3a-af54-4c7684f4dc05	ab712442-8e2d-4996-88ff-c1f1a08e4536
ab712442-8e2d-4996-88ff-c1f1a08e4536	a95442be-2c90-4abf-b32c-ba687ab84fc8
a95442be-2c90-4abf-b32c-ba687ab84fc8	a6fd110b-7bb1-4132-a142-bce19704e85f
a6fd110b-7bb1-4132-a142-bce19704e85f	a2bd45fb-0d46-428b-96c1-9f0d9a67111e
a2bd45fb-0d46-428b-96c1-9f0d9a67111e	93b4389f-27eb-4e64-bbd6-a90e02457c96
93b4389f-27eb-4e64-bbd6-a90e02457c96	8229ef13-22d9-4a19-81d7-bce80e56c2d8
8229ef13-22d9-4a19-81d7-bce80e56c2d8	7da62871-b649-452f-b413-280a25e34f02
7da62871-b649-452f-b413-280a25e34f02	7a4138d9-12e1-4f11-a97d-bb21851f5665
7a4138d9-12e1-4f11-a97d-bb21851f5665	768d749c-1608-45c1-9044-cf03e7da293b
768d749c-1608-45c1-9044-cf03e7da293b	6fc8440c-85e7-495f-b7f1-26fe35934332
6fc8440c-85e7-495f-b7f1-26fe35934332	68de56fa-b411-4532-a933-0b51bd749317
68de56fa-b411-4532-a933-0b51bd749317	5fee1fa4-96bd-48b4-a0f7-46f1eb0caa99
5fee1fa4-96bd-48b4-a0f7-46f1eb0caa99	586d4e35-3947-47de-9b30-ec2fa7da57bc
586d4e35-3947-47de-9b30-ec2fa7da57bc	577891e0-9901-45e4-81fe-8fd53d13659f
577891e0-9901-45e4-81fe-8fd53d13659f	4d56e0c2-f737-4a94-90b5-d6d431eaee06
4d56e0c2-f737-4a94-90b5-d6d431eaee06	3fb3a67f-1b66-45d6-ba39-82e85a99986e
3fb3a67f-1b66-45d6-ba39-82e85a99986e	3cc4c68e-feea-43e2-b587-385cfb6e3388
3cc4c68e-feea-43e2-b587-385cfb6e3388	2f59fce3-676f-46b1-9215-bca5923d1b07
2f59fce3-676f-46b1-9215-bca5923d1b07	2c57ceea-6ea8-4935-8baf-512221057058
2c57ceea-6ea8-4935-8baf-512221057058	29bb79c8-f6d3-4e29-bef1-d9fec63ae8f1
29bb79c8-f6d3-4e29-bef1-d9fec63ae8f1	258d3127-719d-4c90-9f5c-bcb0277643d3
258d3127-719d-4c90-9f5c-bcb0277643d3	0913c9ef-3c59-4156-8527-54345aa1122a
\.


--
-- Data for Name: persons; Type: TABLE DATA; Schema: public; Owner: office
--

COPY persons (id, name) FROM stdin;
0	Coffee Guy
1	Hardworking Guy
\.


--
-- Data for Name: positions; Type: TABLE DATA; Schema: public; Owner: office
--

COPY positions (id, x, y) FROM stdin;
7	7	7
8	3	9
6	5	5
9	6	7
14	6	5
13	8	7
16	5	6
11	7	6
12	5	4
15	7	8
10	4	5
\.


--
-- Name: positions_id_seq; Type: SEQUENCE SET; Schema: public; Owner: office
--

SELECT pg_catalog.setval('positions_id_seq', 1, true);


--
-- Data for Name: relations; Type: TABLE DATA; Schema: public; Owner: office
--

COPY relations (id, relationtype, object, subject) FROM stdin;
3	1	0	3
4	1	1	3
5	2	2	8
6	2	0	3
7	2	3	8
1	2	0	6
2	2	1	7
8	2	1	9
13	2	0	14
12	2	1	13
15	2	0	16
10	2	1	11
11	2	0	12
14	2	1	15
9	2	0	10
\.


--
-- Data for Name: relationtypes; Type: TABLE DATA; Schema: public; Owner: office
--

COPY relationtypes (id, name) FROM stdin;
1	Has
2	At
3	Same Location
\.


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

