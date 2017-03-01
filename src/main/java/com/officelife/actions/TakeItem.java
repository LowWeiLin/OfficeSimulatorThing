package com.officelife.actions;

import java.util.Optional;

import com.officelife.Coords;
import com.officelife.goals.State;
import com.officelife.items.Item;

/**
 * Action for picking up an item
 */
public class TakeItem<T> extends Action {
  
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

    Item item = state.world.items.get(state.world.itemLocations.get(currentCoords.get()));
    if (!itemClass.isInstance(item)) {
      throw new RuntimeException("item is not of class " + itemClass);
    }

    Coords coords = currentCoords.get();

    if (!state.world.itemLocations.containsKey(coords) || !state.world.itemLocations.get(coords).equals(item.id())) {
      throw new RuntimeException("item is no longer found at coords " + coords);
    }
    state.world.itemLocations.remove(coords);
    state.person.addItem(item);

    return true;
  }

  @Override
  public String toString() {
    return String.format("TakeItem %s", itemClass.getSimpleName());
  }
}
