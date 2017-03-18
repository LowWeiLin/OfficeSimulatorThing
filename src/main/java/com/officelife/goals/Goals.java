package com.officelife.goals;

import java.util.ArrayDeque;
import java.util.Deque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.officelife.actions.Action;
import com.officelife.actions.Languish;
import com.officelife.goals.effects.Effect;
import com.officelife.goals.subgoals.FillVoidInSoul;

public class Goals {

  private static final Logger logger = LoggerFactory.getLogger(Goals.class);
  private final Deque<Goal> goals = new ArrayDeque<>();

  public Action plan(State state, boolean previousSucceeded) {
    if (goals.isEmpty()) {
      beginFromRootGoal(state);
    } else if (!previousSucceeded) {
      goals.pop();
    }
    return search(state);
  }

  // TODO generalise this. it should depend on actor
  private void beginFromRootGoal(State state) {
//    logger.debug("RESET");

    goals.clear();
    // TODO root goal is survive?
//    if (state.actor.physiology > state.actor.belonging) {
    goals.push(new FillVoidInSoul());
//    } else {
//      goals.push(new FillVoidInStomach());
//    }
  }

  /**
   * Low-level implementation of a generic depth-first search strategy that works across goals
   */
  private Action search(State state) {
    while (true) {
      if (goals.isEmpty()) {
        return new Languish(state);
      }

      Goal current = goals.peek();
//      logger.debug("> {}", current);

      switch (current.outcome()) {
        case FAILURE:
          goals.pop();
          continue;
        case SUCCESS:
          beginFromRootGoal(state);
          continue;
      }

      // being here means we continue with the current goal

      // explore alternatives by pushing them onto the stack

      Effect effect = goals.peek().effect(state);

      if (!effect.hasWorkLeft()) {
//        logger.debug("{} failed", current);
        goals.pop();
        continue;
      }

      if (effect.isTerminal()) {
        return effect.getTerminalAction().action;
      } else {
        Deque<Goal> alternatives = effect.getAlternatives().alternatives;
        
        // TODO don't do this when debugging
//      Collections.shuffle(alternatives);

        Goal next = alternatives.pop();

        goals.push(next);
      }
    }
  }
}
