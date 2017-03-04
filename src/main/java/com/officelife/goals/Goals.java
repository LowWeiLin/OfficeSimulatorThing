package com.officelife.goals;

import java.util.ArrayDeque;
import java.util.Deque;

import com.officelife.actions.Action;
import com.officelife.actions.Languish;
import com.officelife.goals.effects.Effect;
import com.officelife.goals.subgoals.FillVoidInSoul;
import com.officelife.goals.subgoals.FillVoidInStomach;

public class Goals {

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
//    System.out.println("RESET");
    goals.clear();
    // TODO root goal is survive?
    if (state.person.physiology > state.person.belonging) {
      goals.push(new FillVoidInSoul());
    } else {
      goals.push(new FillVoidInStomach());
    }
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
//      System.out.println("> " + current);

      if (current.failed()) {
//        System.out.println(current + " FAILED");
        goals.pop();
        continue;
      } else if (current.succeeded()) {
//        System.out.println("SUCCEEDED");
        beginFromRootGoal(state);
        continue;
      }

      // being here means we continue with the current goal

      // explore alternatives by pushing them onto the stack

      Effect effect = goals.peek().effect(state);

      if (!effect.hasWorkLeft()) {
//        System.out.println(current + " FAILED");
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
