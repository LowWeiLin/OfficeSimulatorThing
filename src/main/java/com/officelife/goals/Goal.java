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

  public Outcome outcome() {
    return Outcome.CONTINUE;
  }

  /**
   * The effect of a goal is either to create a choice point, or return some action.
   * Returning in here is kind of like yielding in a coroutine (provided the outcome
   * is CONTINUE).
   */
  public abstract Effect effect(State state);

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}


