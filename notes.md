

Rules()

# a goal is just a record containing (string, int)
Goal {
    FULLNESS : int,
    SANITY: int,
    SAFETY: int,
    REST: int,
    MONEY: int,
    GLOBAL_HUMAN_RELATIONSHIPS: int
}

we can represent goals as :
Relations("GOAL", personA, HUNGER, 50) where this means personA has goal of >50 hunger etc...

Represent each goal-type as a single value:
   * this is so even for cases where there are multiple possible relations,
     we need to compute a single numerical value for it


Goals

relations




<hr>

Action = (ActionName, Requirements, Result)
ActionName = ["Take", "Walk"]
Action(id INT PRIMARY KEY)

ActionRequirements(actionId, requirementId)
ActionResults(actionId, relations)


Relations = (RelationType, Object, Subject)

Object = Person | Item | Position | LocationTrait
Subject = Person | Item | Position | LocationTrait

RelationType = Has(Thing, LocationTrait) | Has(Thing, Item) | At(Person, Position)
              | Has(Person, Commodity)

Thing = Person | Position

Requirement = Item | LocationTrait | HumanRelationship
Requirement(id INT PRIMARY KEY)

LocationTrait = ['Cubicle', 'Pantry']
Item = ['Coffee Machine', 'Coffee']
HumanRelationship(id INT PRIMARY KEY, object INT REFERENCES Person, subject INT REFERENCES Person, type REFERENCES HumanRelationshipType, intensity INT)
HumanRelationshipType = ['friend', 'supervisor', 'overlord']


Position = (id, x INT, y INT)

CurrentWorldState = ()


FutureWorldState = (stepNumber INT, relation) # use for computation
ActionsLeadingToFutureWorldState = (stepNumber INT, actionId)

def canPerformAction(actionId):
    """
    If the currentWorldState contains the relations required 
    """
    SELECT * FROM Actions action
        
        WHERE 
        (SELECT COUNT(DISTINCT requirement.relation) FROM 
            ActionRequirements requirement
            WHERE requirement.action = action.id 
            ) 
        =
        (SELECT COUNT(DISTINCT Relations.id) FROM 
            Relations 
            JOIN CurrentWorldState ON CurrentWorldState.relation = Relations.id
            JOIN ActionRequirements requirement ON requirement.action = action.id 
            WHERE ActionRequirements.relation = Relations.id)


def takeAction(actionId):
    # assuming satisfiable requirement

# actionResultType, actionResultValue, actor, timestep
CREATE FUNCTION takeAction(integer, integer) RETURNS void as $$
    BEGIN TRANSACTION;

    SELECT dispatchActionResults($1, integer, integer, integer);
    COMMIT;
$$ LANGUAGE SQL;

def takeActionSingle:
    #consume relations from current world state


    SELECT DISTINCT results.relation
    FROM Actions a, ActionResults results
    JOIN ActionRequirements ar ON ar.action = a.id
    AND  
      (
        SELECT COUNT(DISTINCT requirement.relation) FROM 
          ActionRequirements requirement
          WHERE requirement.action = a.id 
      ) 
      =
      (
        SELECT COUNT(DISTINCT CurrentWorldState.relation) FROM 
          CurrentWorldState 
          JOIN ActionRequirements requirement ON requirement.action = a.id 
          WHERE requirement.relation = CurrentWorldState.relation
      )
    WHERE results.action = a.id AND a.id=$1





def isRequirementsSatifiable(actionId, maxStep):
    canPerform(actionId, WorldState at maxStep)

def hasDesiredRelation(relationId, timestep):
    SELECT CASE WHEN EXISTS (
      SELECT * FROM ForwardChain
      WHERE timestep = ${timestep} AND relation = ${relationId}
    )
    THEN CAST(1 AS BIT)
    ELSE CAST(0 AS BIT) END

    
def rankActions:



def executableActions:


def move = 
SELECT * FROM Relations r JOIN RelationsType rt ON rt.name='At' AND rt.id = r.relationType
WHERE timestep = ${timestep}





update_state =
at time 0, current state
at time t, 


JOIN FutureWorldState t ON 

use recursive CTE

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

Create the world!

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

CREATE TABLE Positions(id INT PRIMARY KEY REFERENCES Entities(id), x INT, Y INT, UNIQUE(X, Y));
INSERT INTO Positions(x,y) VALUES(5,5);

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


...
RelationType = Has(Thing, LocationTrait) | Has(Thing, Item) | At(Person, Position)
              | Has(Person, Commodity)
INSERT INTO RelationTypes 
  VALUES()

INSERT INTO Entities VALUES(6); INSERT INTO Positions VALUES(6,5,5);
INSERT INTO Entities VALUES(7); INSERT INTO Positions VALUES(7,7,7);

CREATE TABLE CurrentWorldState(id INT PRIMARY KEY, relation INT, UNIQUE(relation));
CREATE TABLE FutureWorldState(id SERIAL PRIMARY KEY, timestep INT, relation INT, UNIQUE(timestep, relation));




The requirements of `walk` will be that Relations('At', Person, _) exists
`WHERE rt.name = 'At' AND relationType = rt.id AND (subject IS NULL OR subject=${personId2}) AND object = ${personId}`



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


  

# x, y
CREATE FUNCTION insertNewPosition(integer, integer) RETURNS integer AS $$
  INSERT INTO Relations (id, relationType, object, subject) 
      VALUES( (SELECT MAX(id) + 1 FROM Relations), (SELECT id FROM RelationTypes WHERE name = 'At'), $1, $2) 
      ON CONFLICT DO NOTHING 

      RETURNING id;
$$ LANGUAGE SQL;

# x, y, Direction
CREATE FUNCTION relationAfterWalking(integer, integer, integer) RETURNS integer AS $$
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
$$ LANGUAGE SQL;


# timestep, x, y, Direction
CREATE FUNCTION walk (integer, integer, integer, integer) RETURNS void AS $$
    INSERT INTO FutureWorldState VALUES($1 + 1, relationAfterWalking($2, $3, $4)) 
    ON CONFLICT DO NOTHING;
$$ LANGUAGE SQL;


<!-- # timestep, relation, delta
CREATE FUNCTION changeRelationIntensity(integer, integer, integer) RETURNS void AS $$
  UPSERT INTO FutureWorldState VALUES($1 + 1, )

$$ LANGUAGE SQL;
 -->

# entityId, timestep
# returns id of Positions
CREATE OR REPLACE FUNCTION positionOfActorAtTimestep(integer, integer) RETURNS integer AS $$
  SELECT Positions.id FROM Positions JOIN FutureWorldState ON FutureWorldState.timestep = $2
      JOIN Relations ON Relations.id = FutureWorldState.relation AND Relations.object = $1 
                        AND Relations.subject=Positions.id
      JOIN RelationTypes ON RelationTypes.id = Relations.relationType AND RelationTypes.name = 'At'
$$ LANGUAGE SQL;


CREATE FUNCTION makeFutureRelation(integer, integer) RETURNS void AS $$
  INSERT INTO FutureWorldState VALUES($1 + 1, $2) ON CONFLICT DO NOTHING;
$$ LANGUAGE SQL;

# actionResultType, actionResultValue, actor, timestep
# $1=0 is walk, $1=1 is update relation
CREATE FUNCTION dispatchActionResults(integer, integer, integer, integer) RETURNS void AS $$
  SELECT 
    CASE WHEN $1=0 THEN
        (SELECT walk($3, (SELECT x FROM Positions WHERE id = (SELECT positionOfActorAtTimestep($3, $4))),
                        (SELECT y FROM Positions WHERE id = (SELECT positionOfActorAtTimestep($3, $4))),
                        $2))
       WHEN $1=1 THEN
         (SELECT makeFutureRelation($1, $2))
  END
$$ LANGUAGE SQL;


#### goals


CREATE TABLE Goals(id INTEGER PRIMARY KEY, person INTEGER REFERENCES Persons(id), relation INTEGER REFERENCES Relations(id));

INSERT INTO Relations VALUES(3, 1, 0, 3);
INSERT INTO Goals VALUES(1, 0, 3);

INSERT INTO Relations VALUES(4, 1, 1, 3);
INSERT INTO Goals VALUES(2, 1, 4);

INSERT INTO Entities VALUES(8);
INSERT INTO Positions VALUES(8, 3, 9);

# coffee at (2, 8)
INSERT INTO Relations VALUES(5, 2, 2, 8); 

# action 1 ('take coffee') produces a new relation 
INSERT INTO ActionResults VALUES(4, 1, 1, 4);

CREATE TABLE ActionRequirementsTypes(id INT PRIMARY KEY, name TEXT);
CREATE TABLE ActionRequirements(id INT PRIMARY KEY, action INT REFERENCES Actions(id), requirementType INT REFERENCES ActionRequirementsTypes(id), relation INT REFERENCES Relations(id), UNIQUE(action, relation));

INSERT INTO ActionRequirementsTypes VALUES(0, 'same location');
INSERT INTO ActionRequirementsTypes VALUES(1, 'relations');
INSERT INTO ActionRequirements VALUES(0, 1, 0, 6);


# actor1, actor2, timestep
CREATE FUNCTION sameLocation(integer, integer, integer) RETURNS BOOLEAN AS $$
   SELECT (SELECT positionOfActorAtTimestep($1, $3)) = (SELECT positionOfActorAtTimestep($2, $3));
  
$$ LANGUAGE SQL;

### 

# 
INSERT INTO Relations VALUES(6, 2, 0, 3);

# $1 = actionRequirementsType, action, object, subject, timestep
CREATE OR REPLACE FUNCTION dispatchActionRequirements(integer, integer, integer, integer, integer) RETURNS BOOLEAN AS $$
  SELECT
    CASE WHEN $1 = 0 THEN
      (SELECT sameLocation($3, $4, $5))
    ELSE 
      (SELECT (SELECT COUNT(req.relation) FROM ActionRequirements req WHERE req.action=$2)
              = 
              (SELECT COUNT(req.relation) FROM ActionRequirements req 
                      JOIN FutureWorldState world ON world.relation=req.relation AND world.timestep = $5
                      WHERE req.action=$2)
      )
    END;

$$ LANGUAGE SQL;

# action, actor, subject, timestep
CREATE FUNCTION dispatchActionRequirements(integer, integer, integer, integer) RETURNS BOOLEAN AS $$
  SELECT dispatchActionRequirements(
    (SELECT requirementType FROM ActionRequirements WHERE action = $1),
    $1,
    $2,
    $3,
    $4
  )
$$ LANGUAGE SQL;

# timestep, relation
<!-- CREATE OR REPLACE FUNCTION canReach(integer, integer) RETURNS BOOLEAN AS $$
  WITH RECURSIVE ForwardChain AS (
      SELECT 0, relation 
      FROM CurrentWorldState
    UNION 
      SELECT  fc.timestep + 1, 
              dispatchActionResults(
                  PossibleResults.type, PossibleResults.value,
                  PossibleResults.person, fc.timestep
              )
      FROM ForwardChain fc
      INNER JOIN (
          SELECT results.relation, results.type, p.id AS person
          FROM Actions a
          CROSS JOIN Persons p
          
          JOIN Relations rr ON ar.relation = rr.id
          JOIN ActionRequirements ar ON 
             ar.action = a.id
              AND  
              (SELECT dispatchActionRequirements(a.id, p.id, rr.subject, tempFc.time) = TRUE)
          JOIN ActionResults results ON results.action = a.id
      ) PossibleResults
      ON fc.relation = PossibleResults.relation 
  )
  SELECT CASE WHEN EXISTS (
    SELECT * FROM ForwardChain fc
    WHERE fc.timestep = $1 AND fc.relation = $1
  )
  THEN CAST(1 AS BIT)
  ELSE CAST(0 AS BIT) END
$$ LANGUAGE SQL;

 -->
# timestep
CREATE OR REPLACE FUNCTION createTheFuture(integer) RETURNS void AS $$
  INSERT INTO FutureWorldState (timestep, relation) (SELECT 0, relation FROM CurrentWorldState)
    ON CONFLICT DO NOTHING;

  SELECT (
    SELECT 
      dispatchActionResults(
        PossibleResults.type, PossibleResults.value,
        PossibleResults.person, i
      )
      FROM (
        SELECT results.value, results.type, p.id AS person
          FROM Actions a
          CROSS JOIN Persons p
          CROSS JOIN Relations rr 
          JOIN ActionRequirements ar ON 
             ar.action = a.id
             AND ar.relation = rr.id
             AND (SELECT dispatchActionRequirements(a.id, p.id, rr.subject, i) = TRUE)
          JOIN ActionResults results ON results.action = a.id
      ) PossibleResults
  )
  FROM generate_series(1, $1) AS i

$$ LANGUAGE SQL;


#timestep, relation

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


SELECT results.value, results.type, p.id AS person, a.id, rr.subject
          FROM Actions a
          CROSS JOIN Persons p
          CROSS JOIN Relations rr 
          JOIN ActionRequirements ar ON 
             ar.action = a.id
             AND ar.relation = rr.id
          JOIN ActionResults results ON results.action = ar.action


INSERT INTO Relations VALUES(7, 2, 3, 8);
INSERT INTO CUrrentWorldState VALUES(3, 7);



