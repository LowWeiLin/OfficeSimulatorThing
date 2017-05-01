package com.officelife.planning;

import static com.officelife.Utility.isSubset;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Triple;

import com.officelife.goals.State;
import com.officelife.planning.ops.wildling.WildlingStateScore;

import astar.ISearchNode;

public abstract class Planning {


  public abstract Set<Fact> initialFacts();

  public abstract Set<Fact> goalState();

  public abstract List<Op<Node>> possibleActions();

  private State state() {
    // TODO this may not be required after all? Not sure if we want to use information from here to plan
    return null;
  }

  public static Node cast(ISearchNode other) {
    if (other instanceof Node) {
      return (Node) other;
    }
    throw new RuntimeException(other + " should not be in this set of nodes");
  }
}
