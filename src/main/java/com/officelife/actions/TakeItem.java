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

    Optional<String> maybeItemId = state.world.itemsAtLocation(currentCoords.get())
            .stream()
            .filter(id -> {
              Item itemAtLocation = state.world.items.get(id);

              return itemClass.isInstance(itemAtLocation);
            })
            .findFirst();
    if (!maybeItemId.isPresent()) {
      return false;
    }

    String itemId = maybeItemId.get();

    Item item = state.world.items.get(itemId);

    Coords coords = currentCoords.get();

    state.world.itemsAtLocation(coords).remove(item.id());
    state.person.addItem(item);

    return true;
  }

  @Override
  public String toString() {
    return String.format("TakeItem %s", itemClass.getSimpleName());
  }
}
