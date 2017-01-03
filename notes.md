

"""
ForwardChain gives possible states
"""
WITH RECURSIVE ForwardChain AS (
  SELECT 0, relation 
  FROM CurrentWorldState
  UNION 
  SELECT fc.timestep + 1, relation
  FROM ForwardChain fc
  INNER JOIN (
      SELECT DISTINCT results.relation
      FROM Action a
      JOIN ActionRequirements ar ON ar.action = a.id
      AND  
        (
          SELECT COUNT(DISTINCT requirement.relation) FROM 
            ActionRequirements requirement
            WHERE requirement.action = a.id 
        ) 
        =
        (
          SELECT COUNT(DISTINCT fc.relation) FROM 
            ForwardChain fc2 
            JOIN ActionRequirements requirement ON requirement.action = a.id 
            WHERE requirement.relation = fc2.relation AND fc2.timestep = fc.timestep
        )
      JOIN ActionResults results ON results.action = a.id
  ) PossibleResults
  ON fc.relation = PossibleResults.relation
)
SELECT CASE WHEN EXISTS (
  SELECT * FROM ForwardChain
  WHERE timestep = ${timestep} AND relation = ${relationId}
)
THEN CAST(1 AS BIT)
ELSE CAST(0 AS BIT) END


CREATE FUNCTION relationIdOf(integer, integer, integer) RETURNS integer AS $$
  SELECT id FROM Relations WHERE relationType = $1 AND object = $2 AND subject = $3;
$$ LANGUAGE SQL;

CREATE TABLE Entities(id INT PRIMARY KEY);

CREATE TABLE Persons(id INT PRIMARY KEY REFERENCES Entities(id), name TEXT);

INSERT INTO Entities VALUES(0);
INSERT INTO Persons VALUES(0, 'Coffee Guy');
INSERT INTO Entities VALUES(1);
INSERT INTO Persons VALUES(1, 'Hardworking Guy');

CREATE TABLE Items(id INT PRIMARY KEY REFERENCES Entities(id), name TEXT);

INSERT INTO Entities VALUES(2);
INSERT INTO Items VALUES(2, 'Coffee Machine');
INSERT INTO Entities VALUES(3);
INSERT INTO Items VALUES(3, 'Coffee');

CREATE TABLE Positions(id SERIAL PRIMARY KEY REFERENCES Entities(id), x INT NOT NULL, Y INT NOT NULL, UNIQUE(X, Y));
INSERT INTO Positions VALUES(6, 5,5);

CREATE TABLE LocationTraits(id INT PRIMARY KEY REFERENCES Entities(id), name TEXT);
INSERT INTO Entities VALUES(4);
INSERT INTO LocationTraits VALUES(4, 'Cubicle');

CREATE TABLE Commodities(id INT PRIMARY KEY REFERENCES Entities(id), name TEXT);
INSERT INTO Entities VALUES(5);
INSERT INTO Commodities VALUES(5, 'Food');

CREATE TABLE RelationTypes(id INT PRIMARY KEY, name TEXT);
CREATE TABLE Relations(id INT PRIMARY KEY, relationType INT REFERENCES RelationTypes(id), object INT REFERENCES Entities(id), subject INT REFERENCES Entities(id), UNIQUE(relationType, object, subject));


INSERT INTO RelationTypes VALUES(1, 'Has');
INSERT INTO RelationTypes VALUES(2, 'At');
INSERT INTO RelationTypes VALUES(3, 'Same Location');


-- ...
-- RelationType = Has(Thing, LocationTrait) | Has(Thing, Item) | At(Person, Position)
--               | Has(Person, Commodity)
-- INSERT INTO RelationTypes 
--   VALUES()

INSERT INTO Entities VALUES(6); INSERT INTO Positions VALUES(6,5,5);
INSERT INTO Entities VALUES(7); INSERT INTO Positions VALUES(7,7,7);

CREATE TABLE CurrentWorldState(id INT PRIMARY KEY, relation INT, UNIQUE(relation));
CREATE TABLE FutureWorldState(id SERIAL PRIMARY KEY, pathId UUID, timestep INT, relation INT, UNIQUE(pathId, timestep));




-- The requirements of `walk` will be that Relations('At', Person, _) exists
-- `WHERE rt.name = 'At' AND relationType = rt.id AND (subject IS NULL OR subject=${personId2}) AND object = ${personId}`



CREATE TABLE Actions(id INT PRIMARY KEY, name TEXT);
INSERT INTO Actions VALUES(0, "Drink Coffee");

CREATE TABLE ActionResultsType(id INT PRIMARY KEY, name TEXT);
CREATE TABLE ActionResults(id INT PRIMARY KEY, action INT REFERENCES Actions(id), type INT REFERENCES ActionResultsType(id), value INT);

INSERT INTO ActionResultsType VALUES(0, 'movement');
INSERT INTO ActionResultsType VALUES(1, 'relations');


INSERT INTO Actions VALUES(1, 'take coffee');
INSERT INTO Actions VALUES(2, 'walk left');
INSERT INTO Actions VALUES(3, 'walk up');
INSERT INTO Actions VALUES(4, 'walk right');
INSERT INTO Actions VALUES(5, 'walk down');

INSERT INTO ActionResults VALUES(0, 1, 1, 3);
INSERT INTO ActionResults VALUES(1, 2, 0, 1);
INSERT INTO ActionResults VALUES(2, 3, 0, 2);
INSERT INTO ActionResults VALUES(3, 4, 0, 3);
INSERT INTO ActionResults VALUES(4, 5, 0, 4);


  

-- id, x, y
CREATE OR REPLACE FUNCTION insertNewPosition(integer, integer, integer) RETURNS integer AS $$
  INSERT INTO Positions VALUES($1, $2, $3)
  ON CONFLICT(x,y) DO UPDATE SET x=$2
  RETURNING id;
$$ LANGUAGE SQL;

-- x, y
CREATE OR REPLACE FUNCTION insertNewPosition(integer, integer) RETURNS integer AS $$
  INSERT INTO Entities (SELECT MAX(id) + 1 FROM Entities);
  SELECT insertNewPosition(
    (SELECT MAX(id) FROM Entities),
    $1, 
    $2
  );
$$ LANGUAGE SQL;

-- actor, x, y, Direction
CREATE OR REPLACE FUNCTION relationAfterWalking(integer, integer, integer, integer) RETURNS integer AS $$
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
$$ LANGUAGE SQL;


-- pathId , actor,timestep, x, y, Direction
CREATE OR REPLACE FUNCTION walk (uuid, integer, integer, integer, integer, integer) RETURNS int AS $$
    INSERT INTO FutureWorldState(pathId, timestep, relation) VALUES($1, $3 + 1, relationAfterWalking($2, $4, $5, $6)) 
    ON CONFLICT DO NOTHING
    RETURNING relation;
$$ LANGUAGE SQL;


-- <!-- --timestep, relation, delta
-- CREATE FUNCTION changeRelationIntensity(integer, integer, integer) RETURNS void AS $$
--   UPSERT INTO FutureWorldState VALUES($1 + 1, )

-- $$ LANGUAGE SQL;
--  -->

-- entityId, timestep, pathId
-- returns id of Positions
-- CREATE OR REPLACE FUNCTION positionOfActorAtTimestep(integer, integer, uuid) RETURNS integer AS $$
--   SELECT Positions.id FROM Positions 
--       JOIN FutureWorldState ON FutureWorldState.timestep = $2
--                             AND FutureWorldState.pathId = $3

--       JOIN Relations ON Relations.id = FutureWorldState.relation AND Relations.object = $1 
--                         AND Relations.subject=Positions.id
--       JOIN RelationTypes ON RelationTypes.id = Relations.relationType AND RelationTypes.name = 'At'
-- $$ LANGUAGE SQL;

-- entityId, timestep, pathId
CREATE OR REPLACE FUNCTION positionOfActorAtTimestep(integer, integer, uuid) RETURNS integer AS $$
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
  
$$ LANGUAGE SQL;



-- pathId, timestep, relation
CREATE OR REPLACE FUNCTION makeFutureRelation(uuid, integer, integer) RETURNS int AS $$
  INSERT INTO FutureWorldState(pathId, timestep, relation) VALUES($1, $2 + 1, $3) ON CONFLICT DO NOTHING
  RETURNING id;
$$ LANGUAGE SQL;

-- pathId, actionResultType, actionResultValue, actor, timestep, oldPathId
-- $2=0 is walk, $2=1 is update relation
CREATE OR REPLACE FUNCTION dispatchActionResults(uuid, integer, integer, integer, integer, uuid) RETURNS integer AS $$

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

$$ LANGUAGE SQL;


----goals


CREATE TABLE Goals(id INTEGER PRIMARY KEY, person INTEGER REFERENCES Persons(id), relation INTEGER REFERENCES Relations(id));

INSERT INTO Relations VALUES(3, 1, 0, 3);
INSERT INTO Goals VALUES(1, 0, 3);

INSERT INTO Relations VALUES(4, 1, 1, 3);
INSERT INTO Goals VALUES(2, 1, 4);

INSERT INTO Entities VALUES(8);
INSERT INTO Positions VALUES(8, 3, 9);

-- coffee at (2, 8)
INSERT INTO Relations VALUES(5, 2, 2, 8); 

-- action 1 ('take coffee') produces a new relation 
INSERT INTO ActionResults VALUES(4, 1, 1, 4);

CREATE TABLE ActionRequirementsTypes(id INT PRIMARY KEY, name TEXT);
CREATE TABLE ActionRequirements(id INT PRIMARY KEY, action INT REFERENCES Actions(id), requirementType INT REFERENCES ActionRequirementsTypes(id), relation INT REFERENCES Relations(id), UNIQUE(action, relation));

INSERT INTO ActionRequirementsTypes VALUES(0, 'same location');
INSERT INTO ActionRequirementsTypes VALUES(1, 'relations');

INSERT INTO Relations VALUES(6, 2, 0, 3);
INSERT INTO ActionRequirements VALUES(0, 1, 0, 6);


-- actor1, actor2, timestep, pathId
CREATE FUNCTION sameLocation(integer, integer, integer, uuid) RETURNS BOOLEAN AS $$
   SELECT (SELECT positionOfActorAtTimestep($1, $3, $4)) = (SELECT positionOfActorAtTimestep($2, $3, $4));
  
$$ LANGUAGE SQL;

----

--


-- $1 = actionRequirementsType, action, object, subject, timestep, pathId
CREATE OR REPLACE FUNCTION dispatchActionRequirements(integer, integer, integer, integer, integer, uuid) 
    RETURNS BOOLEAN AS $$
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

$$ LANGUAGE SQL;

-- action, actor, subject, timestep, pathId
CREATE FUNCTION dispatchActionRequirements(integer, integer, integer, integer, uuid) RETURNS BOOLEAN AS $$
  SELECT dispatchActionRequirements(
    (SELECT requirementType FROM ActionRequirements WHERE action = $1),
    $1,
    $2,
    $3,
    $4,
    $5
  )
$$ LANGUAGE SQL;

-- timestep, relation
-- <!-- CREATE OR REPLACE FUNCTION canReach(integer, integer) RETURNS BOOLEAN AS $$
--   WITH RECURSIVE ForwardChain AS (
--       SELECT 0, relation 
--       FROM CurrentWorldState
--     UNION 
--       SELECT  fc.timestep + 1, 
--               dispatchActionResults(
--                   PossibleResults.type, PossibleResults.value,
--                   PossibleResults.person, fc.timestep
--               )
--       FROM ForwardChain fc
--       INNER JOIN (
--           SELECT results.relation, results.type, p.id AS person
--           FROM Actions a
--           CROSS JOIN Persons p
          
--           JOIN Relations rr ON ar.relation = rr.id
--           JOIN ActionRequirements ar ON 
--              ar.action = a.id
--               AND  
--               (SELECT dispatchActionRequirements(a.id, p.id, rr.subject, tempFc.time) = TRUE)
--           JOIN ActionResults results ON results.action = a.id
--       ) PossibleResults
--       ON fc.relation = PossibleResults.relation 
--   )
--   SELECT CASE WHEN EXISTS (
--     SELECT * FROM ForwardChain fc
--     WHERE fc.timestep = $1 AND fc.relation = $1
--   )
--   THEN CAST(1 AS BIT)
--   ELSE CAST(0 AS BIT) END
-- $$ LANGUAGE SQL;

--  -->

CREATE TABLE Paths(id UUID PRIMARY KEY, prev UUID REFERENCES Paths(id));

-- old id, new id
CREATE OR REPLACE FUNCTION dispatchPathUpdate(uuid, uuid) RETURNS void AS $$
  INSERT INTO Paths VALUES ($1, $2) ON CONFLICT DO NOTHING;
$$ LANGUAGE SQL;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE OR REPLACE FUNCTION initFutureWithCurrent(uuid, integer) RETURNS void AS $$
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

$$ LANGUAGE SQL;

-- how many timesteps
-- assumes that futureWOrldState is empty. TODO assert this
CREATE OR REPLACE FUNCTION createTheFuture(integer) RETURNS bigint AS $$
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
$$ LANGUAGE SQL;


-- insert into ActionRequirements
-- all the movements require a 'At' relation
INSERT INTO Relations VALUES(8, 2, )
INSERT INTO ActionRequirements VALUES (1, 2, );


--timestep, relation

CREATE OR REPLACE FUNCTION canReach(integer, integer) RETURNS BOOLEAN AS $$
  SELECT createTheFuture($1);
  
  SELECT CASE WHEN EXISTS (
    SELECT * FROM FutureWorldState
    WHERE timestep = $1 AND relation = $2
  )
  THEN TRUE
  ELSE FALSE END
  
$$ LANGUAGE SQL;



INSERT INTO CurrentWorldState VALUES(2, 5);


-- <!-- SELECT results.value, results.type, p.id AS person, a.id, rr.subject
--           FROM Actions a
--           CROSS JOIN Persons p
--           CROSS JOIN Relations rr 
--           JOIN ActionRequirements ar ON 
--              ar.action = a.id
--              AND ar.relation = rr.id
--           JOIN ActionResults results ON results.action = ar.action
--  -->

INSERT INTO Relations VALUES(7, 2, 3, 8);
INSERT INTO CUrrentWorldState VALUES(3, 7);

INSERT INTO Relations VALUES(1, 2, 0, 6);
INSERT INTO Relations VALUES(2, 2, 1, 7);

INSERT INTO Positions VALUES(6, 5, 5);

INSERT INTO CurrentWorldState VALUES (0, 1), (1,2);


INSERT INTO ActionRequirementsTypes VALUES (2, 'has relation of type');
INSERT INTO ActionRequirements VALUES (1, 2, 2, 1);
INSERT INTO ActionRequirements VALUES (2, 3, 2, 1);
INSERT INTO ActionRequirements VALUES (3, 4, 2, 1);
INSERT INTO ActionRequirements VALUES (4, 5, 2, 1);


SELECT (
  SELECT id FROM Relations
)




-- clears orphaned relations.... -> schema needs improvement
DELETE FROM Relations WHERE subject NOT IN (SELECT id FROM Positions UNION SELECT id FROM Items UNION SELECT id FROM Persons UNION SELECT id FROM Commodities);


-- view positions
SELECT * FROM CurrentWOrldState JOIN Relations r ON CurrentWorldState.relation = r.id 
                                LEFT JOIN Positions ON r.subject = Positions.id;


CREATE OR REPLACE FUNCTION lastPathId() RETURNS uuid AS $$
    
$$ LANGUAGE SQL;

-- CREATE OR REPLACE FUNCTION closeTimestep() RETURNS void AS $$
  

--     INSERT INTO Paths (
--       SELECT pathId, pathId2 FROM 
--         (Select row_number() OVER (ORDER BY c.id DESC), c.id  AS pathId FROM (SELECT id FROM Paths
--         EXCEPT
--         SELECT prev FROM Paths) c) c 
--       JOIN 
--         (SELECT row_number() OVER (ORDER BY c2.id DESC ), c2.id AS pathId2 FROM (SELECT id FROM Paths
--         EXCEPT
--         SELECT prev FROM Paths) c2 OFFSET 1) c2 
--       ON c.row_number = c2.row_number - 1
      
        
--     ) ON CONFLICT(id) DO UPDATE SET prev = excluded.prev;

-- $$ LANGUAGE SQL;

-- init Future at step 0
SELECT initFutureWithCurrent(uuid_generate_v4(), world.relation) 
  FROM CurrentWorldState world



