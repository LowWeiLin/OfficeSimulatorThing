package com.officelife.goals.subgoals;

import static com.officelife.Utility.deque;

import com.officelife.goals.Goal;
import com.officelife.goals.State;
import com.officelife.goals.effects.Alternatives;
import com.officelife.goals.effects.Effect;
import com.officelife.items.Coffee;

// OrGoal
public class FillVoidInStomach extends Goal {


  @Override
  public Effect effect(State state) {

    // TODO proper decision making
    if (state.person.belonging > 5
            && state.world.itemLocation(i -> i instanceof Coffee).isPresent()) {
      return new Alternatives(deque(new GetCoffee()));
    } else {
      return new Alternatives(deque(new PunchPeopleForFood()));
    }

  }
}


