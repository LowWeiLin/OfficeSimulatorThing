package com.officelife.goals;

/**
 * The different types of effects goal can have.
 */
public enum Outcome {

  // Preserve context and continue executing the given action.
  // Sort of like a coroutine yielding.
  CONTINUE,

  // We're done. Clear the goal stack and begin again from the root goal.
  SUCCESS,

  // Backtrack and try another possibility.
  FAILURE
}
