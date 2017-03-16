package com.officelife.actions;

import com.officelife.actions.prerequisite.LocationBeside;
import com.officelife.actors.Actor;
import com.officelife.goals.State;
import com.officelife.items.Coffee;
import com.officelife.items.Item;

import java.util.Optional;

/**
 *
 * TODO make generic GiveItem
 */
public class GiveFood extends Action {

  private final Actor target;

  public GiveFood(State state, Actor target) {
    super(state);
    this.target = target;
  }

  @Override
  public boolean accept() {
    LocationBeside prereq = new LocationBeside(target, state.actor, state.world);
    if (!prereq.satisfied())  {
      return false;
    }

    if (state.actor.inventory().stream().noneMatch(item -> item instanceof Coffee)) {
      return false;
    }

    Optional<Item> itemToRemove = state.actor.inventory().stream()
            .filter(item -> item instanceof Coffee)
            .findFirst();

    Item item = itemToRemove.orElseThrow(() -> new RuntimeException("Unable to get item from inventory"));
    state.actor.inventory().remove(item);

    target.addItem(item);
    return true;
  }

  @Override
  public String toString() {
    return String.format("Give Food to Person %s", target.id());
  }
}
