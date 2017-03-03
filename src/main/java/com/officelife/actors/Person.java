package com.officelife.actors;

import java.util.ArrayList;
import java.util.List;

import com.officelife.World;
import com.officelife.actions.Action;
import com.officelife.goals.Goals;
import com.officelife.goals.State;
import com.officelife.items.Item;

public class Person implements Actor {

  public int physiology = 10;
  public int belonging = 1;

  public int energy = 100;

  private final Goals g = new Goals();

  private final String id;

  public Person(String id) {
    this.id = id;
  }

  public final List<Item> inventory = new ArrayList<>();

  // TODO relationships

  @Override
  public String id() {
    return id;
  }

  @Override
  public Action act(World world, boolean succeeded) {
    return g.plan(new State(world, this), succeeded);
  }

  @Override
  public void addItem(Item item) {
    inventory.add(item);
  }

  @Override
  public char[][] textRepresentation() {
    return new char[][]{{'P'}};
  }
}
