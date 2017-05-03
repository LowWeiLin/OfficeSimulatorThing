package com.officelife.core;

import java.util.ArrayDeque;
import java.util.Deque;

import com.officelife.core.planning.Facts;
import com.officelife.core.planning.Search;
import com.officelife.scenarios.wood.WoodcutterSearch;

/**
 * How an actor receives goals and chooses actions to fulfill them.
 */
public class Planning {

//  private static final Logger logger = LoggerFactory.getLogger(Planning.class);

  private Deque<Action> actions = new ArrayDeque<>();

  private final Search search = new WoodcutterSearch();

  public Action getAction(WorldState state, Facts goal) {
    if (actions.isEmpty()) {
      replan(state, goal);
    }
    return actions.peek();
  }

  public void replan(WorldState state, Facts goal) {
    actions = search.determineActions(state, goal);
  }

  public void act(WorldState state, Facts goal) {
    Action.State s = getAction(state, goal).actUpon(state);
    switch (s) {
      case SUCCESS:
        actions.pop();
        break;
      case FAILURE:
//        replan();
        // in case planning is expensive? do it when it's my turn
        actions.clear();
        break;
      case CONTINUE:
        // do nothing
        break;
    }
  }

}
