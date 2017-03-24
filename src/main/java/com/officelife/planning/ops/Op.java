package com.officelife.planning.ops;

import java.util.HashSet;
import java.util.Set;

import com.officelife.goals.State;
import com.officelife.planning.Fact;

public interface Op {

  // These must be in the state for a transition to occur
  Set<Fact> preconditions();

  // This gets the facts before transition
  int weight(State state, Set<Fact> facts);

  Set<Fact> postconditions();

  default Set<Fact> transition(Set<Fact> facts) {
    Set<Fact> copy = new HashSet<>(facts);

    // TODO this step isn't necessarily the best interface
    copy.removeAll(preconditions());

    copy.addAll(postconditions());
    return copy;
  }

  // This is intended to map planning to the goal space
  // Goal goal();
}
