package com.officelife.goals.subgoals;

import java.util.List;
import java.util.Optional;

import com.officelife.Coords;
import com.officelife.World;
import com.officelife.actions.ConsumeItem;
import com.officelife.actions.Move;
import com.officelife.actions.TakeItem;
import com.officelife.goals.Goal;
import com.officelife.goals.State;
import com.officelife.actions.Languish;
import com.officelife.goals.effects.Effect;
import com.officelife.goals.effects.TerminalAction;
import com.officelife.items.Coffee;

public class GetCoffee extends Goal {

  private enum Status {
    NOT_FOUND, FOUND, CONSUMED
  }

  private Status status = Status.NOT_FOUND;

  private boolean failed = false;

  @Override
  public boolean hasFailed() {
    return failed;
  }

  @Override
  public boolean hasSucceeded() {
    return status == Status.CONSUMED;
  }

  @Override
  public Effect effect(State state) {
    switch (status) {
      case NOT_FOUND:
        // search the map. return move action
        Optional<Coords> currentCoords = state.world.actorLocation(state.person.id());

        if (!currentCoords.isPresent()) {
          throw new RuntimeException("person " + state.person.id() + " is nowhere");
        }

        Optional<List<Coords>> path =
          state.world.itemLocation(i -> i instanceof Coffee).map(coffee ->
            state.world.findPath(currentCoords.get(), new World.EndCoords(coffee)));

        if (!path.isPresent()) {
          failed = true;
          return new TerminalAction(new Languish(state));
        }

        if (path.get().isEmpty()) {
          status = Status.FOUND;
          return new TerminalAction(new TakeItem<>(state, Coffee.class));
        }
        return new TerminalAction(new Move(state, Move.Direction.directionToMove(currentCoords.get(), path.get().get(0))));
      case FOUND:
        if (state.person.inventory.stream().anyMatch(i -> i instanceof Coffee)) {
          status = Status.CONSUMED;
          return new TerminalAction(new ConsumeItem<>(state, Coffee.class));
        } else {
          failed = true;
          return new TerminalAction(new Languish(state));
        }
      default:
        return new TerminalAction(new Languish(state));
    }
  }
}
