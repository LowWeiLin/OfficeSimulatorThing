package com.officelife.goals.subgoals;

import static com.officelife.Utility.deque;

import com.officelife.goals.Goal;
import com.officelife.goals.State;
import com.officelife.goals.effects.Alternatives;
import com.officelife.goals.effects.Effect;

// OrGoal
public class FillVoidInStomach extends Goal {
  // Alternatives are stateful, so we need to return the same object every time,
  // which is why we store and initialise it here. We only need this for TerminalActions
  // if they are stateful as well, but they usually don't have to be because goals can be
  // stateful. TODO is there a better alternative?
  private Effect e;

  @Override
  public Effect effect(State state) {

    // TODO proper decision making
    if (state.person.belonging > 5) {
      e = new Alternatives(deque(new GetCoffee()));
    } else {
      e = new Alternatives(deque(new PunchPeopleForFood()));
    }
    return e;

  }
}


