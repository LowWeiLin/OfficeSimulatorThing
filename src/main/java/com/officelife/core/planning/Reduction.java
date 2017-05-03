package com.officelife.core.planning;

import com.officelife.core.WorldState;

/**
 * Defines how the world is reduced into facts.
 */
public interface Reduction {
  Facts reduce(WorldState state);
}
