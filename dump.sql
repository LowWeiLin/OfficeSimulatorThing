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
        )), 
        PossiblePathResults.pathId, PossiblePathResults.newPathId
      FROM (
        SELECT 
            PossibleResults.pathId, 
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
                                          AND rr.object = p.id
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
153
154
155
156
157
158
159
160
161
162
163
164
165
166
167
168
169
170
171
172
173
174
175
176
177
178
179
180
181
182
183
184
185
186
187
188
189
190
191
192
193
194
195
196
197
198
199
200
201
202
203
204
205
206
207
208
209
210
211
212
213
214
215
216
217
218
219
220
221
222
223
224
225
226
227
228
229
230
231
232
233
234
235
236
237
238
239
240
241
242
243
244
245
246
247
248
249
250
251
252
253
254
255
256
257
258
259
260
261
262
263
264
265
266
267
268
269
270
271
272
273
274
275
276
277
278
279
280
281
282
283
284
285
286
287
288
289
290
291
292
293
294
295
296
297
298
299
300
301
302
303
304
305
306
307
308
309
310
311
312
313
314
315
316
317
318
319
320
321
322
323
324
325
326
327
328
329
330
331
332
333
334
335
336
337
338
339
340
341
342
343
344
345
346
347
348
349
350
351
352
353
354
355
356
357
358
359
360
361
362
363
364
365
366
367
368
369
370
371
372
373
374
375
376
377
378
379
380
381
382
383
384
385
386
387
388
389
390
391
392
393
394
395
396
397
398
399
400
401
402
403
404
405
406
407
408
409
410
411
412
413
414
415
416
417
418
419
420
421
422
423
424
425
426
427
428
429
430
431
432
433
434
435
436
437
438
439
440
441
442
443
444
445
446
447
448
449
450
451
452
453
454
455
456
457
458
459
460
461
462
463
464
465
466
467
468
469
470
471
472
473
474
475
476
477
478
479
480
\.


--
-- Data for Name: futureworldstate; Type: TABLE DATA; Schema: public; Owner: office
--

COPY futureworldstate (id, pathid, timestep, relation) FROM stdin;
597	c05236fc-faf5-4dcb-9a39-d00657d2aa07	0	5
598	6019e32e-7e1d-4b35-8a2e-b8c52361f3ef	0	7
599	feaebc55-1ec4-4652-a187-3d18aafffe53	0	1
600	19c63907-7e34-41dc-83c1-ac2f8eefcfc2	0	2
601	2e0b0377-1d8d-4913-a772-8ed3b069905c	1	14
602	a8a320d1-5cdf-4654-b1c4-903a336b231b	1	10
603	d6f3095d-f700-4810-8be4-77407b29c31c	1	8
604	e11586c5-ae87-4de8-a005-c337ad57ea7d	1	12
605	28f699e8-df20-4961-b9f7-25198762424a	1	13
606	3a3b6e58-498d-400f-8bac-e6f14410adb6	1	9
607	6a28f8db-9871-4189-ba8f-3005c9b140cf	1	15
608	a9f55b9a-000e-4ab7-9f42-e538aa241521	1	11
609	06aea9b1-ca15-4913-b958-32d136465dc5	2	1
610	191c9570-9206-435f-9384-dc9cdc560d5b	2	22
611	4d3f25ee-899d-449e-b43b-aae2e67df8fe	2	19
612	a2276335-e6b5-426e-ba43-5263532dd5ec	2	23
613	0f9a6cab-451e-4b1d-bec3-3b84ef119d43	2	2
614	5b48100c-8368-432a-9c06-70766697d8ee	2	24
615	b058606e-501d-4e20-ad0d-ea3cd556f872	2	25
616	e4667e33-e273-49c6-9622-ce2336291467	2	18
617	34544295-9e76-4be3-9003-a71b23fdf14c	2	17
618	5d07d083-1786-491d-a1e7-de0a9256e956	2	1
619	810a4e2c-eb23-4d9f-85e7-1d8eb83c28a5	2	26
620	fab08d9d-9236-4ee6-aa16-808a8cfb5d19	2	27
621	3065efd1-ae34-4b97-80f8-f0a3065f19bd	2	17
622	4811fe27-b756-4455-8f7a-a70a680dec8b	2	20
623	9949fc52-d7be-48b6-956c-7f6354d9aa49	2	1
624	c1a0ed87-07af-4008-9da5-c5020d8fc3d7	2	19
625	289473c5-5996-43de-a895-99b65f352895	2	16
626	40ebaf00-f6e6-4ce4-a530-696ae4cfdde4	2	28
627	64318dd1-2a3c-4abd-bbf4-074e24f96dce	2	29
628	75277521-1f26-4a6a-9494-4e55bb0ff1d2	2	2
629	1a3d1c11-c02f-414c-83ff-0fe66a08f227	2	1
630	2b0e9111-f678-4e98-b2f7-561f09a48a02	2	30
631	6160fdcc-96dd-4ea4-960a-eaab8e786b85	2	26
632	e97f266d-8331-4d68-b745-aaccfeea5e13	2	23
633	7c8dca12-f4c9-4789-b1b5-cf5919611992	2	2
634	83508cc5-d3d7-4a4d-bb35-85f1f1d69def	2	31
635	84dcdc21-a84e-4806-97d9-5e54caa7e52c	2	29
636	e77278f5-ce52-4793-b7e6-e54fdb232a47	2	25
637	22503b00-c901-420c-99eb-8ddb9a246070	2	21
638	2e69235d-f232-43b0-a40e-4e0e8418b5fe	2	16
639	8ba7f783-9d62-4752-a9b2-b1d94b1bc04f	2	2
640	d7ef3706-f9a2-44f3-bc75-9967ed1183e3	2	18
\.


--
-- Name: futureworldstate_id_seq; Type: SEQUENCE SET; Schema: public; Owner: office
--

SELECT pg_catalog.setval('futureworldstate_id_seq', 640, true);


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
c05236fc-faf5-4dcb-9a39-d00657d2aa07	\N
6019e32e-7e1d-4b35-8a2e-b8c52361f3ef	c05236fc-faf5-4dcb-9a39-d00657d2aa07
feaebc55-1ec4-4652-a187-3d18aafffe53	6019e32e-7e1d-4b35-8a2e-b8c52361f3ef
19c63907-7e34-41dc-83c1-ac2f8eefcfc2	feaebc55-1ec4-4652-a187-3d18aafffe53
2e0b0377-1d8d-4913-a772-8ed3b069905c	19c63907-7e34-41dc-83c1-ac2f8eefcfc2
a8a320d1-5cdf-4654-b1c4-903a336b231b	19c63907-7e34-41dc-83c1-ac2f8eefcfc2
d6f3095d-f700-4810-8be4-77407b29c31c	19c63907-7e34-41dc-83c1-ac2f8eefcfc2
e11586c5-ae87-4de8-a005-c337ad57ea7d	19c63907-7e34-41dc-83c1-ac2f8eefcfc2
28f699e8-df20-4961-b9f7-25198762424a	feaebc55-1ec4-4652-a187-3d18aafffe53
3a3b6e58-498d-400f-8bac-e6f14410adb6	feaebc55-1ec4-4652-a187-3d18aafffe53
6a28f8db-9871-4189-ba8f-3005c9b140cf	feaebc55-1ec4-4652-a187-3d18aafffe53
a9f55b9a-000e-4ab7-9f42-e538aa241521	feaebc55-1ec4-4652-a187-3d18aafffe53
06aea9b1-ca15-4913-b958-32d136465dc5	28f699e8-df20-4961-b9f7-25198762424a
191c9570-9206-435f-9384-dc9cdc560d5b	28f699e8-df20-4961-b9f7-25198762424a
4d3f25ee-899d-449e-b43b-aae2e67df8fe	28f699e8-df20-4961-b9f7-25198762424a
a2276335-e6b5-426e-ba43-5263532dd5ec	28f699e8-df20-4961-b9f7-25198762424a
0f9a6cab-451e-4b1d-bec3-3b84ef119d43	2e0b0377-1d8d-4913-a772-8ed3b069905c
5b48100c-8368-432a-9c06-70766697d8ee	2e0b0377-1d8d-4913-a772-8ed3b069905c
b058606e-501d-4e20-ad0d-ea3cd556f872	2e0b0377-1d8d-4913-a772-8ed3b069905c
e4667e33-e273-49c6-9622-ce2336291467	2e0b0377-1d8d-4913-a772-8ed3b069905c
34544295-9e76-4be3-9003-a71b23fdf14c	3a3b6e58-498d-400f-8bac-e6f14410adb6
5d07d083-1786-491d-a1e7-de0a9256e956	3a3b6e58-498d-400f-8bac-e6f14410adb6
810a4e2c-eb23-4d9f-85e7-1d8eb83c28a5	3a3b6e58-498d-400f-8bac-e6f14410adb6
fab08d9d-9236-4ee6-aa16-808a8cfb5d19	3a3b6e58-498d-400f-8bac-e6f14410adb6
3065efd1-ae34-4b97-80f8-f0a3065f19bd	6a28f8db-9871-4189-ba8f-3005c9b140cf
4811fe27-b756-4455-8f7a-a70a680dec8b	6a28f8db-9871-4189-ba8f-3005c9b140cf
9949fc52-d7be-48b6-956c-7f6354d9aa49	6a28f8db-9871-4189-ba8f-3005c9b140cf
c1a0ed87-07af-4008-9da5-c5020d8fc3d7	6a28f8db-9871-4189-ba8f-3005c9b140cf
289473c5-5996-43de-a895-99b65f352895	a8a320d1-5cdf-4654-b1c4-903a336b231b
40ebaf00-f6e6-4ce4-a530-696ae4cfdde4	a8a320d1-5cdf-4654-b1c4-903a336b231b
64318dd1-2a3c-4abd-bbf4-074e24f96dce	a8a320d1-5cdf-4654-b1c4-903a336b231b
75277521-1f26-4a6a-9494-4e55bb0ff1d2	a8a320d1-5cdf-4654-b1c4-903a336b231b
1a3d1c11-c02f-414c-83ff-0fe66a08f227	a9f55b9a-000e-4ab7-9f42-e538aa241521
2b0e9111-f678-4e98-b2f7-561f09a48a02	a9f55b9a-000e-4ab7-9f42-e538aa241521
6160fdcc-96dd-4ea4-960a-eaab8e786b85	a9f55b9a-000e-4ab7-9f42-e538aa241521
e97f266d-8331-4d68-b745-aaccfeea5e13	a9f55b9a-000e-4ab7-9f42-e538aa241521
7c8dca12-f4c9-4789-b1b5-cf5919611992	d6f3095d-f700-4810-8be4-77407b29c31c
83508cc5-d3d7-4a4d-bb35-85f1f1d69def	d6f3095d-f700-4810-8be4-77407b29c31c
84dcdc21-a84e-4806-97d9-5e54caa7e52c	d6f3095d-f700-4810-8be4-77407b29c31c
e77278f5-ce52-4793-b7e6-e54fdb232a47	d6f3095d-f700-4810-8be4-77407b29c31c
22503b00-c901-420c-99eb-8ddb9a246070	e11586c5-ae87-4de8-a005-c337ad57ea7d
2e69235d-f232-43b0-a40e-4e0e8418b5fe	e11586c5-ae87-4de8-a005-c337ad57ea7d
8ba7f783-9d62-4752-a9b2-b1d94b1bc04f	e11586c5-ae87-4de8-a005-c337ad57ea7d
d7ef3706-f9a2-44f3-bc75-9967ed1183e3	e11586c5-ae87-4de8-a005-c337ad57ea7d
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
15	7	8
11	7	6
9	6	7
8	3	9
13	8	7
14	6	5
10	4	5
16	5	6
12	5	4
454	7	9
460	3	5
450	7	5
470	5	3
459	4	4
452	6	4
455	6	8
186	4	6
6	5	5
192	5	7
191	6	6
193	9	7
185	8	6
7	7	7
190	8	8
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
14	2	1	15
10	2	1	11
8	2	1	9
12	2	1	13
13	2	0	14
9	2	0	10
15	2	0	16
11	2	0	12
22	2	0	450
24	2	1	454
27	2	0	460
28	2	1	450
30	2	0	470
26	2	0	459
23	2	0	452
31	2	1	192
29	2	1	191
25	2	1	455
17	2	0	186
20	2	0	192
19	2	0	191
1	2	0	6
21	2	1	193
16	2	1	185
2	2	1	7
18	2	1	190
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

