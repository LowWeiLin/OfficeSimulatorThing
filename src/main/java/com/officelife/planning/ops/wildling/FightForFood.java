package com.officelife.planning.ops.wildling;

import static com.officelife.Utility.set;

import java.util.Set;

import com.officelife.goals.Goal;
import com.officelife.goals.subgoals.FillVoidInSoul;
import com.officelife.planning.Fact;
import com.officelife.planning.Node;
import com.officelife.planning.Op;

public class FightForFood implements Op<Node> {

  @Override
  public Set<Fact> preconditions() {
    return set(
        new Fact("I am starving", "me", null, "hunger", 50)
    );
  }

  @Override
  public int weight(Node state) {
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
