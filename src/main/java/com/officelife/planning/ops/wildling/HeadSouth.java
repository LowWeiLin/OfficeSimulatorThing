package com.officelife.planning.ops.wildling;

import static com.officelife.Utility.set;

import java.util.Set;

import com.officelife.goals.Goal;
import com.officelife.goals.subgoals.FillVoidInSoul;
import com.officelife.planning.Fact;
import com.officelife.planning.Node;
import com.officelife.planning.Op;

public class HeadSouth implements Op<Node> {

  @Override
  public Set<Fact> preconditions() {
    return set(
            new Fact("i am not that hungry anymore")
    );
  }

  @Override
  public int weight(Node state) {
    return 1;
  }

  @Override
  public Goal goal() {
    return new FillVoidInSoul();// TODO
  }

  @Override
  public Set<Fact> postconditions() {
    return set(new Fact("I feel safe now"));
  }

  @Override
  public String toString() {
    return "Head South";
  }
}
