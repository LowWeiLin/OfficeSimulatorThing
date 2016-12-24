
:- dynamic person/1.
:- dynamic location/1.
:- dynamic adjacent/2.
:- dynamic has/2.
:- dynamic at/2.

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
has(pantry, coffee_cup).

at(ben, board_room).
at(charles, cubicle).

%% Utility

facts :-
  Relevant = [person, location, adjacent, has, at],
  maplist(listing, Relevant).

% Adjacency is symmetric, but not reflexive
is_adjacent(A, B) :-
  adjacent(A, B); adjacent(B, A).

% DFS to check reachability
can_reach(Person, Place) :-
  can_reach(Person, Place, _).

% Returns a path as well, starting from the next place to go to
can_reach(Person, Place, Path) :-
  once(can_reach_(Person, Place, [Place], Path0)),
  reverse([Place|Path0], [_|Path]).

can_reach_(Person, Place, _, []) :-
  at(Person, Place).
can_reach_(Person, Place, Been, [Adj|Path]) :-
  is_adjacent(Place, Adj),
  \+ member(Adj, Been),
  can_reach_(Person, Adj, [Adj|Been], Path).

can_take(Person, Object, How) :-
  location(Place),
  has(Place, Object),
  can_reach(Person, Place, Path),
  maplist(move_to(Person), Path, Movement),
  append(Movement, [take(Person, Place, Object)], How).

move_to(Person, Place, move(Person, Place)).

%% State changes

update_state([]).
update_state([Action|Rest]) :-
  update_state(Action, Sub, Add),
  maplist(retract, Sub),
  maplist(assert, Add),
  update_state(Rest).

update_state(move(Person, Place), [Sub], [Add]) :-
  Sub =.. [at, Person, _],
  Add =.. [at, Person, Place].

update_state(take(Person, From, Object), [Sub], [Add]) :-
  Sub =.. [has, From, Object],
  Add =.. [has, Person, Object].

%% Rules

drink_coffee(Person, How) :-
  person(Person), location(Place),
  (has(Person, coffee_cup), How = [];
    has(Place, coffee_cup), can_take(Person, coffee_cup, How)).

