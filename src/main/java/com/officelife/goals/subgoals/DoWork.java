package com.officelife.goals.subgoals;

import com.officelife.actions.Action;
import com.officelife.goals.Goal;
import com.officelife.goals.Outcome;
import com.officelife.goals.State;
import com.officelife.goals.effects.Effect;
import com.officelife.goals.effects.TerminalAction;

// AndGoal
public class DoWork extends Goal {

  @Override
  public Outcome outcome() {
    return Outcome.FAILURE;
  }

  @Override
  public Effect effect(State state) {
    // Doesn't really matter what is returned here because this always fails
    return new TerminalAction(new Action(state) {
      @Override
      public boolean accept() {
        return true;
      }

      @Override
      public String toString() {
        return "doing work";
      }
    });
  }
}

