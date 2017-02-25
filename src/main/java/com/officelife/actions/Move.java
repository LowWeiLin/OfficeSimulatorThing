package com.officelife.actions;

import java.util.Optional;

import com.officelife.Coords;
import com.officelife.goals.State;

/**
 * Movement action. Mutates the location of an actor in 4 possible directions.
 */
public class Move extends Action {

  public enum Direction {
    DOWN, UP, LEFT, RIGHT;

    public static Direction directionToMove(Coords from, Coords to) {
      if (to.x > from.x) {
        return Move.Direction.RIGHT;
      } else if (to.x < from.x) {
        return Move.Direction.LEFT;
      } else if (to.y < from.y) {
        return Move.Direction.UP;
      } else if (to.y > from.y) {
        return Move.Direction.DOWN;
      } else {
        throw new RuntimeException("Invalid direction logic from " + from + " to " + to);
      }
    }

  }

    private final Direction direction;


  public Move(State state, Direction direction) {
    super(state);
    this.direction = direction;
  }

  @Override
  public boolean accept() {
    Optional<Coords> old = state.world.actorLocation(state.person.id());
    if (!old.isPresent()) {
      System.err.println(String.format("could not update actor %s location", state.person.id()));
    }
    Coords updated = updatedLocation(old.get());

    if (state.world.actorLocations.containsKey(updated)) {
      // if banging against something
      // do not change location
      return false;
    }

    // directly update the location
    state.world.actorLocations.remove(old.get());
    state.world.actorLocations.put(updated, state.person.id());

    return true;
  }

  private Coords updatedLocation(Coords location) {
    switch (direction) {
      case DOWN:
        return new Coords(location.x, location.y + 1);
      case UP:
        return new Coords(location.x, location.y - 1);
      case LEFT:
        return new Coords(location.x - 1, location.y);
      case RIGHT:
        return new Coords(location.x + 1, location.y);
      default:
        throw new RuntimeException("bad direction " + location);
    }
  }

  @Override
  public String toString() {
    return String.format("Move %s", direction);
  }
}
