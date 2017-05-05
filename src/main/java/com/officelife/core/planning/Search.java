package com.officelife.core.planning;

import java.util.Deque;
import java.util.List;

import com.officelife.core.Action;
import com.officelife.core.WorldState;

/**
 * How to determine what abstract actions are needed to reach a goal.
 */
public interface Search {

  List<Op<Node>> operations();

  Deque<Action> determineActions(WorldState state, Facts goal);
}
