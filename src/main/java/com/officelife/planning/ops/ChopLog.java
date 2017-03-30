package com.officelife.planning.ops;

import static com.officelife.Utility.set;

import java.util.Set;

import com.officelife.goals.Goal;
import com.officelife.goals.State;
import com.officelife.planning.Fact;
import com.officelife.planning.Planning;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class ChopLog implements Op<Planning.Node> {

//  @Override
//  public Set<Fact> preconditions() {
//    return set(new Fact("i have axe"));
//  }

  @Override
  public int weight(Planning.Node state) {
    return 4;
  }

  @Override
  public Goal goal() {
    // TODO implement this
    throw new NotImplementedException();
  }

//  @Override
//  public Set<Fact> postconditions() {
//    return set(new Fact("i have firewood"));
//  }

}
