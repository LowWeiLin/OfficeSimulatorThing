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

    Optional<Coords> currentCoords = state.world.actorLocation(state.person.id());

    if (!currentCoords.isPresent()) {
      throw new RuntimeException("person " + state.person.id() + " is nowhere");
    }

    String itemId = state.world.itemLocations
            .get(currentCoords.get())
            .stream()
            .filter(id -> {
              Item itemAtLocation = state.world.items.get(id);

              return itemClass.isInstance(itemAtLocation);
            })
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Unable to find item of " + itemClass));

    Item item = state.world.items.get(itemId);

    Coords coords = currentCoords.get();

    if (!state.world.itemLocations.containsKey(coords) || !state.world.itemLocations.get(coords).contains(item.id())) {
      throw new RuntimeException("item is no longer found at coords " + coords);
    }
    state.world.itemLocations.get(coords).remove(item.id());
    state.person.addItem(item);

    return true;
  }

  @Override
  public String toString() {
    return String.format("TakeItem %s", itemClass.getSimpleName());
  }
}
