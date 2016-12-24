
%% World state

person(ben).
person(charles).

location(pantry).
location(cubicle).
location(meeting_room).

adjacent(pantry, cubicle).
adjacent(pantry, meeting_room).
adjacent(meeting_room, board_room).

has(ben, macbook).

at(coffee_cup, pantry).
at(ben, board_room).
at(charles, cubicle).

%% Utility

% Adjacency is symmetric, but not reflexive
is_adjacent(A, B) :-
  adjacent(A, B); adjacent(B, A).

% DFS to check reachability
can_reach(Person, Place) :-
  can_reach(Person, Place, _).

% Returns a path as well
can_reach(Person, Place, [Place|Path]) :-
  once(can_reach_(Person, Place, [Place], Path)).

can_reach_(Person, Place, _, []) :-
  at(Person, Place).
can_reach_(Person, Place, Been, [Adj|Path]) :-
  is_adjacent(Place, Adj),
  \+ member(Adj, Been),
  can_reach_(Person, Adj, [Adj|Been], Path).

can_take(Person, Object) :-
  location(Place),
  at(Object, Place),
  can_reach(Person, Place).

%% Rules

drink_coffee(Person) :-
  person(Person), location(Place),
  (has(Person, coffee_cup); at(coffee_cup, Place), can_take(Person, coffee_cup)).
