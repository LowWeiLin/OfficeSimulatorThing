package com.officelife.core;

import com.officelife.core.planning.Facts;

/**
 * A director tells an actor what to do.
 */
public interface Director {
  Facts getGoal(Actor actor);
}

