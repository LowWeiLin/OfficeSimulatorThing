package com.officelife.goals.subgoals;

import static com.officelife.Utility.deque;

import com.officelife.actors.Person;
import com.officelife.goals.Goal;
import com.officelife.goals.State;
import com.officelife.goals.effects.Alternatives;
import com.officelife.goals.effects.Effect;
import com.officelife.items.Food;

// OrGoal
public class FillVoidInStomach extends Goal {


  @Override
  public Effect effect(State state) {
    if (!(state.actor instanceof Person)) {
      throw new RuntimeException("FillVoidInSoul requires person as actor");
    }
    Person person = (Person)state.actor;

    // TODO proper decision making
    if (person.belonging > 5
            && state.world.closestLocation(i -> i instanceof Food,
            state.world.actorLocation(person).orElseThrow(() -> new RuntimeException("actor not found"))
    ).isPresent()) {
      return new Alternatives(deque(new GetFood()));
    } else {
      return new Alternatives(deque(new Attack()));
    }

  }
}


