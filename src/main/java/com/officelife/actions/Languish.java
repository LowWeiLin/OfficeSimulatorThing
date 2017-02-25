package com.officelife.actions;

import com.officelife.goals.State;

/**
 * The no-op action
 */
public class Languish extends Action {

  public Languish(State state) {
    super(state);
  }

  @Override
  public boolean accept() {
    // be sad and do nothing
    return true;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}

