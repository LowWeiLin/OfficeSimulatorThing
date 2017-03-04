package com.officelife.goals;

import com.officelife.goals.effects.Effect;

/**
 * Goals form an n-ary tree where the nodes are disjunctions (i.e. Prolog choice points).
 * They are traversed depth-first, left-to-right.
 *
 * When implementing goals, side effects within goals are fine, but avoid effects on the
 * outside world, as you can never tell how a goal may backtrack.
 */
public abstract class Goal {

  // The valid states are:

  // failed = false, succeeded = false => current goal yielded; continue it
  // failed = false, succeeded = true => succeed and clear the stack
  // failed true => fail and backtrack

  // There is no decay. Each goal decides on its own notion of decay.

  boolean failed() {
    return hasFailed();
  }

  boolean succeeded() {
    return !failed() && hasSucceeded();
  }

  /**
   * If this returns true, we will backtrack.
   * Override if necessary.
   */
  public boolean hasFailed() {
    return false;
  }

  /**
   * If this returns true and we haven't failed, clear the goal stack and begin again from the root goal.
   * Override if necessary.
   */
  public boolean hasSucceeded() {
    return false;
  }

  /**
   * The effect of a goal is either to create a choice point, or return some action.
   * Returning in here is kind of like yielding in a coroutine.
   */
  public abstract Effect effect(State state);

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}


