package com.officelife.actions;

import java.util.Optional;

import com.officelife.goals.State;
import com.officelife.items.Item;

public class ConsumeItem<T> extends Action {

  private final Class<T> itemClass;

  public ConsumeItem(State state, Class<T> itemClass) {
    super(state);
    this.itemClass = itemClass;
  }

  @Override
  public boolean accept() {
    Optional<Item> maybe = state.actor.inventory().stream()
            .filter(itemClass::isInstance)
            .findFirst();

    if (!maybe.isPresent()) {
      return false;
    }

    Item item = maybe.get();

    // TODO perform effects for the item that was just removed
    // TODO better data structure

    state.actor.inventory().remove(item);
    return true;
  }

  @Override
  public String toString() {
    return String.format("ConsumeItem %s", itemClass.getSimpleName());
  }
}
