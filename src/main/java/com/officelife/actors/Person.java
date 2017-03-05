package com.officelife.actors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.officelife.World;
import com.officelife.actions.Action;
import com.officelife.goals.Goals;
import com.officelife.goals.State;
import com.officelife.items.Item;

public class Person implements Actor {

  public int physiology = 10;
  public int belonging = 10;

  public int energy = 100;

  private final Goals g = new Goals();

  private final String id;

  public Person(String id, int physiology, int belonging, int energy) {
    this.id = id;
    this.physiology = physiology;
    this.belonging = belonging;
    this.energy = energy;
  }

  public Person(String id) {
    this(id, 10, 10, 10);
  }

  public final List<Item> inventory = new ArrayList<>();

  public final Map<String, Integer> relationships = new HashMap<>();

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
  public boolean isDead() {return this.energy <= 0;}

  @Override
  public char[][] textRepresentation() {
    return new char[][]{{'P'}};
  }
}
