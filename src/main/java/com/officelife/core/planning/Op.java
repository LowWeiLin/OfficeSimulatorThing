package com.officelife.core.planning;

import com.officelife.core.Action;

/**
 * Operations on nodes in the search space.
 *
 * TODO probably don't need the type parameter
 *
 * @param <A> the type of node
 */
public interface Op<A> {

  // These must be in the state for a transition to occur
  Facts preconditions();

  // This gets the facts before transition
  int weight(A state);

  Facts postconditions();

  Action action();
}

