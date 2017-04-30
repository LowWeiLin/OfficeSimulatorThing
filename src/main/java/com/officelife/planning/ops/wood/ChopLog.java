package com.officelife.planning.ops.wood;

import static com.officelife.Utility.set;
import com.officelife.goals.Goal;
import com.officelife.planning.Fact;
import com.officelife.planning.Op;
import com.officelife.planning.Planning;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Set;

public class ChopLog implements Op<Planning.Node> {

  @Override
  public Set<Fact> preconditions() {
    return set(new Fact("i have axe"));
  }

  @Override
  public int weight(Planning.Node state) {
    return 4;
  }

  @Override
  public Goal goal() {
    // TODO implement this
    throw new NotImplementedException();
  }

  @Override
  public Set<Fact> postconditions() {
    return set(new Fact("i have firewood"));
  }

}