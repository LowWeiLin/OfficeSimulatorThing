package com.officelife.planning.ops.wildling;

import com.officelife.goals.Goal;
import com.officelife.goals.subgoals.FillVoidInSoul;
import com.officelife.planning.Fact;
import com.officelife.planning.Op;
import com.officelife.planning.Planning;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Set;

import static com.officelife.Utility.set;

public class FightForFood implements Op<Planning.Node> {

  @Override
  public Set<Fact> preconditions() {
    return set(
        new Fact("I am starving", "me", null, "hunger", 50)
    );
  }

  @Override
  public int weight(Planning.Node state) {
    return 1;
  }

  @Override
  public Goal goal() {
    return new FillVoidInSoul();
  }

  @Override
  public Set<Fact> postconditions() {
    return set(new Fact("i am not that hungry anymore"));
  }

  @Override
  public String toString() {
    return "get food";
  }
}
