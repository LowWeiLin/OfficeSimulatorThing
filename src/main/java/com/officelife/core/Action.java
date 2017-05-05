package com.officelife.core;

/**
 * A means for an actor to interact with the world.
 *
 * Similar to a coroutine, in that an action may occur over several steps.
 * May have internal state. Actions should be kept simple.
 */
public interface Action {

  enum State {

    // Preserve context and continue executing the given action.
    // Sort of like a coroutine yielding.
    CONTINUE,

    // We're done. Continue with the next action.
    SUCCESS,

    // There's no way to complete this sequence of actions; re-plan
    FAILURE
  }

  State actUpon(WorldState state);
}
