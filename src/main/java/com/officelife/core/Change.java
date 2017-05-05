package com.officelife.core;

/**
 * Literally a change to be applied to the world state :|
 */
public abstract class Change {

  public final WorldState state;

  public Change(WorldState state) {
    this.state = state;
  }

  public abstract boolean accept();
}
