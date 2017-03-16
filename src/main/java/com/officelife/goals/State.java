package com.officelife.goals;

import com.officelife.World;
import com.officelife.actors.Actor;

public class State {
  public final World world;
  public final Actor actor;

  public State(World world, Actor actor) {
    this.world = world;
    this.actor = actor;
  }
}
