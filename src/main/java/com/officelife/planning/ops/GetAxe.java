package com.officelife.planning.ops;

import java.util.Set;
import static com.officelife.Utility.set;

import com.officelife.goals.State;
import com.officelife.planning.Fact;

public class GetAxe implements Op {

  @Override
  public Set<Fact> preconditions() {
    return set(new Fact("an axe is available"), new Fact("i don't have axe"));
  }

  @Override
  public int weight(State state, Set<Fact> facts) {
    return 2;
  }

  @Override
  public Set<Fact> postconditions() {
    return set(new Fact("i have axe"));
  }
}
