package com.officelife.goals.subgoals;

import static com.officelife.Utility.list;

import java.util.List;

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
      List<Goal> result = list();
      if (state.person.energy > 10) {
        result.add(new FindWork());
      }
      e = new Alternatives(result);
    }
    return e;
  }
}

