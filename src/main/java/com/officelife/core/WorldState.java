package com.officelife.core;

import com.officelife.core.planning.Facts;
import com.officelife.core.planning.Reduction;

public class WorldState {
  // TODO parameterise this with the type of world?

  public final FirstWorld world;
  public final Actor actor;

  public WorldState(FirstWorld world, Actor actor) {
    this.world = world;
    this.actor = actor;
  }

  public Facts toFacts(Reduction reduction) {
    return reduction.reduce(this);
  }
}
