package com.officelife.actions;

import java.util.Optional;

import com.officelife.Coords;
import com.officelife.goals.State;
import com.officelife.items.Item;

/**
 * Action for picking up an item
 */
public class TakeItem<T> extends Action {

  // TODO use this to check that the item is of the right type
  private final Class<T> itemClass;

  public TakeItem(State state, Class<T> itemClass) {
    super(state);
    this.itemClass = itemClass;
  }


  @Override
  public boolean accept() {
    // TODO there may be many items on this location?

    Optional<Coords> currentCoords = state.world.actorLocation(state.person.id());

    if (!currentCoords.isPresent()) {
      throw new RuntimeException("person " + state.person.id() + " is nowhere");
    }

    // TODO check that the item is of the right type
    Item item = state.world.items.get(state.world.itemLocations.get(currentCoords.get()));
    state.world.itemLocations.remove(currentCoords.get());
    // TODO fail if the item is no longer found
    state.person.addItem(item);

    return true;
  }

  @Override
  public String toString() {
    return String.format("TakeItem %s", itemClass.getSimpleName());
  }
}
