package com.officelife.goals.subgoals;

import com.officelife.Coords;
import com.officelife.World;
import com.officelife.actions.*;
import com.officelife.actions.prerequisite.LocationBeside;
import com.officelife.actors.Actor;
import com.officelife.actors.Person;
import com.officelife.goals.Goal;
import com.officelife.goals.Outcome;
import com.officelife.goals.State;
import com.officelife.goals.effects.Effect;
import com.officelife.goals.effects.TerminalAction;
import com.officelife.items.Food;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

// OrGoal
public class ImproveFriendship extends Goal {

  private enum Status {
    INIT_GOAL, FINDING, COMPLETED
  }

  private Status status;

  private boolean failed = false;

  private Person target;

  public ImproveFriendship(Person target) {
    this.target = target;
    this.status = Status.FINDING;
  }

  public ImproveFriendship() {
    this.target = null; // :(
    this.status = Status.INIT_GOAL;
  }

  @Override
  public Outcome outcome() {
    if (failed) {
      return Outcome.FAILURE;
    }

    if (status == Status.COMPLETED) {
      return Outcome.SUCCESS;
    }

    return Outcome.CONTINUE;
  }

  @Override
  public Effect effect(State state) {
    switch (status) {
      case INIT_GOAL:
        // search the map. return move action

        Coords personCoords = state.world.actorLocation(state.person)
                .orElseThrow(() -> new RuntimeException("Actor not found"));
        List<Actor> nearbyActors = nearbyActors(state, personCoords);

        List<Person> nearbyPersons = nearbyActors.stream()
                .filter(actor -> actor instanceof Person)
                .filter(actor -> !actor.id().equals(state.person.id()))
                .map(actor -> (Person) actor)
                .collect(Collectors.toList());

        if (nearbyPersons.isEmpty()) {
          failed = true;
          return new TerminalAction(new Languish(state));
        }
        Person targetPerson = nearbyPersons.get(ThreadLocalRandom.current().nextInt(0, nearbyPersons.size()));

        Coords currentCoords = state.world.actorLocation(state.person)
                .orElseThrow(() -> new RuntimeException("person " + state.person.id() + " is nowhere"));

        Optional<List<Coords>> path = state.world.actorLocation(targetPerson)
                .map(coords -> state.world.findPath(currentCoords, new World.EndCoords(coords)));

        if (!path.isPresent()) {
          failed = true;
          return new TerminalAction(new Languish(state));
        }

        status = Status.FINDING;
        target = targetPerson;

        return new TerminalAction(
          new Move(state, Move.Direction.directionToMove(currentCoords, path.get().get(0)))
        );

      case FINDING:


        Optional<List<Coords>> pathToTarget = state.world.actorLocation(target)
                .map(coords ->
                        state.world.findPath(state.world.actorLocation(state.person).get(), new World.EndCoords(coords))
                );

        if (!pathToTarget.isPresent()) {
          failed = true;
          return new TerminalAction(new Languish(state));
        }
        if (new LocationBeside(state.person, target, state.world)
                .satisfied()) {
          status = Status.COMPLETED;

          // TODO perform decision making in another class
          if (target.energy < 101 &&
                  state.person.inventory.stream().anyMatch(item -> item instanceof Food)) {
            return new TerminalAction(new GiveFood(state, target));
          }
          return new TerminalAction(new Talk(state, target));
        }

        return new TerminalAction(
            new Move(
              state,
              Move.Direction.directionToMove(
                state.world.actorLocation(state.person).get(), pathToTarget.get().get(0)
              )
            )
        );


      default:
        return new TerminalAction(new Languish(state));
    }
  }

  private List<Actor> nearbyActors(State state, Coords personCoords) {
    List<Actor> nearby = new ArrayList<>();
    for (int i = personCoords.x - 2; i < personCoords.x + 2; i++) {
      for (int j = personCoords.y - 2; j < personCoords.y + 2; j++) {
        Coords coords = new Coords(i, j);
        if (state.world.actorLocations.containsKey(new Coords(i, j))) {
          nearby.add(state.world.actorLocations.get(coords));
        }
      }
    }
    return nearby;
  }
}

