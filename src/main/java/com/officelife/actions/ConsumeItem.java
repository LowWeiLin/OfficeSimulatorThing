package com.officelife.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    Map<Boolean, List<Item>> partitioned = state.person.inventory.stream().collect(
      Collectors.groupingBy(itemClass::isInstance, Collectors.toList()));

    if (!partitioned.containsKey(true) || partitioned.get(true).isEmpty()) {
      // failed to consume
      return false;
    }

    List<Item> notRemoved = new ArrayList<>(partitioned.get(true));

    // TODO perform effects for the item that was just removed
    // TODO better data structure
    notRemoved.remove(0);

    state.person.inventory.clear();
    if (partitioned.containsKey(false)) {
      state.person.inventory.addAll(partitioned.get(false));
    }
    state.person.inventory.addAll(notRemoved);

    return true;
  }

  @Override
  public String toString() {
    return String.format("ConsumeItem %s", itemClass.getSimpleName());
  }
}
