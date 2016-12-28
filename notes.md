

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
    BEGIN TRANSACTION;
    INSERT INTO FutureWorldState (SELECT * FROM CurrentWorldState)
    DELETE FROM WorldState WHERE id IN (
        SELECT relation FROM ActionRequirements WHERE action = actionId
    );
    UPSERT INTO WorldState(
        SELECT relation FROM ActionResults WHERE action = actionId
    );
    COMMIT;

def takeActionSingle:
    #consume relations from current world state
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
        SELECT COUNT(DISTINCT CurrentWorldState.relation) FROM 
          CurrentWorldState 
          JOIN ActionRequirements requirement ON requirement.action = a.id 
          WHERE requirement.relation = CurrentWorldState.relation
      )
    JOIN ActionResults results ON results.action = a.id





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
CREATE TABLE Relations(id INT PRIMARY KEY, relationType INT REFERENCES RelationTypes(id), object INT REFERENCES Entities(id), subject INT REFERENCES Entities(id), intensity INT, UNIQUE(relationType, object, subject));


INSERT INTO RelationTypes VALUES(1, 'Has');
INSERT INTO RelationTypes VALUES(2, 'At');


...
RelationType = Has(Thing, LocationTrait) | Has(Thing, Item) | At(Person, Position)
              | Has(Person, Commodity)
INSERT INTO RelationTypes 
  VALUES()

INSERT INTO Entities VALUES(6); INSERT INTO Positions VALUES(6,5,5);
INSERT INTO Entities VALUES(7); INSERT INTO Positions VALUES(7,7,7);

CREATE TABLE CurrentWorldState(id INT PRIMARY KEY, timestep INT, relation INT, UNIQUE(timestep, relation));




The requirements of `walk` will be that Relations('At', Person, _) exists
`WHERE rt.name = 'At' AND relationType = rt.id AND (subject IS NULL OR subject=${personId2}) AND object = ${personId}`



CREATE TABLE Actions(id INT PRIMARY KEY, name TEXT);
INSERT INTO Actions VALUES(0, "Drink Coffee");

CREATE TABLE ActionResultsType(id INT PRIMARY KEY, name TEXT);
CREATE TABLE ActionResults(id INT PRIMARY KEY,  type INT REFERENCES ActionResultsType(id))

# timestep, x, y, Direction
CREATE FUNCTION walk (integer, integer, integer, integer) RETURNS void AS $$
    UPSERT INTO FutureWorldState VALUES($1 + 1, relationAfterWalking($2, $3, $4))
$$ LANGUAGE SQL;

# x, y
CREATE FUNCTION insertNewPosition(integer, integer) RETURNS integer AS $$
  INSERT INTO Relation (id, relationType, object, subject, intensity) 
      VALUES(SELECT MAX(id) + 1 FROM RELATION, $1, $2) 
      ON CONFLICT IGNORE 
      RETURNING id;
$$ LANGUAGE SQL;

# x, y, Direction
CREATE FUNCTION relationAfterWalking(integer, integer, integer) RETURNS integer AS $$
  CASE WHEN $3=1 THEN 
        SELECT insertNewPosition($1 - 1, $2)
       WHEN $3=2 THEN 
        SELECT insertNewPosition($1, $2 - 1)
       WHEN $3=3 THEN
        SELECT insertNewPosition($1+1, $2)
       WHEN $3=4 THEN
        SELECT insertNewPosition($1, $2+1)
       ELSE 0
  END
$$ LANGUAGE SQL;

<!-- # timestep, relation, delta
CREATE FUNCTION changeRelationIntensity(integer, integer, integer) RETURNS void AS $$
  UPSERT INTO FutureWorldState VALUES($1 + 1, )

$$ LANGUAGE SQL;
 -->

# entityId, timestep
# returns id of Positions
CREATE FUNCTION positionOfActorAtTimestep(integer, integer) RETURNS integer AS $$
  SELECT Positions.id FROM Positions JOIN FutureWorldState ON FutureWorldState.timestep = $1 
      JOIN Relations ON Relations.id = FutureWorldState.relation AND Relations.object = $2 
      JOIN RelationTypes ON RelationTypes.id = Relations.relationType AND RelationTypes.name = 'At'
$$ LANGUAGE SQL;

# actionResultType, actionResultValue, actor, timestep
# $1=0 is walk, $1=1 is update relation
CREATE FUNCTION dispatchActionResults(integer, integer, integer, integer) RETURNS void AS $$
  CASE WHEN $1=0 THEN
        SELECT walk($3, SELECT x FROM Positions WHERE id = (SELECT positionOfActorAtTimestep($3, $4)),
                        SELECT y FROM Positions WHERE id = (SELECT positionOfActorAtTimestep($3, $4)),
                        $2)
       ELSE 
        UPSERT INTO FutureWorldState($1 + 1, $2);
  END
$$ LANGUAGE SQL;