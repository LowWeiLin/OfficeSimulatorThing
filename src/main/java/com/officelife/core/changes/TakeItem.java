package com.officelife.core.changes;

import java.util.Optional;

import com.officelife.core.Change;
import com.officelife.core.WorldState;
import com.officelife.scenarios.items.Item;
import com.officelife.utility.Coords;

/**
 * Action for picking up an item
 */
public class TakeItem<T> extends Change {

  private final Class<T> itemClass;

  public TakeItem(WorldState state, Class<T> itemClass) {
    super(state);
    this.itemClass = itemClass;
  }


  @Override
  public boolean accept() {

    Optional<Coords> currentCoords = state.world.actorLocation(state.actor);

    if (!currentCoords.isPresent()) {
      throw new RuntimeException("actor " + state.actor.id() + " is nowhere");
    }

    Optional<Item> maybeItem = state.world.itemsAtLocation(currentCoords.get())
      .stream()
      .filter(itemClass::isInstance)
      .findFirst();
    if (!maybeItem.isPresent()) {
      return false;
    }

    Item item = maybeItem.get();


    Coords coords = currentCoords.get();

    state.world.itemsAtLocation(coords).remove(item);
    state.actor.addItem(item);

    return true;
  }

  @Override
  public String toString() {
    return String.format("TakeItem %s", itemClass.getSimpleName());
  }
}

