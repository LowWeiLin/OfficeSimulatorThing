package com.officelife.planning.ops;

import static com.officelife.Utility.set;

import java.util.Set;

import com.officelife.goals.State;
import com.officelife.planning.Fact;

public class CollectBranches implements Op {

  @Override
  public Set<Fact> preconditions() {
    return set();
  }

  @Override
  public int weight(State state, Set<Fact> facts) {
//    return 5;
    return 8;
  }

  @Override
  public Set<Fact> postconditions() {
    return set(new Fact("i have firewood"));
  }
}
