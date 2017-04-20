package com.officelife.planning.ops.wildling;

import com.officelife.goals.Goal;
import com.officelife.goals.subgoals.FillVoidInSoul;
import com.officelife.planning.Fact;
import com.officelife.planning.Op;
import com.officelife.planning.Planning;

import java.util.Set;

import static com.officelife.Utility.set;

public class HeadSouth implements Op<Planning.Node> {

  @Override
  public Set<Fact> preconditions() {
    return set(
//        new Fact("I am not starving",
//                "me", null, "hunger", 10),
//        new Fact("So many zombies",
//                    "me", null, "danger", 50)
            new Fact("i am not that hungry anymore")
    );
  }

  @Override
  public int weight(Planning.Node state) {
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
