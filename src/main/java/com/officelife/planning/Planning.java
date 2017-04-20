package com.officelife.planning;

import static com.officelife.Utility.isSubset;
import static com.officelife.Utility.list;
import static com.officelife.Utility.set;

import java.util.*;
import java.util.stream.Collectors;

import astar.*;
import com.officelife.goals.State;
import com.officelife.planning.ops.wood.ChopLog;
import com.officelife.planning.ops.wood.CollectBranches;
import com.officelife.planning.ops.wood.GetAxe;
import org.apache.commons.lang3.tuple.Triple;

public abstract class Planning {


  public abstract Set<Fact> initialState();

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



  private static boolean meetsPreconditions(Set<Fact> toBeMet, Set<Fact> known) {
    return isSubset(toBeMet, known);
  }

  public static class Node extends ASearchNode implements OpNode<Node> {

    public final Set<Fact> facts;

    // The cost of getting here from the predecessor of this node.
    // TODO is there a less awkward way to store this?
    final int costFromPred;

    final List<Op<Node>> possibleActions;

    final State state;

    Planning planningContext;

    Op<Node> op;

    public Node(
            Planning planningContext, int costFromPred, Set<Fact> facts, List<Op<Node>> possibleActions, State state,
            Op<Node> op) {
      this.planningContext = planningContext;
      this.facts = facts;
      this.costFromPred = costFromPred;
      this.possibleActions = possibleActions;
      this.state = state;
      this.op = op;
    }

    @Override
    public double h() {
      // The heuristic is the percentage of the goal contained in the current state.
      // TODO take into account the facts in the current state that do not contribute to the goal?

      Set<Fact> remaining = new HashSet<>(planningContext.goalState());
      int original = remaining.size();
      remaining.removeAll(this.facts);
      int contained = original - remaining.size();
      double intersection = (double) contained / (double) planningContext.goalState().size();

      // Higher percentage => lower cost

      double n = 0;
      double cost = (1 - intersection) * n;

      // No idea what value of n relates % overlap and actual cost so that h is admissible,
      // given that actual cost is arbitrarily decided.
      // n = 0 is definitely correct, n = 1 is probably correct
      return cost;
    }

    @Override
    public List<ISearchNode> getSuccessors() {
      List<Op<Node>> chosen = this.possibleActions.stream()
              .filter(o -> meetsPreconditions(o.preconditions(), facts))
              .collect(Collectors.toList());
//      return null;
//
      return chosen.stream()
              .map(o -> Triple.of(o.weight(this), o.transition(facts), o))
              .map(e -> new Node(
                      planningContext, e.getLeft(), e.getMiddle(), possibleActions, state, e.getRight()))
              .collect(Collectors.toList());
    }

    @Override
    public double c(ISearchNode successor) {
      return cast(successor).costFromPred;
    }

    // More trivial stuff

    Node parent;

    @Override
    public ISearchNode getParent() {
      return parent;
    }

    @Override
    public void setParent(ISearchNode parent) {
      this.parent = cast(parent);
    }

    @Override
    public Integer keyCode() {
      return null;
    }

    @Override
    public String toString() {
      return String.format("[%s]", facts.stream()
        .map(Object::toString)
        .collect(Collectors.joining(", ")));
    }

    @Override
    public Op<Node> op() {
      return op;
    }
  }


}
