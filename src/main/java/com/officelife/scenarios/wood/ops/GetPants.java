package com.officelife.scenarios.wood.ops;

import static com.officelife.core.Action.State.CONTINUE;
import static com.officelife.core.Action.State.FAILURE;
import static com.officelife.core.Action.State.SUCCESS;
import static com.officelife.core.planning.Facts.fact;
import static com.officelife.core.planning.Facts.facts;

import java.util.List;
import java.util.Optional;

import com.officelife.core.Action;
import com.officelife.core.changes.Move;
import com.officelife.core.changes.TakeItem;
import com.officelife.core.planning.Facts;
import com.officelife.core.planning.Node;
import com.officelife.core.planning.Op;
import com.officelife.scenarios.items.Pants;
import com.officelife.utility.Coords;
import com.officelife.utility.EndCoords;

import javaslang.collection.Map;

public class GetPants implements Op<Node> {

  @Override
  public Facts preconditions() {
    return facts(fact("ground", "has", "pants"));
  }

  @Override
  public int weight(Node state) {
    return 0;
  }

  @Override
  public Facts postconditions(Map<String, Object> bindings) {
    return facts(fact("actor", "has", "pants"));
  }

  @Override
  public Action action() {
    return state -> {

      boolean hasAxe = state.actor.inventory().stream()
        .anyMatch(i -> i instanceof Pants);

      if (hasAxe) {
        return SUCCESS;
      }

      boolean exists = state.world.items().stream().anyMatch(i -> i instanceof Pants);

      if (!exists) {
        return FAILURE;
      }

      // Move towards the axe

      // TODO this probably shouldn't return optional?
      Coords coords = state.world.actorLocation(state.actor).get();

      Optional<List<Coords>> path =
        state.world.closestLocation(i -> i instanceof Pants, coords).flatMap(food ->
          state.world.findPath(coords, new EndCoords(food)));

      if (!path.isPresent()) {
        return FAILURE;
      }

      boolean result;
      if (path.get().isEmpty()) {
        // TODO is there still a point to creating these objects?
        result = new TakeItem<>(state, Pants.class).accept();
      } else {
        result = new Move(state, Move.Direction.directionToMove(coords, path.get().get(0))).accept();
      }

      // TODO is there a way to avoid checking for failure every... single... step...
      if (!result) {
        return FAILURE;
      }

      return CONTINUE;
    };
  }
}
