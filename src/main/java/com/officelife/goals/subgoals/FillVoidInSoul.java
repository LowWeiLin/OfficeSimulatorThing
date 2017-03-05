package com.officelife.goals.subgoals;

import static com.officelife.Utility.deque;

import java.util.Deque;

import com.officelife.goals.Goal;
import com.officelife.goals.State;
import com.officelife.goals.effects.Alternatives;
import com.officelife.goals.effects.Effect;

// OrGoal
public class FillVoidInSoul extends Goal {

  private Effect e = null;

  @Override
  public Effect effect(State state) {

    // The set of alternatives can depend on runtime state

    // TODO find some way to avoid this
    if (e == null) {
      Deque<Goal> result = deque();

      if (state.person.energy < 15) {
        result.add(new FillVoidInStomach());
      } else if (state.person.belonging < 50) {
        result.add(new ImproveFriendship());
      }

//      if (state.person.energy > 10) {
//        result.add(new FindWork());
//      }
      e = new Alternatives(result);
    }
    return e;
  }
}

