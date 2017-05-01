package com.officelife.actions;

import java.util.Optional;

import com.officelife.Main;
import com.officelife.actors.Person;
import com.officelife.goals.State;
import com.officelife.items.Consumable;
import com.officelife.items.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumeItem<T> extends Action {

  private static final Logger logger = LoggerFactory.getLogger(ConsumeItem.class);

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

    logger.info("before consuming, energy was " + ((Person)state.actor).energy);
    if (item instanceof Consumable) {
      ((Consumable)item).consumedBy(state.actor);
    }
    logger.info("after consuming, energy was " + ((Person)state.actor).energy);

    state.actor.inventory().remove(item);
    return true;
  }

  @Override
  public String toString() {
    return String.format("ConsumeItem %s", itemClass.getSimpleName());
  }
}
